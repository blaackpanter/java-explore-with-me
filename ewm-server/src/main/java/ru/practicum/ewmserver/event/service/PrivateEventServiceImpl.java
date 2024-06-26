package ru.practicum.ewmserver.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewmserver.category.model.Category;
import ru.practicum.ewmserver.category.storage.CategoryRepository;
import ru.practicum.ewmserver.error.exception.DataConflictException;
import ru.practicum.ewmserver.error.exception.EntityNotFoundException;
import ru.practicum.ewmserver.error.exception.InvalidRequestException;
import ru.practicum.ewmserver.event.dto.*;
import ru.practicum.ewmserver.event.mapper.EventMapper;
import ru.practicum.ewmserver.event.model.Event;
import ru.practicum.ewmserver.event.model.EventState;
import ru.practicum.ewmserver.event.storage.EventRepository;
import ru.practicum.ewmserver.request.dto.ParticipationRequestDto;
import ru.practicum.ewmserver.request.mapper.RequestMapper;
import ru.practicum.ewmserver.request.model.Request;
import ru.practicum.ewmserver.request.model.RequestStatus;
import ru.practicum.ewmserver.request.storage.RequestRepository;
import ru.practicum.ewmserver.user.model.User;
import ru.practicum.ewmserver.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.ewmserver.error.constants.ErrorStrings.*;

@Service
@RequiredArgsConstructor
public class PrivateEventServiceImpl implements PrivateEventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;

    @Override
    @Transactional
    public EventFullDto postEvent(NewEventDto newEventDto, int userId) {
        if (LocalDateTime.now().plusHours(2).isAfter(newEventDto.getEventDate())) {
            throw new InvalidRequestException(EVENT_DATE_2_HOURS_MIN_SHOULD_BE + newEventDto.getEventDate());
        }
        final User userFromDb = userRepository.findById(userId)
                .orElseThrow(
                        () -> new EntityNotFoundException(String.format(USER_NOT_FOUND_BY_ID, userId))
                );
        final Category categoryFromDb = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(
                        () -> new EntityNotFoundException(String.format(CATEGORY_NOT_FOUND_BY_ID, newEventDto.getCategory()))
                );
        final Event event = EventMapper.createEvent(newEventDto, userFromDb, categoryFromDb);
        final Event eventFromDb = eventRepository.save(event);
        return EventMapper.createEventFullDto(
                eventFromDb,
                requestRepository.countRequestByEventIdAndStatus(eventFromDb.getId(), RequestStatus.CONFIRMED)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getUserEvents(int userId, int from, int size) {
        final PageRequest pageRequest = PageRequest.of(from > 0 ? from / size : 0, size);
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException(String.format(USER_NOT_FOUND_BY_ID, userId));
        }
        return eventRepository.getByInitiatorId(userId, pageRequest).stream()
                .map(
                        event -> EventMapper.createEventShortDto(
                                event,
                                requestRepository.countRequestByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED))
                )
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventById(int userId, int eventId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException(String.format(USER_NOT_FOUND_BY_ID, userId));
        }
        final Event eventFromDb = eventRepository.findById(eventId)
                .orElseThrow(
                        () -> new EntityNotFoundException(String.format(EVENT_NOT_FOUND_BY_ID, eventId))
                );
        return EventMapper.createEventFullDto(
                eventFromDb,
                requestRepository.countRequestByEventIdAndStatus(eventFromDb.getId(), RequestStatus.CONFIRMED)
        );
    }

    @Override
    @Transactional
    public EventFullDto patchEvent(UpdateEventUserRequest updateEventUserRequest, int userId, int eventId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException(String.format(USER_NOT_FOUND_BY_ID, userId));
        }
        final Event eventFromDb = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(EVENT_NOT_FOUND_BY_ID, eventId)));
        if (eventFromDb.getState().equals(EventState.PUBLISHED)) {
            throw new DataConflictException(PATCH_NOT_PENDING_STATE);
        }
        if (updateEventUserRequest.getStateAction() != null) {
            switch (updateEventUserRequest.getStateAction()) {
                case CANCEL_REVIEW:
                    eventFromDb.setState(EventState.CANCELED);
                    break;
                case SEND_TO_REVIEW:
                    eventFromDb.setState(EventState.PENDING);
                    break;
                default:
                    throw new InvalidRequestException(INVALID_ACTION + eventFromDb);
            }
        }
        if (updateEventUserRequest.getEventDate() != null) {
            if (LocalDateTime.now().plusHours(2).isAfter(updateEventUserRequest.getEventDate())) {
                throw new InvalidRequestException(EVENT_DATE_2_HOURS_MIN_SHOULD_BE);
            }
            eventFromDb.setEventDate(updateEventUserRequest.getEventDate());
        }
        if (updateEventUserRequest.getAnnotation() != null) {
            eventFromDb.setAnnotation(updateEventUserRequest.getAnnotation());
        }
        if (updateEventUserRequest.getCategory() != null &&
                updateEventUserRequest.getCategory() != (eventFromDb.getCategory().getId())) {
            Category category = categoryRepository.findById(updateEventUserRequest.getCategory())
                    .orElseThrow(
                            () -> new EntityNotFoundException(
                                    String.format(CATEGORY_NOT_FOUND_BY_ID, updateEventUserRequest.getCategory())
                            )
                    );
            eventFromDb.setCategory(category);
        }
        if (updateEventUserRequest.getDescription() != null) {
            eventFromDb.setDescription(updateEventUserRequest.getDescription());
        }
        if (updateEventUserRequest.getLocation() != null) {
            eventFromDb.setLocation(updateEventUserRequest.getLocation());
        }
        if (updateEventUserRequest.getPaid() != null) {
            eventFromDb.setPaid(updateEventUserRequest.getPaid());
        }
        if (updateEventUserRequest.getRequestModeration() != null) {
            eventFromDb.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }
        if (updateEventUserRequest.getTitle() != null) {
            eventFromDb.setTitle(updateEventUserRequest.getTitle());
        }
        if (updateEventUserRequest.getParticipantLimit() != null) {
            eventFromDb.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }
        return EventMapper.createEventFullDto(
                eventRepository.save(eventFromDb),
                requestRepository.countRequestByEventIdAndStatus(eventFromDb.getId(), RequestStatus.CONFIRMED)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsInEvent(int userId, int eventId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException(String.format(USER_NOT_FOUND_BY_ID, userId));
        }
        if (!eventRepository.existsById(eventId)) {
            throw new EntityNotFoundException(String.format(EVENT_NOT_FOUND_BY_ID, eventId));
        }
        return requestRepository.getRequestsByEventId(eventId).stream()
                .map(RequestMapper::createParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult patchRequests(EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest,
                                                        int userId,
                                                        int eventId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException(String.format(USER_NOT_FOUND_BY_ID, userId));
        }
        if (!eventRepository.existsById(eventId)) {
            throw new EntityNotFoundException(String.format(EVENT_NOT_FOUND_BY_ID, eventId));
        }
        final List<Request> requests = requestRepository.findAllById(eventRequestStatusUpdateRequest.getRequestIds());
        switch (eventRequestStatusUpdateRequest.getStatus()) {
            case REJECTED:
                for (Request request : requests) {
                    if (request.getStatus() == RequestStatus.CONFIRMED) {
                        throw new DataConflictException("Cannot reject an already confirmed request.");
                    }
                    request.setStatus(RequestStatus.REJECTED);
                }
                break;
            case CONFIRMED:
                for (Request request : requests) {
                    if (!request.getEvent().getRequestModeration() || request.getEvent().getParticipantLimit() == 0) {
                        request.setStatus(RequestStatus.CONFIRMED);
                        continue;
                    }
                    final int countRequestByEventIdAndStatus = requestRepository.countRequestByEventIdAndStatus(
                            request.getEvent().getId(),
                            RequestStatus.CONFIRMED
                    );
                    if (countRequestByEventIdAndStatus >= request.getEvent().getParticipantLimit()) {
                        request.setStatus(RequestStatus.REJECTED);
                        throw new DataConflictException("Cant accept request.");
                    }
                    request.setStatus(RequestStatus.CONFIRMED);
                }
                break;
        }
        return RequestMapper.createEventRequestStatusUpdateResult(requestRepository.saveAll(requests));
    }

}

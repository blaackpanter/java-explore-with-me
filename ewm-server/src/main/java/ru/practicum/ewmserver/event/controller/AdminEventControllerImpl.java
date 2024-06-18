package ru.practicum.ewmserver.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewmserver.event.dto.EventFullDto;
import ru.practicum.ewmserver.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewmserver.event.service.AdminEventService;
import ru.practicum.statdto.dto.Constants;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
@Validated
public class AdminEventControllerImpl {
    private final AdminEventService adminEventService;

    @PatchMapping("/{eventId}")
    public EventFullDto patchEvent(@RequestBody @Valid UpdateEventAdminRequest updateEventAdminRequest,
                                   @PathVariable @PositiveOrZero int eventId) {
        return adminEventService.patchEvent(updateEventAdminRequest, eventId);
    }

    @GetMapping
    public List<EventFullDto> getEvents(@RequestParam(required = false) List<Integer> users,
                                        @RequestParam(required = false) List<String> states,
                                        @RequestParam(required = false) List<Integer> categories,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = Constants.DATETIME_FORMAT) LocalDateTime rangeStart,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = Constants.DATETIME_FORMAT) LocalDateTime rangeEnd,
                                        @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                        @RequestParam(defaultValue = "10") @Positive int size) {
        return adminEventService.getEvents(users, states, categories, rangeStart, rangeEnd, from, size);
    }
}

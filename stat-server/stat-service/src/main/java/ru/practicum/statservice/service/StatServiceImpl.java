package ru.practicum.statservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.statdto.dto.EndpointHitDto;
import ru.practicum.statdto.dto.ViewStatsDto;
import ru.practicum.statservice.mapper.EndPointHitMapper;
import ru.practicum.statservice.model.EndpointHit;
import ru.practicum.statservice.storage.StatServiceRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {

    private final StatServiceRepository statServiceRepository;

    @Transactional()
    @Override
    public EndpointHitDto postEndpointHit(EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = EndPointHitMapper.createEndPointHit(endpointHitDto);
        EndpointHit endpointHitFromDb = statServiceRepository.save(endpointHit);
        return EndPointHitMapper.createEndPointHitDto(endpointHitFromDb);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<ViewStatsDto> getStat(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (unique) {
            if (uris.isEmpty()) {
                return statServiceRepository.getStatNoUrisUniqueIp(start, end);
            }
            return statServiceRepository.getStatUniqueIp(start, end, uris);

        } else {
            if (uris.isEmpty()) {
                return statServiceRepository.getStatNoUris(start, end);
            }
            return statServiceRepository.getStat(start, end, uris);
        }
    }
}

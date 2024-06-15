package ru.practicum.statservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.statdto.dto.EndpointHitDto;
import ru.practicum.statdto.dto.ViewStatsDto;
import ru.practicum.statservice.service.StatService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.statdto.dto.Constants.DATETIME_FORMAT;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StatServiceControllerImpl {
    private final StatService statServiceService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/hit")
    public EndpointHitDto postEndpoint(@RequestBody EndpointHitDto endpointHitDto) {
        log.debug("Вызван метод postEndpoint");
        return statServiceService.postEndpointHit(endpointHitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStat(@RequestParam @DateTimeFormat(pattern = DATETIME_FORMAT) LocalDateTime start,
                                      @RequestParam @DateTimeFormat(pattern = DATETIME_FORMAT) LocalDateTime end,
                                      @RequestParam(defaultValue = "") List<String> uris,
                                      @RequestParam(defaultValue = "false") Boolean unique) {
        log.debug("Вызван метод getStat");
        return statServiceService.getStat(start, end, uris, unique);
    }
}

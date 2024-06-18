package ru.practicum.ewmserver.compilation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewmserver.compilation.dto.CompilationDto;
import ru.practicum.ewmserver.compilation.service.PublicCompilationService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
@Validated
public class PublicCompilationControllerImpl {
    private final PublicCompilationService publicCompilationService;

    @GetMapping
    public List<CompilationDto> getCompilation(@RequestParam(defaultValue = "false") boolean pinned,
                                               @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                               @RequestParam(defaultValue = "10") @Positive int size) {
        return publicCompilationService.getCompilation(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable @PositiveOrZero int compId) {
        return publicCompilationService.getCompilationById(compId);
    }
}

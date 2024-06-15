package ru.practicum.ewmserver.category.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewmserver.category.dto.CategoryDto;
import ru.practicum.ewmserver.category.dto.NewCategoryDto;
import ru.practicum.ewmserver.category.service.AdminCategoryService;

import javax.validation.Valid;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
@Validated
public class AdminCategoryControllerImpl {
    private final AdminCategoryService adminCategoryService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto postCategory(@RequestBody @Valid NewCategoryDto newCategoryDto) {
        return adminCategoryService.postCategoryAdmin(newCategoryDto);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable @PositiveOrZero int catId) {
        adminCategoryService.deleteCategoryAdmin(catId);
    }

    @PatchMapping("/{catId}")
    public CategoryDto patchCategory(@RequestBody @Valid CategoryDto categoryDto,
                                     @PathVariable @PositiveOrZero int catId) {
        return adminCategoryService.patchCategoryAdmin(categoryDto, catId);
    }
}

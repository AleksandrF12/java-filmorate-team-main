package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@Slf4j
public class MpaController {
    private final MpaService mpaService;

    public MpaController(MpaService mpaService) {
        this.mpaService = mpaService;
    }

    //получение всех рейтингов MPA
    @GetMapping
    protected List<MPA> getMpas() {
        log.info("Получен запрос на чтение рейтингов MPA.");
        return mpaService.getMpas();
    }

    //получение рейтинга MPA по id
    @GetMapping("/{id}")
    protected MPA getMpa(@PathVariable("id") int mpaId) {
        log.info("Получен запрос на чтение рейтинга MPA с id={}",mpaId);
        return mpaService.getMpa(mpaId);
    }
}

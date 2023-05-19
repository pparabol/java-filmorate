package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class Mpa {

    private final int id;

    @NotBlank
    private final String name;

}

package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.validator.DateBefore;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
//генерирует @Getter,@Setter,@ToString,@EqualsAndHashCode,@RequiredArgsConstructor
public class Film {
    private long id; //целочисленный идентификатор

    @NotBlank
    private String name; //название

    @Size(max = 200)
    private String description; //описание

    @DateBefore
    private LocalDate releaseDate; //дата релиза

    @Positive
    private int duration; //продолжительность фильма

    private int rate;

    private MPA mpa; //рейтинг фильма

    private Set<Genre> genres; //жанр фильма

    public Set<Genre> getGenres() {
        return genres;
    }

    public void setGenres(Set<Genre> genres) {
        this.genres = genres;
    }

    public void addGenres(Genre genre) {
        this.genres.add(genre);
    }
}

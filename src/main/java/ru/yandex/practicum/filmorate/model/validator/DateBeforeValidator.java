package ru.yandex.practicum.filmorate.model.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class DateBeforeValidator implements ConstraintValidator<DateBefore,LocalDate> {

    private final LocalDate dateMin=LocalDate.of(1895,12,28);

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext cxt) {
        if(date == null||date.isBefore(dateMin)) {
            return false;
        }
        return  date.toString().matches("^\\d{4}-\\d{2}-\\d{2}$");
    }
}

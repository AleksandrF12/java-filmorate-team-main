package ru.yandex.practicum.filmorate.model.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = DateBeforeValidator.class)
public @interface DateBefore {
    String message() default "{Дата релиза не может быть раньше 1895-12-28}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}

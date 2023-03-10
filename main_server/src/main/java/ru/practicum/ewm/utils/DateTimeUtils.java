package ru.practicum.ewm.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateTimeUtils {

    private static final String DT_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern(DT_FORMAT);

    public static LocalDateTime parse(@NonNull final String datetime) throws ValidationException {
        try {
            return LocalDateTime.parse(datetime, DT_FORMATTER);
        } catch (final DateTimeParseException exc) {
            throw new ValidationException(exc.getMessage(), exc);
        }
    }

    public static String format(@NonNull final LocalDateTime datetime) {
        return datetime.format(DT_FORMATTER);
    }
}

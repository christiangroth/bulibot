package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestUtils {

    public static final String DATE_PATTERN = "dd.MM.yyyy HH:mm";

    public static LocalDateTime date(String dateTimeString) {
        return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(DATE_PATTERN));
    }
}

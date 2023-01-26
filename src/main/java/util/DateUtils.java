package util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Date;

public final class DateUtils {

    public static final String PATTERN_OPENLIGADB = "yyyy-MM-dd'T'HH:mm:ss";

    public static final String PATTERN_BULIBOT = "dd.MM HH:mm";

    public static Comparator<LocalDateTime> OLDEST_FIRST = (o1, o2) -> o1.compareTo(o2);

    public static Comparator<LocalDateTime> NEWEST_FIRST = (o1, o2) -> o2.compareTo(o1);

    private DateUtils() {
        // utility class, private constructor
    }

    public static LocalDateTime convertDate(Date value) {
        return LocalDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault());
    }

    public static Date convertLocalDateTime(LocalDateTime value) {
        return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDateTime between(LocalDateTime earlyDate, LocalDateTime lateDate) {
        return earlyDate.plus(ChronoUnit.SECONDS.between(earlyDate, lateDate) / 2, ChronoUnit.SECONDS);
    }

    public static LocalDateTime parseOpenligaDbDate(String utcTimeZoneString) {
        return LocalDateTime.parse(utcTimeZoneString, java.time.format.DateTimeFormatter.ofPattern(PATTERN_OPENLIGADB));
    }

    public static int currentYear() {
        return LocalDateTime.now().getYear();
    }

    public static String format(LocalDateTime scheduled, String pattern) {
        return DateTimeFormatter.ofPattern(pattern).format(scheduled);
    }
}

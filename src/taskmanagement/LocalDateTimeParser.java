package taskmanagement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeParser {

    static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");

    static LocalDateTime parseToLocalDateTime(String date) {
        return LocalDateTime.parse(date, dateTimeFormatter);
    }

    static String localDateTimeToString(LocalDateTime date) {
        return date.format(dateTimeFormatter);
    }

}

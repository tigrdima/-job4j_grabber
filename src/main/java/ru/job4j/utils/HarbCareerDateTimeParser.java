package ru.job4j.utils;

import java.time.LocalDateTime;

public class HarbCareerDateTimeParser implements DateTimeParser {

    @Override
    public LocalDateTime parse(String parse) {
        String rsl = parse.substring(0, 16);
        return LocalDateTime.parse(rsl);
    }
}

package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.utils.DateTimeParser;
import ru.job4j.utils.HarbCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;

public class HabrCareerParse {
    private static int countPages;
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=%d", SOURCE_LINK, countPages);

    public static void main(String[] args) throws IOException {
        for (countPages = 1; countPages <= 5; countPages++) {
            Connection connection = Jsoup.connect(PAGE_LINK);

            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                Element dateTitleElement = row.select(".vacancy-card__date").first();

                DateTimeParser time = new HarbCareerDateTimeParser();
                LocalDateTime date = time.parse(
                        dateTitleElement
                                .child(0)
                                .attr("datetime"));

                String vacancyName = titleElement.text();
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                System.out.printf("%s %s %s%n", date.toString(), vacancyName, link);
            });
        }
    }
}

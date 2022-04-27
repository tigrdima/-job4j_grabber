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
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = "/vacancies/java_developer?page=";

    public static void main(String[] args) throws IOException {
        for (int countPages = 1; countPages <= 5; countPages++) {
            String pageLink = String.format("%s%s%d", SOURCE_LINK, PAGE_LINK, countPages);
            Connection connection = Jsoup.connect(pageLink);

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
                String[] description = retrieveDescription(link).split("\\.\\s+");
                System.out.printf("%s %s %s%n%n", date.toString(), vacancyName, link);
                for (String s : description) {
                    System.out.println(s);
                }
                System.out.println();
            });
        }
    }

    private static String retrieveDescription(String link) {
        Connection connection = Jsoup.connect(link);
        String description = null;
        try {
           Document document = connection.get();
            Elements rows = document.select(".job_show_description__vacancy_description");
            for (Element row : rows) {
                Element titleElement = row.select(".style-ugc").first();
                description = titleElement.text();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return description;
    }
}


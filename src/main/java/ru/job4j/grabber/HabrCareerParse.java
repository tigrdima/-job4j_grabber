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
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = "/vacancies/java_developer?page=";
    private static final int COUNT_PAGES = 5;
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) {
        HabrCareerParse habrCareerParse = new HabrCareerParse(new HarbCareerDateTimeParser());
        habrCareerParse.list(PAGE_LINK).forEach(System.out::println);
    }

    @Override
    public List<Post> list(String link) {
        List<Post> posts = new ArrayList<>();

        for (int countPages = 1; countPages <= COUNT_PAGES; countPages++) {
            String pageLink = String.format("%s%s%d", SOURCE_LINK, link, countPages);
            Connection connection = Jsoup.connect(pageLink);
            try {
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                    posts.add(post(rows));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return posts;
    }

    private Post post(Elements rows) {
        Post post = null;
        for (Element row : rows) {
            Element titleElement = row.select(".vacancy-card__title").first();
            Element linkElement = titleElement.child(0);
            Element dateTitleElement = row.select(".vacancy-card__date").first();

            String linkTitle = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
            String description = retrieveDescription(linkTitle);
            String dateElement = dateTitleElement.child(0).attr("datetime");
            LocalDateTime dateTime = dateTimeParser.parse(dateElement);

            post = new Post(titleElement.text(), linkTitle, description, dateTime);
        }
           return post;
    }

    private static String retrieveDescription(String link) {
        String description = null;
        try {
            Connection connection = Jsoup.connect(link);
            Document document = connection.get();
            Element descriptionTitleElement = document.selectFirst(".style-ugc");
            description = descriptionTitleElement.text();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return description;
    }
}


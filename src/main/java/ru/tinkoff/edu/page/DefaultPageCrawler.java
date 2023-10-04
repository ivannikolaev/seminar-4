package ru.tinkoff.edu.page;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.function.Predicate;

public class DefaultPageCrawler implements PageCrawler {
    public Page crawl(String url) {
        try {
            var response = Jsoup.connect(url).execute();
            var content = response.body();
            var webDocument = response.parse();
            var relativeLinks = webDocument.select("a")
                    .stream()
                    .map(e -> e.attr("href"))
                    .filter(Predicate.not(String::isBlank))
                    .filter(link -> link.startsWith("/") && !link.startsWith("//"))
                    .toList();
            return new Page(url, content, relativeLinks);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

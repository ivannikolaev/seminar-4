package ru.tinkoff.edu;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import ru.tinkoff.edu.page.DefaultPageCrawler;
import ru.tinkoff.edu.page.Page;
import ru.tinkoff.edu.page.PageCrawler;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class MultiThreadedSiteCrawlerTest {

    @Test
    @DisplayName("Что если краулер отдельной страниц кидает ошибки?")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testCrawlError() {
        // given
        PageCrawler pageCrawler = url -> {
            throw new RuntimeException();
        };
        MultiThreadedSiteCrawler siteCrawler = new MultiThreadedSiteCrawler(pageCrawler,
                10,
                "https://en.wikipedia.org",
                "/wiki/Tinkoff_Bank",
                50);

        // when
        List<Page> pages = siteCrawler.crawl();

        // then
        log.info("crawled pages: {}", pages.size());
        assertEquals(0, pages.size());
    }

    @Test
    @DisplayName("Что если страниц < 50?")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testCrawlLessThan50Pages() {
        // given
        PageCrawler pageCrawler = url -> new Page("http://page.com", "Hello world!", List.of());
        MultiThreadedSiteCrawler siteCrawler = new MultiThreadedSiteCrawler(pageCrawler,
                10,
                "https://en.wikipedia.org",
                "/wiki/Tinkoff_Bank",
                50);

        // when
        List<Page> pages = siteCrawler.crawl();

        // then
        log.info("crawled pages: {}", pages.size());
        assertEquals(1, pages.size());
    }

    @RepeatedTest(value = 100)
    @DisplayName("Конкурентное изменение потоконебезопасной коллекции")
    void testCrawlPossibleConcurrentModificationException() {
        // given
        List<String> relativeUrls = IntStream.range(0, 1000)
                .mapToObj(String::valueOf)
                .toList();
        PageCrawler pageCrawler = url -> new Page("http://page.com", "Hello world!", relativeUrls);
        MultiThreadedSiteCrawler siteCrawler = new MultiThreadedSiteCrawler(pageCrawler,
                100,
                "https://en.wikipedia.org",
                "/wiki/Tinkoff_Bank",
                10000);

        // when
        List<Page> pages = siteCrawler.crawl();

        // then
        log.info("crawled pages: {}", pages.size());
        assertEquals(10000, pages.size());
    }

    @Test
    @DisplayName("Запуск на реальном сайте")
    void testCrawlRealResults() {
        // given
        MultiThreadedSiteCrawler siteCrawler = new MultiThreadedSiteCrawler(new DefaultPageCrawler(),
                10,
                "https://en.wikipedia.org",
                "/wiki/Tinkoff_Bank",
                150);

        // when
        List<Page> pages = siteCrawler.crawl();

        // then
        log.info("crawled pages: {}", pages.size());
    }

}
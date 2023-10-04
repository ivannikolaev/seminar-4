package ru.tinkoff.edu;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.tinkoff.edu.page.DefaultPageCrawler;
import ru.tinkoff.edu.page.Page;

import java.util.List;

@Slf4j
class SingleThreadedSiteCrawlerTest {

    @Test
    @DisplayName("Запускаем наш краулер на реальных данных")
    void crawlRealSite() {
        SingleThreadedSiteCrawler siteCrawler = new SingleThreadedSiteCrawler(new DefaultPageCrawler());
        List<Page> pages = siteCrawler.crawl(150, "https://en.wikipedia.org", "/wiki/Tinkoff_Bank");
        log.info("crawled page: {}", pages.size());
    }
}
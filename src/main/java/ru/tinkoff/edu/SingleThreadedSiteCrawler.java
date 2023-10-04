package ru.tinkoff.edu;

import ru.tinkoff.edu.page.Page;
import ru.tinkoff.edu.page.PageCrawler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class SingleThreadedSiteCrawler {

    private final PageCrawler pageCrawler;

    public SingleThreadedSiteCrawler(PageCrawler pageCrawler) {
        this.pageCrawler = pageCrawler;
    }

    public List<Page> crawl(int pageLimit, String baseUrl, String startingPage) {
        Queue<String> candidates = new ArrayDeque<>();
        List<Page> crawledPages = new ArrayList<>();
        candidates.add(baseUrl + startingPage);
        while (pageLimit-- > 0 && candidates.size() > 0) {
            var pageUrl = candidates.poll();
            if (pageUrl == null) {
                break;
            }
            Page page = pageCrawler.crawl(pageUrl);
            crawledPages.add(page);
            page.getRelativeLinks().forEach(url -> candidates.add(baseUrl + url));
        }
        return crawledPages;
    }
}

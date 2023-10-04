package ru.tinkoff.edu;

import lombok.extern.slf4j.Slf4j;
import ru.tinkoff.edu.page.Page;
import ru.tinkoff.edu.page.PageCrawler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Predicate;

@Slf4j
public class MultiThreadedSiteCrawler {

    private final BlockingDeque<Page> crawledPages;
    private final BlockingDeque<String> urls = new LinkedBlockingDeque<>();
    private final int pageLimit;
    private final PageCrawler pageCrawler;
    private final int concurrency;
    private final String baseUrl;
    private final String startingPage;
    private final ExecutorService executor;
    private final WorkerStatusMaintainer workerStatusMaintainer = new WorkerStatusMaintainer();

    public MultiThreadedSiteCrawler(PageCrawler pageCrawler,
                                    int concurrency,
                                    String baseUrl,
                                    String startingPage,
                                    int pageLimit) {
        this.pageCrawler = pageCrawler;
        this.executor = Executors.newFixedThreadPool(concurrency);
        this.concurrency = concurrency;
        this.baseUrl = baseUrl;
        this.startingPage = startingPage;
        this.pageLimit = pageLimit;
        this.crawledPages = new LinkedBlockingDeque<>(pageLimit);
    }

    public List<Page> crawl() {
        for (int i = 0; i < concurrency; i++) {
            startWorkerThread();
        }
        urls.add(baseUrl + startingPage);
        return awaitAndGetResults();
    }

    private void startWorkerThread() {
        executor.submit(() -> {
            var thread = Thread.currentThread();
            workerStatusMaintainer.registerWorker(thread);
            while (!executor.isShutdown()) {
                try {
                    var url = urls.poll(1, TimeUnit.SECONDS);
                    if (url == null) {
                        workerStatusMaintainer.markIdle(thread);
                        continue;
                    }
                    workerStatusMaintainer.markBusy(thread);
                    crawlPage(url);
                } catch (InterruptedException e) {
                    log.error("interrupted", e);
                }
            }
        });
    }

    private void crawlPage(String url) {
        try {
            Page page = pageCrawler.crawl(url);
            crawledPages.offer(page);
            page.getRelativeLinks().forEach(link -> urls.push(baseUrl + link));
        } catch (RuntimeException e) {
            log.error("failed to crawl page", e);
        }
    }

    public List<Page> awaitAndGetResults() {
        try {
            while (crawledPages.size() < pageLimit && !(workerStatusMaintainer.allIdle() && urls.isEmpty())) {
                TimeUnit.MILLISECONDS.sleep(100);
            }
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
            return crawledPages.stream().toList();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private class WorkerStatusMaintainer {

        private final Map<Thread, WorkerStatus> workerStatuses = new ConcurrentHashMap<>();

        void registerWorker(Thread worker) {
            workerStatuses.put(worker, WorkerStatus.BUSY);
        }

        void markBusy(Thread worker) {
            workerStatuses.put(worker, WorkerStatus.BUSY);
        }

        void markIdle(Thread worker) {
            workerStatuses.put(worker, WorkerStatus.IDLE);
        }

        boolean allIdle() {
            return workerStatuses.values().stream().allMatch(Predicate.isEqual(WorkerStatus.IDLE));
        }
    }

    private enum WorkerStatus {
        IDLE, BUSY
    }

}

package ru.tinkoff.edu.collections;

import org.junit.jupiter.api.RepeatedTest;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConcurrentLoadingCacheTest {


    @RepeatedTest(value = 10)
    void getConcurrently() throws ExecutionException, InterruptedException, TimeoutException {
        // given
        AtomicInteger invocationCount = new AtomicInteger();
        var cache = new ConcurrentLoadingCache<String, String>((key) -> {
            invocationCount.incrementAndGet();
            return key.toLowerCase();
        });
        var executor = Executors.newFixedThreadPool(10);
        // when
        CompletableFuture.allOf(IntStream.range(0, 10)
                .mapToObj(i -> CompletableFuture.runAsync(() -> cache.get("123")))
                .toArray(CompletableFuture[]::new)).get(1, TimeUnit.SECONDS);
        // then
        assertEquals(1, invocationCount.get());
    }
}
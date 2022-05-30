package com.multicurrency;

import org.example.multithreading.test1.OptimisticAuction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuctionTests {
    private int iterations = 1_000_000;
    private int poolSize = 8;
    private int bidCount = iterations * poolSize;

    private OptimisticAuction auction = new OptimisticAuction();
    private ExecutorService executor;
    private BlockingQueue<Long> priceQueue;
    private long expectedPrice;

    @BeforeEach
    public void setup() {
        executor = Executors.newFixedThreadPool(poolSize);
        priceQueue = new ArrayBlockingQueue<>(bidCount);
        for (long i = 0; i < bidCount / 3; i++) {
            // формируем очередь ставок
            priceQueue.offer(i - 1);
            priceQueue.offer(i);
            priceQueue.offer(i + 1);
        }
        expectedPrice = bidCount / 3;
    }

    @Test
    public void testCorrectLatestBid() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        long start = System.currentTimeMillis();
        for (int i = 0; i < poolSize; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                } catch (InterruptedException ignored) {
                }

                for (int it = 0; it < iterations; it++) {
                    long value = priceQueue.poll();
                    OptimisticAuction.Bid bid = new OptimisticAuction.Bid(value, value, value);
                    auction.propose(bid);
                    // эмулируем запросы на чтение
                    if (it % 200 == 0) {
                        auction.getLatestBid();
                    }
                }
            });
        }

        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        long end = System.currentTimeMillis();

        assertEquals(expectedPrice, auction.getLatestBid().getPrice());
        System.out.println("Rough execution time: " + (end - start));
    }
}

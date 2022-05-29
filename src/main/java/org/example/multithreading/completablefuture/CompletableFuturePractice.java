package org.example.multithreading.completablefuture;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class CompletableFuturePractice {
    public static class PriceRetriever {

        // узнаёт цену на товар в конкретном магазине. Может выполняться долго.
        public double getPrice(long itemId, long shopId) {
            // имитация долгого HTTP-запроса
            int delay = ThreadLocalRandom.current().nextInt(10);
            try {
                Thread.sleep(delay * 1000);
            } catch (InterruptedException e) {
            }

            return ThreadLocalRandom.current().nextDouble(1000);
        }
    }

    public static class PriceAggregator {

        private final PriceRetriever priceRetriever = new PriceRetriever();

        private final Set<Long> shopIds = Set.of(10L, 45L, 66L, 345L, 234L, 333L, 67L, 123L, 768L); // Список магазинов
        private final ExecutorService executorService = Executors.newFixedThreadPool(shopIds.size());

        // возвращает минимальную цену на товар среди всех магазинов
        // отправляется запрос getPrice в 10 магазинов. Кто-то из них ответит быстро, а кто-то не очень.
        //
        // Ответ от метода getMinPrice должен прийти меньше, чем за 3 секунды.
        // Минимальная цена выбирается из тех результатов, которые успели прийти за это время.
        public Double getMinPrice(long itemId) {
            long start = System.currentTimeMillis();
            List<CompletableFuture<Double>> futures = shopIds.stream()
                    .map(shopId -> CompletableFuture.supplyAsync(() -> priceRetriever.getPrice(itemId, shopId), executorService).completeOnTimeout(Double.MAX_VALUE, 2700, TimeUnit.MILLISECONDS)).collect(Collectors.toList());
            long end = System.currentTimeMillis();
            System.out.println(end - start);
            try {
                return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .thenApply(future -> futures.stream()
                                .mapToDouble(CompletableFuture::join)
                                .filter(price -> price != Double.MAX_VALUE)
                                .min()).get().orElse(Double.NaN);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            return Double.NaN;
        }
    }

    public static void main(String[] args) {
        PriceAggregator priceAggregator = new PriceAggregator();
        long itemId = 12L;

        long start = System.currentTimeMillis();
        Double min = priceAggregator.getMinPrice(itemId);
        priceAggregator.executorService.shutdownNow();
        long end = System.currentTimeMillis();

        System.out.println(min);
        System.out.println((end - start) < 3000); // should be true
    }
}

package org.example.multithreading;

import org.example.multithreading.concurrentcollections.RestaurantSearchService;
import org.example.multithreading.test1.OptimisticAuction;
import org.example.multithreading.test1.OptimisticStoppableAuction;
import org.example.multithreading.unmodifiedvars.Item;
import org.example.multithreading.unmodifiedvars.OrderService;
import org.example.multithreading.unmodifiedvars.PaymentInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class Appl {
    public static void main(String[] args) throws InterruptedException {
//        // test optimistic auction
//        final ExecutorService executorService = Executors.newFixedThreadPool(100);
//
//        OptimisticAuction auction = new OptimisticAuction();
//        final List<Long> participants = List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
//        final List<Long> prices = List.of(100L, 200L, 150L, 300L, 900L, 500L, 220L, 560L, 800L, 700L);
//        for (int i = 0; i < 10; i++) {
//            OptimisticAuction.Bid bid =  new OptimisticAuction.Bid((long) i, participants.get(i), prices.get(i));
//            executorService.execute(() -> auction.propose(bid));
//        }
//        executorService.execute(() -> auction.propose(new OptimisticAuction.Bid(1L, 3L, 550L)));
//        executorService.shutdownNow();
//
//        Thread.sleep(1000);
//        System.out.println("latest bid: " + auction.getLatestBid());

        // test optimistic stoppable auction
        final ExecutorService executorService = Executors.newFixedThreadPool(100);

        OptimisticStoppableAuction auction = new OptimisticStoppableAuction();
        final List<Long> participants = List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
        final List<Long> prices = List.of(100L, 200L, 150L, 300L, 900L, 500L, 220L, 560L, 800L, 700L);
        for (int i = 0; i < 10; i++) {
            OptimisticStoppableAuction.Bid bid =  new OptimisticStoppableAuction.Bid((long) i, participants.get(i), prices.get(i));
            executorService.execute(() -> auction.propose(bid));
            if (i == 7) auction.stopAuction();
        }
        executorService.execute(() -> auction.propose(new OptimisticStoppableAuction.Bid(1L, 3L, 550L)));
        executorService.shutdownNow();

        Thread.sleep(1000);
        System.out.println("latest bid: " + auction.getLatestBid());


//        RestaurantSearchService restaurantSearchService = new RestaurantSearchService();
//
//        restaurantSearchService.addToStat("Burger King");
//        restaurantSearchService.addToStat("KFC");
//        restaurantSearchService.addToStat("KFC");
//        restaurantSearchService.addToStat("MacDonald's");
//
//        restaurantSearchService.printStat().forEach(System.out::println);


//        OrderService orderService = new OrderService();
//
//        orderService.createOrder(List.of(new Item(), new Item()));
//
//        orderService.setPacked(1);
//        orderService.updatePaymentInfo(1, new PaymentInfo());

//        System.out.println(orderService.getCurrentOrders());


//        ThreadPoolExecutor executor = new ThreadPoolExecutor(8, 8, 1, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.DiscardPolicy());
//
//        for (int i = 0; i < 9; i++) {
//            final int k = i;
//            executor.execute(() -> System.out.println(k));
//        }
//
//        Thread.sleep(1000);
//        executor.shutdown();


    }
}

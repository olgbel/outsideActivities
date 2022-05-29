package org.example.multithreading.concurrentcollections;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RestaurantSearchService {
    private final ConcurrentHashMap<String, AtomicInteger> stat = new ConcurrentHashMap<>(); // накапливает статистику

    public Restaurant getByName(String restaurantName) {
        addToStat(restaurantName);
        return new Restaurant("t");
    }

    // обновляет статистику
    public void addToStat(String restaurantName) {
        stat.computeIfAbsent(restaurantName, count -> new AtomicInteger()).incrementAndGet();
//        stat.merge(restaurantName, new AtomicInteger(1), (key, value) -> new AtomicInteger(value.incrementAndGet()));
    }

    // отдаёт текущую информацию в виде строк
    //      Marcellis - 5
    //      Burger King - 7
    public Set<String> printStat() {
        return stat.entrySet().stream()
                .map(entry -> (entry.getKey() + " - " + entry.getValue()))
                .collect(Collectors.toSet());
    }
}

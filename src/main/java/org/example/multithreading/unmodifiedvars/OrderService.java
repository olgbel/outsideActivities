package org.example.multithreading.unmodifiedvars;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class OrderService {
    private final Map<Long, Order> currentOrders = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(0);

    public long createOrder(List<Item> items) {
        long id = nextId.addAndGet(1);
        Order order = new Order(id, items, null, false, null);
        currentOrders.put(id, order);
        return id;
    }

    public void updatePaymentInfo(long cartId, PaymentInfo paymentInfo) {
        Order order = currentOrders.computeIfPresent(cartId, (key, value) -> currentOrders.get(cartId).withPaymentInfo(paymentInfo));

        if (order != null && order.checkStatus()) {
            deliver(order);
        }
    }

    public void setPacked(long cartId) {
        Order order = currentOrders.computeIfPresent(cartId, (key, value) -> currentOrders.get(cartId).withIsPacked(true));

        if (order != null && order.checkStatus()) {
            deliver(order);
        }
    }

    private void deliver(Order order) {
        currentOrders.computeIfPresent(order.getId(), (key, value) -> order.withStatus(Status.DELIVERED));
    }
}

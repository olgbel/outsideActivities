package org.example.multithreading.test1;

import java.util.concurrent.atomic.AtomicReference;

/*
    Когда участник аукциона выставляет заявку, он вызывает метод propose
    Если цена в новой заявке ниже, чем latestBid, она отбрасывается, а метод propose возвращает false
    Если цена в новой заявке выше, чем latestBid, новая заявка становится latestBid.
    Владельцу предыдущей latestBid приходит уведомление, что его заявка устарела. Метод propose в случае обновления заявки возвращает true
    Метод sendOutdatedMessage выполняется 2 секунды
*/
public class OptimisticAuction {
    public static class Bid {
        // учебный пример без private модификаторов и get методов
        Long id; // ID заявки
        Long participantId; // ID участника
        Long price; // предложенная цена

        public Bid(Long id, Long participantId, Long price) {
            this.id = id;
            this.participantId = participantId;
            this.price = price;
        }

        @Override
        public String toString() {
            return "Bid{" +
                    "id=" + id +
                    ", participantId=" + participantId +
                    ", price=" + price +
                    '}';
        }
    }

    public static class Notifier {
        public void sendOutdatedMessage(Bid bid) { /*...*/ }
    }

    private final Notifier notifier = new Notifier();

    private AtomicReference<Bid> latestBid = new AtomicReference<>(new Bid(1L, 0L, 0L));

    public boolean propose(Bid bid) {
        Bid oldBid = latestBid.get();
        if (bid.price > latestBid.get().price && latestBid.compareAndSet(oldBid, bid)) {
            notifier.sendOutdatedMessage(oldBid);
            return true;
        }
        return false;
    }

    public Bid getLatestBid() {
        return latestBid.get();
    }
}
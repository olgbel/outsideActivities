package org.example.multithreading.test1;

import java.util.concurrent.atomic.AtomicMarkableReference;

/*
    Когда участник аукциона выставляет заявку, он вызывает метод propose
    Если цена в новой заявке ниже, чем latestBid, она отбрасывается, а метод propose возвращает false
    Если цена в новой заявке выше, чем latestBid, новая заявка становится latestBid.
    Владельцу предыдущей latestBid приходит уведомление, что его заявка устарела. Метод propose в случае обновления заявки возвращает true
    Метод sendOutdatedMessage выполняется 2 секунды
*/
public class OptimisticStoppableAuction {
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

        public Long getPrice() {
            return price;
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
        public void sendOutdatedMessage(Bid bid) {
            System.out.println("your bid " + bid + " is rejected");
        }
    }

    private final Notifier notifier = new Notifier();

    private final AtomicMarkableReference<Bid> latestBid = new AtomicMarkableReference<>(new Bid(1L, 0L, 0L), false);

    public boolean propose(Bid bid) {
        System.out.println(Thread.currentThread() + " propose " + bid);
        boolean success;
        Bid oldBid;
        boolean newBidSucceeded;

        do {
            if (latestBid.isMarked()) {
                System.out.println(bid + " is rejected because auction is stopped");
                return false;
            }
            oldBid = latestBid.getReference();

            if (oldBid.getPrice() < bid.getPrice()) {
                success = latestBid.compareAndSet(oldBid, bid, false, false);
                newBidSucceeded = true;
            } else {
                success = true;
                newBidSucceeded = false;
            }
        } while (!success);

        if (newBidSucceeded) {
            notifier.sendOutdatedMessage(oldBid);
        }
        return newBidSucceeded;
    }

    // останавливает аукцион. Заявки больше не принимаются
    public void stopAuction() {
        latestBid.set(latestBid.getReference(), true);
    }

    public Bid getLatestBid() {
        return latestBid.getReference();
    }
}

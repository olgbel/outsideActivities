package org.example.multithreading.test1;

/*
    Когда участник аукциона выставляет заявку, он вызывает метод propose
    Если цена в новой заявке ниже, чем latestBid, она отбрасывается, а метод propose возвращает false
    Если цена в новой заявке выше, чем latestBid, новая заявка становится latestBid.
    Владельцу предыдущей latestBid приходит уведомление, что его заявка устарела. Метод propose в случае обновления заявки возвращает true
    Метод sendOutdatedMessage выполняется 2 секунды
*/
public class PessimisticDoubleCheckAuction {
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

    private volatile Bid latestBid = new Bid(1L, 0L, 0L);
    private final Object object = new Object();

    public boolean propose(Bid bid) {
        if (bid.price > latestBid.price) {
            synchronized (object) {
                if (bid.price > latestBid.price) {
                    notifier.sendOutdatedMessage(latestBid);
                    latestBid = bid;
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    public Bid getLatestBid() {
        return latestBid;
    }
}

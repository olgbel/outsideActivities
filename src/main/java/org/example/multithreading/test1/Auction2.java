package org.example.multithreading.test1;

/*
    Когда участник аукциона выставляет заявку, он вызывает метод propose
    Если цена в новой заявке ниже, чем latestBid, она отбрасывается, а метод propose возвращает false
    Если цена в новой заявке выше, чем latestBid, новая заявка становится latestBid.
    Владельцу предыдущей latestBid приходит уведомление, что его заявка устарела. Метод propose в случае обновления заявки возвращает true
    Метод sendOutdatedMessage выполняется 2 секунды
*/
public class Auction2 {
    public static class Bid {
        // учебный пример без private модификаторов и get методов
        Long id; // ID заявки
        Long participantId; // ID участника
        Long price; // предложенная цена
    }

    public static class Notifier {
        public void sendOutdatedMessage(Bid bid) { /*...*/ }
    }

    private Notifier notifier = new Notifier();

    private volatile Bid latestBid; // возможно, тут что-то другое

    private volatile boolean isStopped;

    public synchronized boolean propose(Bid bid) {
        // ваш код
        if (isStopped) return false;

        if (bid.price > latestBid.price) {
            notifier.sendOutdatedMessage(latestBid);
            latestBid = bid;
            return true;
        }
        return false;
    }

    public Bid getLatestBid() {
        return latestBid;
    }

    // останавливает аукцион. Заявки больше не принимаются
    public void stopAuction() {
        // ваш код
        isStopped = true;
    }
}

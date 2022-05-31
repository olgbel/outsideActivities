package org.example.multithreading.test2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class MountTableRefresherService {

    private Others.RouterStore routerStore = new Others.RouterStore();
    private long cacheUpdateTimeout;

    /**
     * All router admin clients cached. So no need to create the client again and
     * again. Router admin address(host:port) is used as key to cache RouterClient
     * objects.
     */
    private Others.LoadingCache<String, Others.RouterClient> routerClientsCache;

    /**
     * Removes expired RouterClient from routerClientsCache.
     */
    private ScheduledExecutorService clientCacheCleanerScheduler;

    public void serviceInit() {
        long routerClientMaxLiveTime = 15L;
        this.cacheUpdateTimeout = 10L;
        routerClientsCache = new Others.LoadingCache<>();
        routerStore.getCachedRecords().stream().map(Others.RouterState::getAdminAddress)
                .forEach(addr -> routerClientsCache.add(addr, new Others.RouterClient()));

        initClientCacheCleaner(routerClientMaxLiveTime);
    }

    public void serviceStop() {
        clientCacheCleanerScheduler.shutdown();
        // remove and close all admin clients
        routerClientsCache.cleanUp();
    }

    private void initClientCacheCleaner(long routerClientMaxLiveTime) {
        ThreadFactory tf = r -> {
            Thread t = new Thread();
            t.setName("MountTableRefresh_ClientsCacheCleaner");
            t.setDaemon(true);
            return t;
        };

        clientCacheCleanerScheduler =
                Executors.newSingleThreadScheduledExecutor(tf);
        /*
         * When cleanUp() method is called, expired RouterClient will be removed and closed.
         */
        clientCacheCleanerScheduler.scheduleWithFixedDelay(
                () -> routerClientsCache.cleanUp(), routerClientMaxLiveTime,
                routerClientMaxLiveTime, TimeUnit.MILLISECONDS);
    }

    /**
     * Refresh mount table cache of this router as well as all other routers.
     */
    public void refresh() {
        List<Others.RouterState> cachedRecords = routerStore.getCachedRecords();
        List<MountTableRefresherThread> refreshThreads = new ArrayList<>();
        for (Others.RouterState routerState : cachedRecords) {
            String adminAddress = routerState.getAdminAddress();
            if (adminAddress == null || adminAddress.length() == 0) {
                // this router has not enabled router admin.
                continue;
            }
            if (isLocalAdmin(adminAddress)) {
                /*
                 * Local router's cache update does not require RPC call, so no need for
                 * RouterClient
                 */
                refreshThreads.add(getLocalRefresher(adminAddress));
            } else {
                refreshThreads.add(new MountTableRefresherThread(
                        new Others.MountTableManager(adminAddress), adminAddress));
            }
        }
        if (!refreshThreads.isEmpty()) {
            invokeRefresh(refreshThreads);
        }
    }

    protected MountTableRefresherThread getLocalRefresher(String adminAddress) {
        return new MountTableRefresherThread(new Others.MountTableManager("local"), adminAddress);
    }

    private void removeFromCache(String adminAddress) {
        routerClientsCache.invalidate(adminAddress);
    }

    private void invokeRefresh(List<MountTableRefresherThread> refreshThreads) {
        List<CompletableFuture<MountTableRefresherThread>> completableFutures = refreshThreads.stream()
                .map(refreshThread -> CompletableFuture.supplyAsync(() -> {
                    refreshThread.run();
                    return refreshThread;
                }))
                .collect(Collectors.toList());

        try {
            CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]))
                    .completeOnTimeout(null, cacheUpdateTimeout, TimeUnit.MILLISECONDS)
                    .thenApply(future -> completableFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()))
                    .thenAccept(this::logResult)
                    .get();
        } catch (InterruptedException e) {
            System.out.println("Mount table cache refresher was interrupted.");
        } catch (ExecutionException e) {
            System.out.println("Not all router admins updated their cache");
        }
    }

    private boolean isLocalAdmin(String adminAddress) {
        return adminAddress.contains("local");
    }

    private void logResult(List<MountTableRefresherThread> refreshThreads) {
        int successCount = 0;
        int failureCount = 0;
        for (MountTableRefresherThread mountTableRefreshThread : refreshThreads) {
            if (mountTableRefreshThread.isSuccess()) {
                successCount++;
            } else {
                failureCount++;
                // remove RouterClient from cache so that new client is created
                removeFromCache(mountTableRefreshThread.getAdminAddress());
            }
        }
        System.out.printf(
                "Mount table entries cache refresh successCount=%d,failureCount=%d%n",
                successCount, failureCount);
    }

    public static void main(String[] args) throws InterruptedException {
        MountTableRefresherService service = new MountTableRefresherService();
        service.serviceInit();

        service.refresh();

        Thread.sleep(2000);
        System.out.println("done");
        service.serviceStop();
    }
}

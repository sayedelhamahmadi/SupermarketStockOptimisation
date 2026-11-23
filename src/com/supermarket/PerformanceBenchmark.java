package com.supermarket;

import com.supermarket.model.DistributionCentre;
import com.supermarket.model.ReplenishmentRequest;
import com.supermarket.model.Store;
import com.supermarket.service.AdvancedAllocationService;
import com.supermarket.service.RequestManager;
import com.supermarket.service.StockAllocationService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PerformanceBenchmark {

    private static final int[] SIZES = {
            100, 500, 1000, 5000, 10000
    };

    public static void main(String[] args) {

        System.out.println("=== PERFORMANCE BENCHMARK ===");
        System.out.println(
                "Requests | Basic (ms) | Advanced (ms)"
        );

        for (int size : SIZES) {

            double basicTime = runBasic(size);
            double advancedTime = runAdvanced(size);

            System.out.printf(
                    "%8d | %10.3f | %13.3f%n",
                    size, basicTime, advancedTime
            );
        }
    }

    private static double runBasic(int size) {

        DistributionCentre centre =
                new DistributionCentre(
                        "DC001", "Warehouse", "London"
                );

        centre.addStock("P001", size * 50);

        Map<String, Store> stores = createStores(size);
        List<ReplenishmentRequest> requests =
                createRequests(size);

        RequestManager manager = new RequestManager();

        StockAllocationService service =
                new StockAllocationService();

        for (ReplenishmentRequest request : requests) {
            manager.addRequest(request);
        }

        long start = System.nanoTime();

        while (manager.hasPendingRequests()) {

            ReplenishmentRequest request =
                    manager.getNextRequest();

            service.allocateStock(
                    centre,
                    stores.get(request.getStoreId()),
                    request
            );
        }

        long end = System.nanoTime();

        return (end - start) / 1_000_000.0;
    }

    private static double runAdvanced(int size) {

        DistributionCentre centre =
                new DistributionCentre(
                        "DC001", "Warehouse", "London"
                );

        centre.addStock("P001", size * 50);

        Map<String, Store> stores = createStores(size);
        List<ReplenishmentRequest> requests =
                createRequests(size);

        AdvancedAllocationService service =
                new AdvancedAllocationService();

        long start = System.nanoTime();

        service.allocateStock(
                centre, stores, requests
        );

        long end = System.nanoTime();

        return (end - start) / 1_000_000.0;
    }

    private static Map<String, Store> createStores(int size) {

        Map<String, Store> stores = new HashMap<>();

        for (int i = 0; i < size; i++) {

            String id = "S" + i;

            stores.put(
                    id,
                    new Store(id, "Store " + i, "London")
            );
        }

        return stores;
    }

    private static List<ReplenishmentRequest> createRequests(
            int size) {

        List<ReplenishmentRequest> requests =
                new ArrayList<>();

        Random random = new Random(42);

        LocalDateTime deadline =
                LocalDateTime.now().plusDays(1);

        for (int i = 0; i < size; i++) {

            requests.add(
                    new ReplenishmentRequest(
                            "R" + i,
                            "S" + i,
                            "P001",
                            100,
                            random.nextInt(50),
                            random.nextInt(5) + 1,
                            random.nextInt(500),
                            deadline
                    )
            );
        }

        return requests;
    }
}

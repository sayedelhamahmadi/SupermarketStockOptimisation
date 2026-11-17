
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

public class Main {

    public static void main(String[] args) {

        System.out.println("=== BASIC ALLOCATION ===");
        runBasicAllocation();

        System.out.println();

        System.out.println("=== ADVANCED ALLOCATION ===");
        runAdvancedAllocation();
    }

    private static void runBasicAllocation() {

        DistributionCentre centre = new DistributionCentre(
                "DC001", "London Warehouse", "London"
        );
        centre.addStock("P001", 500);

        Map<String, Store> stores = createStores();
        List<ReplenishmentRequest> requests = createRequests();

        RequestManager manager = new RequestManager();
        StockAllocationService service =
                new StockAllocationService();

        for (ReplenishmentRequest request : requests) {
            manager.addRequest(request);
        }

        while (manager.hasPendingRequests()) {
            ReplenishmentRequest request =
                    manager.getNextRequest();

            Store store = stores.get(request.getStoreId());

            int allocated = service.allocateStock(
                    centre, store, request
            );

            System.out.println(
                    request.getRequestId()
                            + " | Allocated: " + allocated
                            + " | Remaining: " + request.getRemainingQuantity()
            );
        }

        System.out.println(
                "Warehouse remaining: " + centre.getStock("P001")
        );
    }

    private static void runAdvancedAllocation() {

        DistributionCentre centre = new DistributionCentre(
                "DC001", "London Warehouse", "London"
        );
        centre.addStock("P001", 500);

        Map<String, Store> stores = createStores();
        List<ReplenishmentRequest> requests = createRequests();

        AdvancedAllocationService service =
                new AdvancedAllocationService();

        Map<String, Integer> results =
                service.allocateStock(centre, stores, requests);

        for (ReplenishmentRequest request : requests) {
            System.out.println(
                    request.getRequestId()
                            + " | Allocated: "
                            + results.getOrDefault(request.getRequestId(), 0)
                            + " | Remaining: "
                            + request.getRemainingQuantity()
            );
        }

        System.out.println(
                "Warehouse remaining: " + centre.getStock("P001")
        );
    }

    private static Map<String, Store> createStores() {

        Map<String, Store> stores = new HashMap<>();

        stores.put("S001",
                new Store("S001", "Tesco Feltham", "Feltham"));

        stores.put("S002",
                new Store("S002", "Tesco Hounslow", "Hounslow"));

        stores.put("S003",
                new Store("S003", "Tesco Kingston", "Kingston"));

        return stores;
    }

    private static List<ReplenishmentRequest> createRequests() {

        List<ReplenishmentRequest> requests = new ArrayList<>();

        LocalDateTime deadline =
                LocalDateTime.now().plusDays(1);

        requests.add(new ReplenishmentRequest(
                "R001", "S001", "P001", 200, 10, 2, 150, deadline
        ));

        requests.add(new ReplenishmentRequest(
                "R002", "S002", "P001", 300, 5, 5, 250, deadline
        ));

        requests.add(new ReplenishmentRequest(
                "R003", "S003", "P001", 250, 20, 3, 180, deadline
        ));

        return requests;
    }
}

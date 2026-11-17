
package com.supermarket;

import com.supermarket.model.DistributionCentre;
import com.supermarket.model.Store;
import com.supermarket.model.ReplenishmentRequest;
import com.supermarket.service.RequestManager;
import com.supermarket.service.StockAllocationService;

import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {

        DistributionCentre warehouse =
                new DistributionCentre("DC001", "London Warehouse", "London");

        Store store1 = new Store("S001", "Tesco Feltham", "Feltham");
        Store store2 = new Store("S002", "Tesco Hounslow", "Hounslow");
        Store store3 = new Store("S003", "Tesco Kingston", "Kingston");

        warehouse.addStock("P001", 500);

        LocalDateTime deadline = LocalDateTime.now().plusDays(1);

        ReplenishmentRequest request1 = new ReplenishmentRequest(
                "R001", "S001", "P001", 200, 10, 2, 150, deadline
        );

        ReplenishmentRequest request2 = new ReplenishmentRequest(
                "R002", "S002", "P001", 300, 5, 5, 250, deadline
        );

        ReplenishmentRequest request3 = new ReplenishmentRequest(
                "R003", "S003", "P001", 250, 20, 3, 180, deadline
        );

        RequestManager manager = new RequestManager();
        StockAllocationService allocationService =
                new StockAllocationService();

        manager.addRequest(request1);
        manager.addRequest(request2);
        manager.addRequest(request3);

        System.out.println("SUPERMARKET STOCK ALLOCATION TEST");
        System.out.println("Initial warehouse stock: " + warehouse.getStock("P001"));

        while (manager.hasPendingRequests()) {

            ReplenishmentRequest request = manager.getNextRequest();

            Store store;

            if (request.getStoreId().equals("S001")) {
                store = store1;
            } else if (request.getStoreId().equals("S002")) {
                store = store2;
            } else {
                store = store3;
            }

            int allocated = allocationService.allocateStock(
                    warehouse, store, request
            );

            System.out.println(
                    request.getRequestId()
                            + " | Priority: " + request.getPriority()
                            + " | Allocated: " + allocated
                            + " | Remaining request: " + request.getRemainingQuantity()
            );
        }

        System.out.println("Final warehouse stock: " + warehouse.getStock("P001"));
        System.out.println("Feltham stock: " + store1.getStock("P001"));
        System.out.println("Hounslow stock: " + store2.getStock("P001"));
        System.out.println("Kingston stock: " + store3.getStock("P001"));
    }
}

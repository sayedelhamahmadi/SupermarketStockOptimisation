
package com.supermarket;

import com.supermarket.model.DistributionCentre;
import com.supermarket.model.ReplenishmentRequest;
import com.supermarket.model.Store;
import com.supermarket.service.AdvancedAllocationService;
import com.supermarket.service.RequestManager;
import com.supermarket.service.StockAllocationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AllocationTests {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {

        System.out.println("====================================");
        System.out.println(" SUPERMARKET ALGORITHM TEST SUITE");
        System.out.println("====================================");

        runTest("Full stock allocation",
                AllocationTests::testFullAllocation);

        runTest("Partial stock allocation",
                AllocationTests::testPartialAllocation);

        runTest("Warehouse stock conservation",
                AllocationTests::testStockConservation);

        runTest("No stock available",
                AllocationTests::testNoStock);

        runTest("Wrong store validation",
                AllocationTests::testWrongStore);

        runTest("Dynamic priority ordering",
                AllocationTests::testDynamicPriority);

        runTest("Advanced allocation conservation",
                AllocationTests::testAdvancedAllocation);

        System.out.println("\n====================================");
        System.out.println(" FINAL TEST RESULTS");
        System.out.println("====================================");

        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Total:  " + (passed + failed));

        if (failed == 0) {
            System.out.println("\nALL TESTS PASSED!");
        } else {
            throw new AssertionError(
                    failed + " test(s) failed"
            );
        }
    }

    private static ReplenishmentRequest createRequest(
            String id,
            String storeId,
            int quantity,
            int currentStock,
            int priority) {

        return new ReplenishmentRequest(
                id,
                storeId,
                "P001",
                quantity,
                currentStock,
                priority,
                100,
                LocalDateTime.now().plusDays(7)
        );
    }

    private static void testFullAllocation() {

        DistributionCentre centre = new DistributionCentre(
                "DC001", "Main Warehouse", "London"
        );

        Store store = new Store(
                "S001", "Feltham", "Feltham"
        );

        centre.addStock("P001", 100);

        ReplenishmentRequest request = createRequest(
                "R001", "S001", 60, 0, 3
        );

        StockAllocationService service =
                new StockAllocationService();

        int allocated = service.allocateStock(
                centre, store, request
        );

        check(allocated == 60,
                "Expected 60 allocated units");

        check(centre.getStock("P001") == 40,
                "Warehouse should have 40 units");

        check(store.getStock("P001") == 60,
                "Store should receive 60 units");

        check(request.isFulfilled(),
                "Request should be fulfilled");
    }

    private static void testPartialAllocation() {

        DistributionCentre centre = new DistributionCentre(
                "DC001", "Warehouse", "London"
        );

        Store store = new Store(
                "S001", "Store", "Feltham"
        );

        centre.addStock("P001", 30);

        ReplenishmentRequest request = createRequest(
                "R002", "S001", 100, 0, 3
        );

        int allocated = new StockAllocationService()
                .allocateStock(centre, store, request);

        check(allocated == 30,
                "Only 30 units should be allocated");

        check(request.getRemainingQuantity() == 70,
                "70 units should remain requested");

        check(centre.getStock("P001") == 0,
                "Warehouse should be empty");

        check(!request.isFulfilled(),
                "Request should remain partially fulfilled");
    }

    private static void testStockConservation() {

        DistributionCentre centre = new DistributionCentre(
                "DC001", "Warehouse", "London"
        );

        Store store = new Store(
                "S001", "Store", "Feltham"
        );

        centre.addStock("P001", 200);
        store.addStock("P001", 20);

        int initialTotal =
                centre.getStock("P001")
                        + store.getStock("P001");

        ReplenishmentRequest request = createRequest(
                "R003", "S001", 100, 20, 3
        );

        new StockAllocationService().allocateStock(
                centre, store, request
        );

        int finalTotal =
                centre.getStock("P001")
                        + store.getStock("P001");

        check(initialTotal == finalTotal,
                "Stock must not be created or lost");
    }

    private static void testNoStock() {

        DistributionCentre centre = new DistributionCentre(
                "DC001", "Warehouse", "London"
        );

        Store store = new Store(
                "S001", "Store", "Feltham"
        );

        ReplenishmentRequest request = createRequest(
                "R004", "S001", 50, 0, 3
        );

        int allocated = new StockAllocationService()
                .allocateStock(centre, store, request);

        check(allocated == 0,
                "No stock should be allocated");

        check(centre.getStock("P001") == 0,
                "Warehouse stock must remain zero");

        check(store.getStock("P001") == 0,
                "Store stock must remain zero");

        check(request.getRemainingQuantity() == 50,
                "Request quantity must remain unchanged");
    }

    private static void testWrongStore() {

        DistributionCentre centre = new DistributionCentre(
                "DC001", "Warehouse", "London"
        );

        Store store = new Store(
                "S002", "Wrong Store", "Hounslow"
        );

        centre.addStock("P001", 100);

        ReplenishmentRequest request = createRequest(
                "R005", "S001", 50, 0, 3
        );

        boolean rejected = false;

        try {
            new StockAllocationService().allocateStock(
                    centre, store, request
            );
        } catch (IllegalArgumentException e) {
            rejected = true;
        }

        check(rejected,
                "Allocation to the wrong store must be rejected");

        check(centre.getStock("P001") == 100,
                "Warehouse stock must remain unchanged");
    }

    private static void testDynamicPriority() {

        RequestManager manager = new RequestManager();

        ReplenishmentRequest low = createRequest(
                "R006", "S001", 50, 20, 1
        );

        ReplenishmentRequest high = createRequest(
                "R007", "S002", 50, 20, 5
        );

        manager.addRequest(low);
        manager.addRequest(high);

        check(manager.peekNextRequest() == high,
                "Higher-priority request should be first");

        manager.updatePriority("R006", 5);
        manager.updatePriority("R007", 1);

        check(manager.peekNextRequest() == low,
                "Priority update should reorder the queue");
    }

    private static void testAdvancedAllocation() {

        DistributionCentre centre = new DistributionCentre(
                "DC001", "Warehouse", "London"
        );

        Store store1 = new Store(
                "S001", "Feltham", "Feltham"
        );

        Store store2 = new Store(
                "S002", "Hounslow", "Hounslow"
        );

        centre.addStock("P001", 100);

        ReplenishmentRequest r1 = createRequest(
                "R008", "S001", 80, 0, 3
        );

        ReplenishmentRequest r2 = createRequest(
                "R009", "S002", 80, 0, 4
        );

        AdvancedAllocationService service =
                new AdvancedAllocationService();

        Map<String, Integer> results = service.allocateStock(
                centre,
                Map.of(
                        "S001", store1,
                        "S002", store2
                ),
                List.of(r1, r2)
        );

        int allocated = results.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();

        check(allocated == 100,
                "Advanced allocation should use 100 units");

        check(centre.getStock("P001") == 0,
                "Warehouse should have zero remaining stock");

        check(store1.getStock("P001")
                        + store2.getStock("P001") == 100,
                "Stores should receive exactly 100 units");

        check(r1.getRemainingQuantity()
                        + r2.getRemainingQuantity() == 60,
                "60 units should remain unfulfilled");
    }

    private static void check(
            boolean condition, String message) {

        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void runTest(
            String name, Runnable test) {

        try {
            test.run();
            passed++;
            System.out.println("[PASS] " + name);
        } catch (Throwable error) {
            failed++;
            System.out.println("[FAIL] " + name);
            System.out.println("       " + error.getMessage());
        }
    }
}
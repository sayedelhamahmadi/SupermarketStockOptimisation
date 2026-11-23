
package com.supermarket;

import com.supermarket.analysis.DemandForecastService;
import com.supermarket.model.DistributionCentre;
import com.supermarket.model.Product;
import com.supermarket.model.ReplenishmentRequest;
import com.supermarket.model.Store;
import com.supermarket.service.AdvancedAllocationService;
import com.supermarket.service.RequestManager;
import com.supermarket.service.StockAllocationService;
import com.supermarket.service.SupermarketSystem;

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

        runTest(
                "Full stock allocation",
                AllocationTests::testFullAllocation
        );

        runTest(
                "Partial stock allocation",
                AllocationTests::testPartialAllocation
        );

        runTest(
                "Warehouse stock conservation",
                AllocationTests::testStockConservation
        );

        runTest(
                "No stock available",
                AllocationTests::testNoStock
        );

        runTest(
                "Wrong store validation",
                AllocationTests::testWrongStore
        );

        runTest(
                "Dynamic priority ordering",
                AllocationTests::testDynamicPriority
        );

        runTest(
                "Advanced allocation conservation",
                AllocationTests::testAdvancedAllocation
        );

        runTest(
                "Store integer overflow protection",
                AllocationTests::testStoreOverflow
        );

        runTest(
                "Basic allocation rollback on overflow",
                AllocationTests::testBasicOverflowProtection
        );

        runTest(
                "Advanced allocation rollback on overflow",
                AllocationTests::testAdvancedOverflowProtection
        );

        runTest(
                "Weekly demand pattern detection",
                AllocationTests::testWeeklyDemandPattern
        );

        runTest(
                "Historical demand affects allocation",
                AllocationTests::testHistoricalDemandAllocation
        );

        System.out.println();
        System.out.println("====================================");
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

    // Reusable request for allocation testing.
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

    // ========================================
    // TEST 1: FULL STOCK ALLOCATION
    // ========================================

    private static void testFullAllocation() {

        DistributionCentre centre =
                new DistributionCentre(
                        "DC001", "Warehouse", "London"
                );

        Store store = new Store(
                "S001", "Feltham", "Feltham"
        );

        centre.addStock("P001", 100);

        ReplenishmentRequest request = createRequest(
                "R001", "S001", 60, 0, 3
        );

        int allocated = new StockAllocationService()
                .allocateStock(centre, store, request);

        check(allocated == 60,
                "Expected 60 allocated units");

        check(centre.getStock("P001") == 40,
                "Warehouse should have 40 units");

        check(store.getStock("P001") == 60,
                "Store should receive 60 units");

        check(request.isFulfilled(),
                "Request should be fulfilled");
    }

    // ========================================
    // TEST 2: PARTIAL STOCK ALLOCATION
    // ========================================

    private static void testPartialAllocation() {

        DistributionCentre centre =
                new DistributionCentre(
                        "DC001", "Warehouse", "London"
                );

        Store store = new Store(
                "S001", "Feltham", "Feltham"
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

        check(store.getStock("P001") == 30,
                "Store should receive 30 units");

        check(!request.isFulfilled(),
                "Request must remain partially fulfilled");
    }

    // ========================================
    // TEST 3: STOCK CONSERVATION
    // ========================================

    private static void testStockConservation() {

        DistributionCentre centre =
                new DistributionCentre(
                        "DC001", "Warehouse", "London"
                );

        Store store = new Store(
                "S001", "Feltham", "Feltham"
        );

        centre.addStock("P001", 200);
        store.addStock("P001", 20);

        long initialTotal =
                (long) centre.getStock("P001")
                        + store.getStock("P001");

        ReplenishmentRequest request = createRequest(
                "R003", "S001", 100, 20, 3
        );

        new StockAllocationService().allocateStock(
                centre, store, request
        );

        long finalTotal =
                (long) centre.getStock("P001")
                        + store.getStock("P001");

        check(initialTotal == finalTotal,
                "Stock must not be created or lost");

        check(request.isFulfilled(),
                "Request should be completed");
    }

    // ========================================
    // TEST 4: NO AVAILABLE STOCK
    // ========================================

    private static void testNoStock() {

        DistributionCentre centre =
                new DistributionCentre(
                        "DC001", "Warehouse", "London"
                );

        Store store = new Store(
                "S001", "Feltham", "Feltham"
        );

        ReplenishmentRequest request = createRequest(
                "R004", "S001", 50, 0, 3
        );

        int allocated = new StockAllocationService()
                .allocateStock(centre, store, request);

        check(allocated == 0,
                "No stock should be allocated");

        check(centre.getStock("P001") == 0,
                "Warehouse must remain empty");

        check(store.getStock("P001") == 0,
                "Store must remain empty");

        check(request.getRemainingQuantity() == 50,
                "Request quantity must not change");
    }

    // ========================================
    // TEST 5: WRONG STORE VALIDATION
    // ========================================

    private static void testWrongStore() {

        DistributionCentre centre =
                new DistributionCentre(
                        "DC001", "Warehouse", "London"
                );

        Store wrongStore = new Store(
                "S002", "Hounslow", "Hounslow"
        );

        centre.addStock("P001", 100);

        ReplenishmentRequest request = createRequest(
                "R005", "S001", 50, 0, 3
        );

        boolean rejected = false;

        try {
            new StockAllocationService().allocateStock(
                    centre, wrongStore, request
            );
        } catch (IllegalArgumentException exception) {
            rejected = true;
        }

        check(rejected,
                "Wrong store must be rejected");

        check(centre.getStock("P001") == 100,
                "Warehouse must remain unchanged");

        check(wrongStore.getStock("P001") == 0,
                "Wrong store must not receive stock");
    }

    // ========================================
    // TEST 6: DYNAMIC PRIORITY ORDERING
    // ========================================

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
                "High-priority request must be first");

        manager.updatePriority("R006", 5);
        manager.updatePriority("R007", 1);

        check(manager.peekNextRequest() == low,
                "Priority update must reorder requests");

        check(manager.getPendingRequestCount() == 2,
                "Two requests should remain pending");
    }

    // ========================================
    // TEST 7: ADVANCED ALLOCATION
    // ========================================

    private static void testAdvancedAllocation() {

        DistributionCentre centre =
                new DistributionCentre(
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

        Map<String, Integer> results =
                new AdvancedAllocationService().allocateStock(
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
                "Advanced allocation must use 100 units");

        check(centre.getStock("P001") == 0,
                "Warehouse must be empty");

        check(
                store1.getStock("P001")
                        + store2.getStock("P001") == 100,
                "Stores must receive exactly 100 units"
        );

        check(
                r1.getRemainingQuantity()
                        + r2.getRemainingQuantity() == 60,
                "60 units must remain unfulfilled"
        );
    }

    // ========================================
    // TEST 8: STORE INTEGER OVERFLOW
    // ========================================

    private static void testStoreOverflow() {

        Store store = new Store(
                "S001", "Overflow Store", "London"
        );

        int initialStock = Integer.MAX_VALUE - 5;

        store.addStock("P001", initialStock);

        boolean rejected = false;

        try {
            store.addStock("P001", 10);
        } catch (ArithmeticException exception) {
            rejected = true;
        }

        check(rejected,
                "Store must reject integer overflow");

        check(store.getStock("P001") == initialStock,
                "Original stock must remain unchanged");

        check(store.getStock("P001") >= 0,
                "Stock cannot become negative");
    }

    // ========================================
    // TEST 9: BASIC ALLOCATION OVERFLOW
    // ========================================

    private static void testBasicOverflowProtection() {

        DistributionCentre centre =
                new DistributionCentre(
                        "DC001", "Warehouse", "London"
                );

        Store store = new Store(
                "S001", "Feltham", "Feltham"
        );

        centre.addStock("P001", 10);

        int originalStock = Integer.MAX_VALUE - 5;

        store.addStock("P001", originalStock);

        ReplenishmentRequest request = createRequest(
                "R010", "S001", 10, originalStock, 3
        );

        boolean rejected = false;

        try {
            new StockAllocationService().allocateStock(
                    centre, store, request
            );
        } catch (ArithmeticException exception) {
            rejected = true;
        }

        check(rejected,
                "Basic allocation must reject overflow");

        check(centre.getStock("P001") == 10,
                "Warehouse must remain unchanged");

        check(store.getStock("P001") == originalStock,
                "Store must remain unchanged");

        check(request.getRemainingQuantity() == 10,
                "Request must remain unchanged");
    }

    // ========================================
    // TEST 10: ADVANCED ALLOCATION OVERFLOW
    // ========================================

    private static void testAdvancedOverflowProtection() {

        DistributionCentre centre =
                new DistributionCentre(
                        "DC001", "Warehouse", "London"
                );

        Store store = new Store(
                "S001", "Feltham", "Feltham"
        );

        centre.addStock("P001", 10);

        int originalStock = Integer.MAX_VALUE - 5;

        store.addStock("P001", originalStock);

        ReplenishmentRequest request = createRequest(
                "R011", "S001", 10, originalStock, 5
        );

        boolean rejected = false;

        try {
            new AdvancedAllocationService().allocateStock(
                    centre,
                    Map.of("S001", store),
                    List.of(request)
            );
        } catch (ArithmeticException exception) {
            rejected = true;
        }

        check(rejected,
                "Advanced allocation must reject overflow");

        check(centre.getStock("P001") == 10,
                "Warehouse must remain unchanged");

        check(store.getStock("P001") == originalStock,
                "Store must remain unchanged");

        check(request.getRemainingQuantity() == 10,
                "Request must remain unchanged");
    }

    // ========================================
    // TEST 11: WEEKLY DEMAND PATTERN
    // ========================================

    private static void testWeeklyDemandPattern() {

        DemandForecastService forecast =
                new DemandForecastService();

        String storeId = "S001";
        String productCode = "P001";

        // Two weeks of simulated daily sales.
        // Day index 2 has high demand in both weeks.
        int[] dailySales = {
                10, 10, 80, 10, 10, 10, 10,
                10, 10, 80, 10, 10, 10, 10
        };

        for (int quantity : dailySales) {
            forecast.recordDailySales(
                    storeId, productCode, quantity
            );
        }

        check(
                forecast.getHistoryLength(
                        storeId, productCode
                ) == 14,
                "Expected 14 recorded days"
        );

        check(
                forecast.hasWeeklyPattern(
                        storeId, productCode
                ),
                "A repeated high-demand day should be detected"
        );

        double[] averages = forecast.getWeeklyAverages(
                storeId, productCode
        );

        check(averages.length == 7,
                "Seven daily-cycle averages are expected");

        check(Math.abs(averages[2] - 80.0) < 0.001,
                "High-demand day should average 80");

        check(Math.abs(averages[0] - 10.0) < 0.001,
                "Normal-demand day should average 10");

        // Constant sales should not produce a high-day pattern.
        DemandForecastService stable =
                new DemandForecastService();

        for (int day = 0; day < 14; day++) {
            stable.recordDailySales(
                    storeId, productCode, 10
            );
        }

        check(
                !stable.hasWeeklyPattern(
                        storeId, productCode
                ),
                "Constant demand should not show repeating spikes"
        );

        // One week is insufficient to establish repetition.
        DemandForecastService insufficient =
                new DemandForecastService();

        for (int day = 0; day < 7; day++) {
            insufficient.recordDailySales(
                    storeId, productCode,
                    day == 2 ? 80 : 10
            );
        }

        check(
                !insufficient.hasWeeklyPattern(
                        storeId, productCode
                ),
                "One week is insufficient for this detector"
        );
    }

    // ========================================
    // TEST 12: HISTORICAL DEMAND IN ALLOCATION
    // ========================================

    private static void testHistoricalDemandAllocation() {

        // Scenario A: No historical sales.
        // Both requests have identical initial parameters.
        SupermarketSystem baseline = createDemandScenario();

        Map<String, Integer> baselineResults =
                baseline.processAdvancedRequests("DC001");

        int baselineS002 =
                baselineResults.getOrDefault("R002", 0);

        // Scenario B: Same starting conditions,
        // but S002 has recorded high historical demand.
        SupermarketSystem withHistory =
                createDemandScenario();

        for (int day = 0; day < 7; day++) {
            withHistory.recordDailySales(
                    "S002", "P001", 100
            );
        }

        Map<String, Integer> historyResults =
                withHistory.processAdvancedRequests("DC001");

        int historyS002 =
                historyResults.getOrDefault("R002", 0);

        int baselineTotal = baselineResults.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();

        int historyTotal = historyResults.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();

        check(baselineTotal == 100,
                "Baseline must allocate exactly 100 units");

        check(historyTotal == 100,
                "Historical scenario must allocate exactly 100 units");

        // High historical sales should increase S002's
        // allocation relative to the baseline scenario.
        check(
                historyS002 > baselineS002,
                "Historical demand should increase "
                        + "S002's allocated quantity"
        );

        check(
                withHistory.findCentre("DC001")
                        .getStock("P001") == 0,
                "Historical allocation must conserve stock"
        );

        long storeTotal =
                (long) withHistory.findStore("S001")
                        .getStock("P001")
                        + withHistory.findStore("S002")
                        .getStock("P001");

        check(storeTotal == 100,
                "Stores must receive exactly 100 units");

        System.out.println(
                "       S002 allocation without history: "
                        + baselineS002
        );

        System.out.println(
                "       S002 allocation with history: "
                        + historyS002
        );
    }

    // Create an identical two-store scenario.
    private static SupermarketSystem createDemandScenario() {

        SupermarketSystem system =
                new SupermarketSystem();

        system.addProduct(new Product(
                "P001", "Milk", "Dairy", "Supplier"
        ));

        system.addStore(new Store(
                "S001", "Feltham", "Feltham"
        ));

        system.addStore(new Store(
                "S002", "Hounslow", "Hounslow"
        ));

        system.addDistributionCentre(
                new DistributionCentre(
                        "DC001", "Warehouse", "London"
                )
        );

        system.receiveDelivery(
                "DC001", "P001", 100
        );

        // Same demand, priority, stock and deadline.
        LocalDateTime deadline =
                LocalDateTime.now().plusDays(7);

        ReplenishmentRequest first =
                new ReplenishmentRequest(
                        "R001", "S001", "P001",
                        100, 20, 3, 100, deadline
                );

        ReplenishmentRequest second =
                new ReplenishmentRequest(
                        "R002", "S002", "P001",
                        100, 20, 3, 100, deadline
                );

        system.submitRequest(first);
        system.submitRequest(second);

        return system;
    }

    // ========================================
    // TEST HELPERS
    // ========================================

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
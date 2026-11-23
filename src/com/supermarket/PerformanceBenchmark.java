
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

    private static final int WARMUP_RUNS = 3;
    private static final int MEASURED_RUNS = 5;
    private static final int STOCK_PER_REQUEST = 50;
    private static final String PRODUCT_CODE = "P001";

    public static void main(String[] args) {

        System.out.println(
                "==============================================="
        );
        System.out.println(
                " SUPERMARKET PERFORMANCE BENCHMARK"
        );
        System.out.println(
                "==============================================="
        );

        System.out.println("Java version: "
                + System.getProperty("java.version"));

        System.out.println(
                "Warm-up runs: " + WARMUP_RUNS
        );

        System.out.println(
                "Measured runs: " + MEASURED_RUNS
        );

        System.out.println(
                "Each run uses fresh, identical test data."
        );

        System.out.println(
                "Timing includes request scheduling and allocation."
        );

        System.out.println();

        System.out.printf(
                "%10s | %14s | %14s | %12s%n",
                "Requests",
                "Basic Avg(ms)",
                "Advanced Avg",
                "Faster"
        );

        System.out.println(
                "------------------------------------------------------------"
        );

        for (int size : SIZES) {

            // Warm up the JVM to reduce startup effects.
            for (int i = 0; i < WARMUP_RUNS; i++) {
                runBasic(size);
                runAdvanced(size);
            }

            double[] basicTimes =
                    new double[MEASURED_RUNS];

            double[] advancedTimes =
                    new double[MEASURED_RUNS];

            // Alternate execution order to reduce ordering bias.
            for (int i = 0; i < MEASURED_RUNS; i++) {

                if (i % 2 == 0) {
                    basicTimes[i] = runBasic(size);
                    advancedTimes[i] = runAdvanced(size);
                } else {
                    advancedTimes[i] = runAdvanced(size);
                    basicTimes[i] = runBasic(size);
                }
            }

            double basicAverage = average(basicTimes);
            double advancedAverage = average(advancedTimes);

            String faster;

            if (basicAverage < advancedAverage) {
                faster = "Basic";
            } else if (advancedAverage < basicAverage) {
                faster = "Advanced";
            } else {
                faster = "Equal";
            }

            System.out.printf(
                    "%10d | %14.3f | %14.3f | %12s%n",
                    size,
                    basicAverage,
                    advancedAverage,
                    faster
            );

            System.out.printf(
                    "           Standard deviation: "
                            + "Basic %.3f ms | Advanced %.3f ms%n",
                    standardDeviation(basicTimes),
                    standardDeviation(advancedTimes)
            );
        }

        System.out.println();
        System.out.println(
                "Benchmark completed successfully."
        );

        System.out.println(
                "Note: Times vary with JVM and system load."
        );
    }

    // Measure queue construction and basic allocation.
    private static double runBasic(int size) {

        BenchmarkData data = createData(size);

        RequestManager manager = new RequestManager();

        StockAllocationService service =
                new StockAllocationService();

        long start = System.nanoTime();

        // Include the cost of inserting requests.
        for (ReplenishmentRequest request : data.requests) {
            manager.addRequest(request);
        }

        // Refresh priorities once before allocation.
        manager.reprioritiseRequests();

        long totalAllocated = 0;

        while (manager.hasPendingRequests()) {

            ReplenishmentRequest request =
                    manager.getNextRequest();

            int allocated = service.allocateStock(
                    data.centre,
                    data.stores.get(request.getStoreId()),
                    request
            );

            totalAllocated += allocated;
        }

        long end = System.nanoTime();

        verifyAllocation(
                data.centre, size, totalAllocated
        );

        return (end - start) / 1_000_000.0;
    }

    // Measure advanced grouping, scoring, sorting and allocation.
    private static double runAdvanced(int size) {

        BenchmarkData data = createData(size);

        AdvancedAllocationService service =
                new AdvancedAllocationService();

        long start = System.nanoTime();

        Map<String, Integer> results =
                service.allocateStock(
                        data.centre,
                        data.stores,
                        data.requests
                );

        long end = System.nanoTime();

        long totalAllocated = 0;

        for (int quantity : results.values()) {
            totalAllocated += quantity;
        }

        verifyAllocation(
                data.centre, size, totalAllocated
        );

        return (end - start) / 1_000_000.0;
    }

    // Ensure both algorithms actually process the expected stock.
    private static void verifyAllocation(
            DistributionCentre centre,
            int size,
            long totalAllocated) {

        long expected = (long) size * STOCK_PER_REQUEST;

        if (totalAllocated != expected) {
            throw new IllegalStateException(
                    "Incorrect allocation: expected "
                            + expected + ", got "
                            + totalAllocated
            );
        }

        if (centre.getStock(PRODUCT_CODE) != 0) {
            throw new IllegalStateException(
                    "Warehouse stock should be zero"
            );
        }
    }

    // Create identical datasets for both algorithms.
    private static BenchmarkData createData(int size) {

        DistributionCentre centre =
                new DistributionCentre(
                        "DC001",
                        "Main Warehouse",
                        "London"
                );

        centre.addStock(
                PRODUCT_CODE,
                size * STOCK_PER_REQUEST
        );

        Map<String, Store> stores = new HashMap<>();

        List<ReplenishmentRequest> requests =
                new ArrayList<>(size);

        Random random = new Random(42);

        LocalDateTime deadline =
                LocalDateTime.of(2027, 1, 1, 12, 0);

        for (int i = 0; i < size; i++) {

            String storeId = "S" + i;
            String requestId = "R" + i;

            Store store = new Store(
                    storeId,
                    "Store " + i,
                    "London"
            );

            stores.put(storeId, store);

            ReplenishmentRequest request =
                    new ReplenishmentRequest(
                            requestId,
                            storeId,
                            PRODUCT_CODE,
                            100,
                            random.nextInt(50),
                            random.nextInt(5) + 1,
                            random.nextInt(500),
                            deadline
                    );

            requests.add(request);
        }

        return new BenchmarkData(
                centre,
                stores,
                requests
        );
    }

    // Calculate mean execution time.
    private static double average(double[] values) {

        double total = 0;

        for (double value : values) {
            total += value;
        }

        return total / values.length;
    }

    // Calculate variation between benchmark runs.
    private static double standardDeviation(double[] values) {

        double mean = average(values);
        double sum = 0;

        for (double value : values) {
            double difference = value - mean;
            sum += difference * difference;
        }

        return Math.sqrt(sum / values.length);
    }

    // Container for a fresh benchmark scenario.
    private static class BenchmarkData {

        final DistributionCentre centre;
        final Map<String, Store> stores;
        final List<ReplenishmentRequest> requests;

        BenchmarkData(
                DistributionCentre centre,
                Map<String, Store> stores,
                List<ReplenishmentRequest> requests) {

            this.centre = centre;
            this.stores = stores;
            this.requests = requests;
        }
    }
}
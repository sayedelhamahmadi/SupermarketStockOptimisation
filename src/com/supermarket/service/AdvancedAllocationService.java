
package com.supermarket.service;

import com.supermarket.model.DistributionCentre;
import com.supermarket.model.ReplenishmentRequest;
import com.supermarket.model.Store;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdvancedAllocationService {

    public Map<String, Integer> allocateStock(
            DistributionCentre centre,
            Map<String, Store> stores,
            List<ReplenishmentRequest> requests) {

        return allocateStock(
                centre,
                stores,
                requests,
                Map.of()
        );
    }

    public Map<String, Integer> allocateStock(
            DistributionCentre centre,
            Map<String, Store> stores,
            List<ReplenishmentRequest> requests,
            Map<String, Integer> effectivePriorities) {

        if (centre == null || stores == null
                || requests == null || effectivePriorities == null) {

            throw new IllegalArgumentException(
                    "Allocation inputs cannot be null"
            );
        }

        Map<String, Integer> results = new HashMap<>();

        Map<String, List<ReplenishmentRequest>> groups =
                new HashMap<>();

        // Validate requests and group them by product.
        for (ReplenishmentRequest request : requests) {

            if (request == null) {
                throw new IllegalArgumentException(
                        "Request cannot be null"
                );
            }

            if (request.isFulfilled()) {
                continue;
            }

            Store store = stores.get(request.getStoreId());

            if (store == null) {
                throw new IllegalArgumentException(
                        "Unknown store: " + request.getStoreId()
                );
            }

            groups.computeIfAbsent(
                    request.getProductCode(),
                    key -> new ArrayList<>()
            ).add(request);
        }

        LocalDateTime evaluationTime = LocalDateTime.now();

        // Process each product independently.
        for (Map.Entry<String, List<ReplenishmentRequest>> entry
                : groups.entrySet()) {

            String productCode = entry.getKey();

            List<ReplenishmentRequest> group = entry.getValue();

            int initialStock = centre.getStock(productCode);

            if (initialStock <= 0) {
                continue;
            }

            Map<ReplenishmentRequest, Double> scores =
                    new HashMap<>();

            double totalScore = 0;

            // Calculate weighted urgency for each request.
            for (ReplenishmentRequest request : group) {

                int priority = effectivePriorities.getOrDefault(
                        request.getRequestId(),
                        request.getPriority()
                );

                double score = calculateScore(
                        request,
                        priority,
                        evaluationTime
                );

                scores.put(request, score);
                totalScore += score;
            }

            // Higher scores are processed first.
            group.sort(
                    Comparator.comparingDouble(
                                    (ReplenishmentRequest request) ->
                                            scores.get(request)
                            ).reversed()
                            .thenComparing(
                                    ReplenishmentRequest::getDeadline
                            )
                            .thenComparing(
                                    ReplenishmentRequest::getRequestId
                            )
            );

            // FIRST PASS: proportional allocation.
            for (ReplenishmentRequest request : group) {

                int available = centre.getStock(productCode);

                if (available <= 0) {
                    break;
                }

                double weight = scores.get(request) / totalScore;

                int proportionalShare = (int) Math.floor(
                        initialStock * weight
                );

                int quantity = Math.min(
                        proportionalShare,
                        Math.min(
                                available,
                                request.getRemainingQuantity()
                        )
                );

                if (quantity > 0) {
                    allocate(
                            centre,
                            stores.get(request.getStoreId()),
                            request,
                            quantity,
                            results
                    );
                }
            }

            // SECOND PASS: redistribute remaining warehouse stock.
            for (ReplenishmentRequest request : group) {

                int available = centre.getStock(productCode);

                if (available <= 0) {
                    break;
                }

                int quantity = Math.min(
                        available,
                        request.getRemainingQuantity()
                );

                if (quantity > 0) {
                    allocate(
                            centre,
                            stores.get(request.getStoreId()),
                            request,
                            quantity,
                            results
                    );
                }
            }
        }

        return results;
    }

    // Weighted urgency based on priority, demand,
    // shortage severity and delivery deadline.
    private double calculateScore(
            ReplenishmentRequest request,
            int effectivePriority,
            LocalDateTime evaluationTime) {

        double priorityScore = effectivePriority * 10.0;

        double demandScore =
                Math.min(request.getExpectedDemand(), 1000) / 20.0;

        double shortageScore =
                request.getRemainingQuantity()
                        / (double) (request.getCurrentStock() + 1);

        shortageScore = Math.min(shortageScore, 30.0);

        long hours = ChronoUnit.HOURS.between(
                evaluationTime,
                request.getDeadline()
        );

        double deadlineScore;

        if (hours <= 0) {
            deadlineScore = 30;
        } else if (hours <= 24) {
            deadlineScore = 20;
        } else if (hours <= 48) {
            deadlineScore = 10;
        } else {
            deadlineScore = 0;
        }

        return priorityScore
                + demandScore
                + shortageScore
                + deadlineScore;
    }

    // Safely transfer stock between warehouse and store.
    private void allocate(
            DistributionCentre centre,
            Store store,
            ReplenishmentRequest request,
            int quantity,
            Map<String, Integer> results) {

        if (store == null || request == null) {
            throw new IllegalArgumentException(
                    "Store and request cannot be null"
            );
        }

        if (!store.getStoreId().equals(request.getStoreId())) {
            throw new IllegalArgumentException(
                    "Request does not belong to this store"
            );
        }

        if (quantity <= 0
                || quantity > request.getRemainingQuantity()) {

            throw new IllegalArgumentException(
                    "Invalid allocation quantity"
            );
        }

        String productCode = request.getProductCode();

        // Check that the receiving store will not overflow.
        int currentStoreStock = store.getStock(productCode);

        Math.addExact(currentStoreStock, quantity);

        // Verify available warehouse stock.
        if (centre.getStock(productCode) < quantity) {
            throw new IllegalStateException(
                    "Insufficient warehouse stock"
            );
        }

        // Verify result total will not overflow.
        int previousResult = results.getOrDefault(
                request.getRequestId(), 0
        );

        int updatedResult = Math.addExact(
                previousResult, quantity
        );

        // Remove warehouse inventory.
        if (!centre.removeStock(productCode, quantity)) {
            throw new IllegalStateException(
                    "Warehouse stock removal failed"
            );
        }

        // Update the receiving store.
        try {
            store.addStock(productCode, quantity);
        } catch (RuntimeException exception) {

            // Restore warehouse inventory if delivery fails.
            centre.addStock(productCode, quantity);

            throw exception;
        }

        // Update request tracking.
        try {
            request.allocateStock(quantity);
        } catch (RuntimeException exception) {

            // Roll back the stock transfer.
            store.removeStock(productCode, quantity);
            centre.addStock(productCode, quantity);

            throw exception;
        }

        // Record successful allocation.
        results.put(
                request.getRequestId(),
                updatedResult
        );
    }
}

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

            for (ReplenishmentRequest request : group) {

                int available = centre.getStock(productCode);

                if (available <= 0) {
                    break;
                }

                double weight =
                        scores.get(request) / totalScore;

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

    private void allocate(
            DistributionCentre centre,
            Store store,
            ReplenishmentRequest request,
            int quantity,
            Map<String, Integer> results) {

        String productCode = request.getProductCode();

        if (!centre.removeStock(productCode, quantity)) {
            throw new IllegalStateException(
                    "Insufficient warehouse stock"
            );
        }

        store.addStock(productCode, quantity);
        request.allocateStock(quantity);

        results.merge(
                request.getRequestId(),
                quantity,
                Integer::sum
        );
    }
}

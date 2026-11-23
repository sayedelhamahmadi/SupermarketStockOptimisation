package com.supermarket.analysis;

import com.supermarket.model.Store;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DemandForecastService {

    private final Map<String, Map<String, List<Integer>>> salesHistory;

    public DemandForecastService() {
        salesHistory = new HashMap<>();
    }

    public void recordDailySales(
            String storeId,
            String productCode,
            int quantity) {

        if (storeId == null || storeId.isBlank()
                || productCode == null || productCode.isBlank()) {
            throw new IllegalArgumentException(
                    "Store and product IDs are required"
            );
        }

        if (quantity < 0) {
            throw new IllegalArgumentException(
                    "Daily sales cannot be negative"
            );
        }

        salesHistory
                .computeIfAbsent(storeId, k -> new HashMap<>())
                .computeIfAbsent(productCode, k -> new ArrayList<>())
                .add(quantity);
    }

    public double forecastDailyDemand(
            String storeId,
            String productCode,
            int days) {

        if (days <= 0) {
            throw new IllegalArgumentException(
                    "Days must be positive"
            );
        }

        List<Integer> history = getHistory(storeId, productCode);

        if (history.isEmpty()) {
            return 0;
        }

        int start = Math.max(0, history.size() - days);

        long total = 0;

        for (int i = start; i < history.size(); i++) {
            total += history.get(i);
        }

        return total / (double) (history.size() - start);
    }

    public List<String> predictShortages(
            Store store,
            int forecastDays,
            int historyWindow) {

        if (store == null || forecastDays <= 0
                || historyWindow <= 0) {
            throw new IllegalArgumentException(
                    "Invalid forecast parameters"
            );
        }

        List<String> shortages = new ArrayList<>();

        Map<String, List<Integer>> products =
                salesHistory.getOrDefault(
                        store.getStoreId(),
                        Map.of()
                );

        for (String productCode : products.keySet()) {

            double dailyDemand = forecastDailyDemand(
                    store.getStoreId(),
                    productCode,
                    historyWindow
            );

            double expectedSales = dailyDemand * forecastDays;

            if (store.getStock(productCode) < expectedSales) {
                shortages.add(productCode);
            }
        }

        shortages.sort(String::compareTo);

        return shortages;
    }

    public boolean isDemandSpike(
            String storeId,
            String productCode,
            int recentDays,
            double multiplier) {

        if (recentDays <= 0 || multiplier <= 1.0) {
            throw new IllegalArgumentException(
                    "Invalid spike parameters"
            );
        }

        List<Integer> history = getHistory(storeId, productCode);

        if (history.size() <= recentDays) {
            return false;
        }

        int split = history.size() - recentDays;

        long historicalTotal = 0;
        long recentTotal = 0;

        for (int i = 0; i < split; i++) {
            historicalTotal += history.get(i);
        }

        for (int i = split; i < history.size(); i++) {
            recentTotal += history.get(i);
        }

        double historicalAverage =
                historicalTotal / (double) split;

        double recentAverage =
                recentTotal / (double) recentDays;

        if (historicalAverage == 0) {
            return recentAverage > 0;
        }

        return recentAverage > historicalAverage * multiplier;
    }

    public String identifyTrend(
            String storeId,
            String productCode,
            int window) {

        if (window <= 0) {
            throw new IllegalArgumentException(
                    "Window must be positive"
            );
        }

        List<Integer> history = getHistory(storeId, productCode);

        if (history.size() < window * 2) {
            return "INSUFFICIENT_DATA";
        }

        int size = history.size();

        long previousTotal = 0;
        long recentTotal = 0;

        for (int i = size - 2 * window;
             i < size - window; i++) {
            previousTotal += history.get(i);
        }

        for (int i = size - window; i < size; i++) {
            recentTotal += history.get(i);
        }

        if (recentTotal > previousTotal) {
            return "INCREASING";
        }

        if (recentTotal < previousTotal) {
            return "DECREASING";
        }

        return "STABLE";
    }

    private List<Integer> getHistory(
            String storeId, String productCode) {

        return salesHistory
                .getOrDefault(storeId, Map.of())
                .getOrDefault(productCode, List.of());
    }
}

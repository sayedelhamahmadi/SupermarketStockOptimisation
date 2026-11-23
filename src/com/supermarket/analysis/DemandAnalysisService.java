
package com.supermarket.analysis;

import com.supermarket.model.Store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DemandAnalysisService {

    private final Map<String, Integer> productSales;

    private final Map<String, Integer> storeSales;

    private final Map<String, Map<String, Integer>> salesByStore;

    public DemandAnalysisService() {
        productSales = new HashMap<>();
        storeSales = new HashMap<>();
        salesByStore = new HashMap<>();
    }

    public void recordSale(
            String storeId,
            String productCode,
            int quantity) {

        if (storeId == null || storeId.isBlank()
                || productCode == null || productCode.isBlank()) {
            throw new IllegalArgumentException(
                    "Store ID and product code cannot be empty"
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Sales quantity must be positive"
            );
        }

        productSales.merge(
                productCode, quantity, Math::addExact
        );

        storeSales.merge(
                storeId, quantity, Math::addExact
        );

        salesByStore.computeIfAbsent(
                storeId, key -> new HashMap<>()
        ).merge(
                productCode, quantity, Math::addExact
        );
    }

    public int getProductDemand(String productCode) {
        return productSales.getOrDefault(productCode, 0);
    }

    public int getStoreDemand(String storeId) {
        return storeSales.getOrDefault(storeId, 0);
    }

    public int getStoreProductDemand(
            String storeId, String productCode) {

        Map<String, Integer> sales =
                salesByStore.get(storeId);

        if (sales == null) {
            return 0;
        }

        return sales.getOrDefault(productCode, 0);
    }

    public List<String> rankProductsByDemand() {

        List<String> products =
                new ArrayList<>(productSales.keySet());

        return mergeSort(products, productSales);
    }

    public List<String> rankStoresByDemand() {

        List<String> stores =
                new ArrayList<>(storeSales.keySet());

        return mergeSort(stores, storeSales);
    }

    public List<String> findHighDemandProducts(int threshold) {

        List<String> result = new ArrayList<>();

        for (String product : productSales.keySet()) {
            if (productSales.get(product) > threshold) {
                result.add(product);
            }
        }

        return mergeSort(result, productSales);
    }

    public List<String> findLowDemandProducts(int threshold) {

        List<String> result = new ArrayList<>();

        for (String product : productSales.keySet()) {
            if (productSales.get(product) < threshold) {
                result.add(product);
            }
        }

        return mergeSort(result, productSales);
    }

    public List<String> findStockShortages(
            Store store, int threshold) {

        if (store == null || threshold < 0) {
            throw new IllegalArgumentException(
                    "Invalid store or threshold"
            );
        }

        List<String> shortages = new ArrayList<>();

        for (Map.Entry<String, Integer> entry
                : store.getInventory().entrySet()) {

            if (entry.getValue() < threshold) {
                shortages.add(entry.getKey());
            }
        }

        shortages.sort(String::compareTo);

        return shortages;
    }

    private List<String> mergeSort(
            List<String> items,
            Map<String, Integer> values) {

        if (items.size() <= 1) {
            return items;
        }

        int middle = items.size() / 2;

        List<String> left = mergeSort(
                new ArrayList<>(items.subList(0, middle)),
                values
        );

        List<String> right = mergeSort(
                new ArrayList<>(items.subList(middle, items.size())),
                values
        );

        return merge(left, right, values);
    }

    private List<String> merge(
            List<String> left,
            List<String> right,
            Map<String, Integer> values) {

        List<String> result = new ArrayList<>();

        int i = 0;
        int j = 0;

        while (i < left.size() && j < right.size()) {

            int leftValue = values.get(left.get(i));
            int rightValue = values.get(right.get(j));

            if (leftValue > rightValue ||
                    (leftValue == rightValue &&
                            left.get(i).compareTo(right.get(j)) <= 0)) {

                result.add(left.get(i++));
            } else {
                result.add(right.get(j++));
            }
        }

        while (i < left.size()) {
            result.add(left.get(i++));
        }

        while (j < right.size()) {
            result.add(right.get(j++));
        }

        return result;
    }
}
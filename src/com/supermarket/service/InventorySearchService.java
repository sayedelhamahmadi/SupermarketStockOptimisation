
package com.supermarket.service;

import com.supermarket.model.Product;
import com.supermarket.model.Store;
import com.supermarket.model.DistributionCentre;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides inventory and product search operations for supermarket records.
 * Filtered searches may scan collections, unlike direct hash-map lookups.
 */
public class InventorySearchService {

    private final Map<String, Product> products;
    private final Map<String, Store> stores;
    private final Map<String, DistributionCentre> centres;

    public InventorySearchService() {
        products = new HashMap<>();
        stores = new HashMap<>();
        centres = new HashMap<>();
    }

    public void addProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        products.put(product.getProductCode(), product);
    }

    public boolean removeProduct(String productCode) {
        return products.remove(productCode) != null;
    }

    public Product findProduct(String productCode) {
        return products.get(productCode);
    }

    public List<Product> findByCategory(String category) {
        List<Product> result = new ArrayList<>();

        for (Product product : products.values()) {
            if (product.getCategory().equalsIgnoreCase(category)) {
                result.add(product);
            }
        }

        return result;
    }

    public void addStore(Store store) {
        if (store == null) {
            throw new IllegalArgumentException("Store cannot be null");
        }
        stores.put(store.getStoreId(), store);
    }

    public boolean removeStore(String storeId) {
        return stores.remove(storeId) != null;
    }

    public void addCentre(DistributionCentre centre) {
        if (centre == null) {
            throw new IllegalArgumentException("Centre cannot be null");
        }
        centres.put(centre.getCentreId(), centre);
    }

    public List<String> findLowStockProducts(
            String storeId, int threshold) {

        if (threshold < 0) {
            throw new IllegalArgumentException(
                    "Threshold cannot be negative"
            );
        }

        Store store = stores.get(storeId);

        if (store == null) {
            throw new IllegalArgumentException("Store not found");
        }

        List<String> result = new ArrayList<>();

        for (String productCode : products.keySet()) {
            if (store.getStock(productCode) < threshold) {
                result.add(productCode);
            }
        }

        return result;
    }

    public List<DistributionCentre> findCentresWithProduct(
            String productCode) {

        List<DistributionCentre> result = new ArrayList<>();

        for (DistributionCentre centre : centres.values()) {
            if (centre.getStock(productCode) > 0) {
                result.add(centre);
            }
        }

        return result;
    }

    public List<Store> findStoresWithLowStock(
            String productCode, int threshold) {

        List<Store> result = new ArrayList<>();

        for (Store store : stores.values()) {
            if (store.getStock(productCode) < threshold) {
                result.add(store);
            }
        }

        return result;
    }

    public int getProductCount() {
        return products.size();
    }

    public int getStoreCount() {
        return stores.size();
    }

    public int getCentreCount() {
        return centres.size();
    }
}
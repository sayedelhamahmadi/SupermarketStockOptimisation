
package com.supermarket.model;

import java.util.HashMap;
import java.util.Map;

public class Store {

    private String storeId;
    private String name;
    private String location;
    private Map<String, Integer> inventory;

    // Constructor
    public Store(String storeId, String name, String location) {
        this.storeId = storeId;
        this.name = name;
        this.location = location;
        this.inventory = new HashMap<>();
    }

    // Getters
    public String getStoreId() {
        return storeId;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public Map<String, Integer> getInventory() {
        return new HashMap<>(inventory);
    }

    // Add stock to the store
    public void addStock(String productCode, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        inventory.put(
                productCode,
                inventory.getOrDefault(productCode, 0) + quantity
        );
    }

    // Remove stock from the store
    public boolean removeStock(String productCode, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        int currentStock = getStock(productCode);

        if (currentStock < quantity) {
            return false;
        }

        inventory.put(productCode, currentStock - quantity);
        return true;
    }

    // Check stock quantity
    public int getStock(String productCode) {
        return inventory.getOrDefault(productCode, 0);
    }
}

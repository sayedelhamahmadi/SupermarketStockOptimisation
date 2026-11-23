
package com.supermarket.model;

import java.util.HashMap;
import java.util.Map;

public class Store {

    private String storeId;
    private String name;
    private String location;
    private Map<String, Integer> inventory;

    public Store(String storeId, String name, String location) {
        this.storeId = storeId;
        this.name = name;
        this.location = location;
        this.inventory = new HashMap<>();
    }

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

    public void addStock(String productCode, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        int currentStock = inventory.getOrDefault(productCode, 0);

        // Prevent integer overflow and negative stock.
        int updatedStock = Math.addExact(currentStock, quantity);

        inventory.put(productCode, updatedStock);
    }

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

    public int getStock(String productCode) {
        return inventory.getOrDefault(productCode, 0);
    }
}
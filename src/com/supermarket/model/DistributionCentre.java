package com.supermarket.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a distribution centre and its stock quantities by product code.
 * Inventory changes are validated before stock is added or removed.
 */
public class DistributionCentre {

    private String centreId;
    private String name;
    private String location;
    private Map<String, Integer> inventory;

    public DistributionCentre(String centreId, String name,
                              String location) {
        this.centreId = centreId;
        this.name = name;
        this.location = location;
        this.inventory = new HashMap<>();
    }

    public String getCentreId() {
        return centreId;
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

    public int getStock(String productCode) {
        return inventory.getOrDefault(productCode, 0);
    }

    public void addStock(String productCode, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        inventory.put(
                productCode,
                Math.addExact(getStock(productCode), quantity)
        );
    }

    public boolean removeStock(String productCode, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        int available = getStock(productCode);

        if (available < quantity) {
            return false;
        }

        inventory.put(productCode, available - quantity);
        return true;
    }
}

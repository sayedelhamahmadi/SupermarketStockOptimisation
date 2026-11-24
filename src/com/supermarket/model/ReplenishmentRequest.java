package com.supermarket.model;

import java.time.LocalDateTime;

/**
 * Stores a supermarket replenishment request, including priority, deadline,
 * requested quantity and the quantity still awaiting allocation.
 */
public class ReplenishmentRequest {

    private String requestId;
    private String storeId;
    private String productCode;

    private int quantityRequired;
    private int currentStock;
    private int priority;
    private int expectedDemand;

    private LocalDateTime requestTime;
    private LocalDateTime deadline;

    private int remainingQuantity;

    public ReplenishmentRequest(
            String requestId,
            String storeId,
            String productCode,
            int quantityRequired,
            int currentStock,
            int priority,
            int expectedDemand,
            LocalDateTime deadline) {

        if (quantityRequired <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        if (currentStock < 0 || expectedDemand < 0) {
            throw new IllegalArgumentException(
                    "Stock and demand cannot be negative"
            );
        }

        if (priority < 1 || priority > 5) {
            throw new IllegalArgumentException(
                    "Priority must be between 1 and 5"
            );
        }

        this.requestId = requestId;
        this.storeId = storeId;
        this.productCode = productCode;
        this.quantityRequired = quantityRequired;
        this.currentStock = currentStock;
        this.priority = priority;
        this.expectedDemand = expectedDemand;

        this.requestTime = LocalDateTime.now();
        this.deadline = deadline;

        this.remainingQuantity = quantityRequired;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getStoreId() {
        return storeId;
    }

    public String getProductCode() {
        return productCode;
    }

    public int getQuantityRequired() {
        return quantityRequired;
    }

    public int getCurrentStock() {
        return currentStock;
    }

    public int getPriority() {
        return priority;
    }

    public int getExpectedDemand() {
        return expectedDemand;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public void allocateStock(int quantity) {
        if (quantity <= 0 || quantity > remainingQuantity) {
            throw new IllegalArgumentException(
                    "Invalid allocation quantity"
            );
        }

        remainingQuantity -= quantity;
    }

    public boolean isFulfilled() {
        return remainingQuantity == 0;
    }
}

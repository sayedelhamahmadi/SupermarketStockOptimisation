
package com.supermarket.service;

import com.supermarket.model.DistributionCentre;
import com.supermarket.model.ReplenishmentRequest;
import com.supermarket.model.Store;

public class StockAllocationService {

    public int allocateStock(
            DistributionCentre centre,
            Store store,
            ReplenishmentRequest request) {

        // Validate allocation inputs.
        if (centre == null || store == null || request == null) {
            throw new IllegalArgumentException(
                    "Centre, store and request cannot be null"
            );
        }

        // Ensure stock is delivered to the correct store.
        if (!store.getStoreId().equals(request.getStoreId())) {
            throw new IllegalArgumentException(
                    "Request does not belong to this store"
            );
        }

        // No allocation is needed for completed requests.
        if (request.isFulfilled()) {
            return 0;
        }

        String productCode = request.getProductCode();

        int availableStock = centre.getStock(productCode);
        int requestedStock = request.getRemainingQuantity();

        if (availableStock <= 0 || requestedStock <= 0) {
            return 0;
        }

        // Never allocate more than available or requested.
        int allocatedQuantity = Math.min(
                availableStock,
                requestedStock
        );

        // Check receiving store capacity BEFORE removing stock.
        // Math.addExact throws if the integer would overflow.
        int currentStoreStock = store.getStock(productCode);

        Math.addExact(currentStoreStock, allocatedQuantity);

        // Remove stock from distribution centre.
        boolean removed = centre.removeStock(
                productCode,
                allocatedQuantity
        );

        if (!removed) {
            return 0;
        }

        // Transfer stock to the store.
        try {
            store.addStock(productCode, allocatedQuantity);

        } catch (RuntimeException exception) {

            // Restore warehouse stock if store update fails.
            centre.addStock(productCode, allocatedQuantity);

            throw exception;
        }

        // Update the outstanding request.
        request.allocateStock(allocatedQuantity);

        return allocatedQuantity;
    }
}
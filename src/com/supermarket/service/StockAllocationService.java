package com.supermarket.service;

import com.supermarket.model.DistributionCentre;
import com.supermarket.model.ReplenishmentRequest;
import com.supermarket.model.Store;

public class StockAllocationService {

    public int allocateStock(
            DistributionCentre centre,
            Store store,
            ReplenishmentRequest request) {

        if (centre == null || store == null || request == null) {
            throw new IllegalArgumentException(
                    "Centre, store and request cannot be null"
            );
        }

        if (!store.getStoreId().equals(request.getStoreId())) {
            throw new IllegalArgumentException(
                    "Request does not belong to this store"
            );
        }

        if (request.isFulfilled()) {
            return 0;
        }

        String productCode = request.getProductCode();

        int availableStock = centre.getStock(productCode);
        int requestedStock = request.getRemainingQuantity();

        int allocatedQuantity =
                Math.min(availableStock, requestedStock);

        if (allocatedQuantity <= 0) {
            return 0;
        }

        boolean removed =
                centre.removeStock(productCode, allocatedQuantity);

        if (!removed) {
            return 0;
        }

        store.addStock(productCode, allocatedQuantity);
        request.allocateStock(allocatedQuantity);

        return allocatedQuantity;
    }
}

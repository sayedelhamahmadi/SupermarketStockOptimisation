
package com.supermarket.service;

import com.supermarket.analysis.DemandAnalysisService;
import com.supermarket.analysis.DemandForecastService;
import com.supermarket.model.DistributionCentre;
import com.supermarket.model.Product;
import com.supermarket.model.ReplenishmentRequest;
import com.supermarket.model.Store;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupermarketSystem {

    private final Map<String, Product> products =
            new HashMap<>();

    private final Map<String, Store> stores =
            new HashMap<>();

    private final Map<String, DistributionCentre> centres =
            new HashMap<>();

    private final RequestManager requestManager =
            new RequestManager();

    private final StockAllocationService basicAllocation =
            new StockAllocationService();

    private final AdvancedAllocationService advancedAllocation =
            new AdvancedAllocationService();

    private final DemandAnalysisService demandAnalysis =
            new DemandAnalysisService();

    private final DemandForecastService demandForecast =
            new DemandForecastService();

    // Register a product
    public void addProduct(Product product) {

        if (product == null) {
            throw new IllegalArgumentException(
                    "Product cannot be null"
            );
        }

        products.put(product.getProductCode(), product);
    }

    // Register a store
    public void addStore(Store store) {

        if (store == null) {
            throw new IllegalArgumentException(
                    "Store cannot be null"
            );
        }

        stores.put(store.getStoreId(), store);
    }

    // Register a distribution centre
    public void addDistributionCentre(
            DistributionCentre centre) {

        if (centre == null) {
            throw new IllegalArgumentException(
                    "Distribution centre cannot be null"
            );
        }

        centres.put(centre.getCentreId(), centre);
    }

    // Search methods
    public Product findProduct(String productCode) {
        return products.get(productCode);
    }

    public Store findStore(String storeId) {
        return stores.get(storeId);
    }

    public DistributionCentre findCentre(String centreId) {
        return centres.get(centreId);
    }

    // Receive warehouse delivery
    public void receiveDelivery(
            String centreId,
            String productCode,
            int quantity) {

        DistributionCentre centre = requireCentre(centreId);

        if (!products.containsKey(productCode)) {
            throw new IllegalArgumentException(
                    "Product not found"
            );
        }

        centre.addStock(productCode, quantity);
    }

    // Submit replenishment request
    public void submitRequest(ReplenishmentRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Request cannot be null"
            );
        }

        validateStoreAndProduct(
                request.getStoreId(),
                request.getProductCode()
        );

        requestManager.addRequest(request);
    }

    // Cancel pending request
    public boolean cancelRequest(String requestId) {
        return requestManager.cancelRequest(requestId);
    }

    // Manually change request priority
    public void updateRequestPriority(
            String requestId,
            int priority) {

        requestManager.updatePriority(
                requestId, priority
        );
    }

    // BASIC ALLOCATION
    public int processRequests(String centreId) {

        DistributionCentre centre = requireCentre(centreId);

        requestManager.reprioritiseRequests();

        int initialPending =
                requestManager.getPendingRequestCount();

        int totalAllocated = 0;

        // Process each pending request once
        for (int i = 0; i < initialPending; i++) {

            ReplenishmentRequest request =
                    requestManager.getNextRequest();

            if (request == null) {
                break;
            }

            Store store = stores.get(request.getStoreId());

            try {

                int allocated = basicAllocation.allocateStock(
                        centre,
                        store,
                        request
                );

                totalAllocated = Math.addExact(
                        totalAllocated,
                        allocated
                );

                if (request.isFulfilled()) {
                    requestManager.completeRequest(request);
                } else {
                    requestManager.requeueRequest(request);
                }

            } catch (RuntimeException exception) {

                // Keep an unfinished request in the system
                if (request.isFulfilled()) {
                    requestManager.completeRequest(request);
                } else {
                    requestManager.requeueRequest(request);
                }

                throw exception;
            }
        }

        return totalAllocated;
    }

    // ADVANCED WEIGHTED ALLOCATION
    public Map<String, Integer> processAdvancedRequests(
            String centreId) {

        DistributionCentre centre = requireCentre(centreId);

        // Calculate current demand and deadline priorities
        requestManager.reprioritiseRequests();

        // Capture requests without removing them
        List<ReplenishmentRequest> pendingRequests =
                requestManager.getAllRequests();

        // Capture the effective priorities
        Map<String, Integer> priorities =
                requestManager.getPrioritySnapshot();

        // Allocate stock using dynamic priority values
        Map<String, Integer> results =
                advancedAllocation.allocateStock(
                        centre,
                        stores,
                        pendingRequests,
                        priorities
                );

        // Clean up completed requests and retain incomplete ones
        int pendingCount =
                requestManager.getPendingRequestCount();

        for (int i = 0; i < pendingCount; i++) {

            ReplenishmentRequest request =
                    requestManager.getNextRequest();

            if (request == null) {
                break;
            }

            if (request.isFulfilled()) {

                requestManager.completeRequest(request);

            } else {

                requestManager.requeueRequest(request);
            }
        }

        return results;
    }

    // Validate distribution centre
    private DistributionCentre requireCentre(
            String centreId) {

        DistributionCentre centre = centres.get(centreId);

        if (centre == null) {
            throw new IllegalArgumentException(
                    "Distribution centre not found"
            );
        }

        return centre;
    }

    // Record historical or simulated sale
    public void recordSale(
            String storeId,
            String productCode,
            int quantity) {

        validateStoreAndProduct(
                storeId, productCode
        );

        demandAnalysis.recordSale(
                storeId, productCode, quantity
        );
    }

    // Record one day of simulated sales
    public void recordDailySales(
            String storeId,
            String productCode,
            int quantity) {

        validateStoreAndProduct(
                storeId, productCode
        );

        demandForecast.recordDailySales(
                storeId, productCode, quantity
        );
    }

    // Validate store and product
    private void validateStoreAndProduct(
            String storeId,
            String productCode) {

        if (!stores.containsKey(storeId)) {
            throw new IllegalArgumentException(
                    "Store not found"
            );
        }

        if (!products.containsKey(productCode)) {
            throw new IllegalArgumentException(
                    "Product not found"
            );
        }
    }

    // Forecast future daily demand
    public double forecastDemand(
            String storeId,
            String productCode,
            int days) {

        validateStoreAndProduct(
                storeId, productCode
        );

        return demandForecast.forecastDailyDemand(
                storeId,
                productCode,
                days
        );
    }

    // Access demand analysis
    public DemandAnalysisService getDemandAnalysis() {
        return demandAnalysis;
    }

    // Access forecasting
    public DemandForecastService getDemandForecast() {
        return demandForecast;
    }

    // Current number of pending requests
    public int getPendingRequests() {
        return requestManager.getPendingRequestCount();
    }

    // System statistics
    public int getTotalProducts() {
        return products.size();
    }

    public int getTotalStores() {
        return stores.size();
    }

    public int getTotalCentres() {
        return centres.size();
    }
}
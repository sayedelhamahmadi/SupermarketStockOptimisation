
package com.supermarket.service;

import com.supermarket.analysis.DemandAnalysisService;
import com.supermarket.analysis.DemandForecastService;
import com.supermarket.model.DistributionCentre;
import com.supermarket.model.Product;
import com.supermarket.model.ReplenishmentRequest;
import com.supermarket.model.Store;

import java.util.HashMap;
import java.util.Map;

public class SupermarketSystem {

    private final Map<String, Product> products;
    private final Map<String, Store> stores;
    private final Map<String, DistributionCentre> centres;

    private final RequestManager requestManager;
    private final StockAllocationService allocationService;
    private final DemandAnalysisService demandAnalysis;
    private final DemandForecastService demandForecast;

    public SupermarketSystem() {

        products = new HashMap<>();
        stores = new HashMap<>();
        centres = new HashMap<>();

        requestManager = new RequestManager();
        allocationService = new StockAllocationService();
        demandAnalysis = new DemandAnalysisService();
        demandForecast = new DemandForecastService();
    }

    // Register products
    public void addProduct(Product product) {

        if (product == null) {
            throw new IllegalArgumentException(
                    "Product cannot be null"
            );
        }

        products.put(product.getProductCode(), product);
    }

    // Register stores
    public void addStore(Store store) {

        if (store == null) {
            throw new IllegalArgumentException(
                    "Store cannot be null"
            );
        }

        stores.put(store.getStoreId(), store);
    }

    // Register distribution centres
    public void addDistributionCentre(
            DistributionCentre centre) {

        if (centre == null) {
            throw new IllegalArgumentException(
                    "Distribution centre cannot be null"
            );
        }

        centres.put(centre.getCentreId(), centre);
    }

    // Find a product
    public Product findProduct(String productCode) {
        return products.get(productCode);
    }

    // Find a store
    public Store findStore(String storeId) {
        return stores.get(storeId);
    }

    // Find a distribution centre
    public DistributionCentre findCentre(String centreId) {
        return centres.get(centreId);
    }

    // Receive new stock at a warehouse
    public void receiveDelivery(
            String centreId,
            String productCode,
            int quantity) {

        DistributionCentre centre = centres.get(centreId);

        if (centre == null) {
            throw new IllegalArgumentException(
                    "Distribution centre not found"
            );
        }

        if (!products.containsKey(productCode)) {
            throw new IllegalArgumentException(
                    "Product not found"
            );
        }

        centre.addStock(productCode, quantity);
    }

    // Register a replenishment request
    public void submitRequest(
            ReplenishmentRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Request cannot be null"
            );
        }

        if (!stores.containsKey(request.getStoreId())) {
            throw new IllegalArgumentException(
                    "Store not found"
            );
        }

        if (!products.containsKey(request.getProductCode())) {
            throw new IllegalArgumentException(
                    "Product not found"
            );
        }

        requestManager.addRequest(request);
    }

    // Cancel a pending request
    public boolean cancelRequest(String requestId) {
        return requestManager.cancelRequest(requestId);
    }

    // Process requests using a selected warehouse
    public int processRequests(String centreId) {

        DistributionCentre centre = centres.get(centreId);

        if (centre == null) {
            throw new IllegalArgumentException(
                    "Distribution centre not found"
            );
        }

        requestManager.reprioritiseRequests();

        int pending = requestManager.getPendingRequestCount();
        int totalAllocated = 0;

        // Process each existing request once per cycle
        for (int i = 0; i < pending; i++) {

            ReplenishmentRequest request =
                    requestManager.getNextRequest();

            if (request == null) {
                break;
            }

            Store store = stores.get(request.getStoreId());

            int allocated = allocationService.allocateStock(
                    centre,
                    store,
                    request
            );

            totalAllocated += allocated;

            System.out.println(
                    "Request: " + request.getRequestId()
                            + " | Allocated: " + allocated
                            + " | Remaining: "
                            + request.getRemainingQuantity()
            );

            // Keep incomplete requests pending
            if (!request.isFulfilled()) {
                requestManager.requeueRequest(request);
            }
        }

        return totalAllocated;
    }

    // Record simulated sales
    public void recordSale(
            String storeId,
            String productCode,
            int quantity) {

        if (!stores.containsKey(storeId)
                || !products.containsKey(productCode)) {
            throw new IllegalArgumentException(
                    "Unknown store or product"
            );
        }

        demandAnalysis.recordSale(
                storeId, productCode, quantity
        );
    }

    // Record daily demand data
    public void recordDailySales(
            String storeId,
            String productCode,
            int quantity) {

        if (!stores.containsKey(storeId)
                || !products.containsKey(productCode)) {
            throw new IllegalArgumentException(
                    "Unknown store or product"
            );
        }

        demandForecast.recordDailySales(
                storeId, productCode, quantity
        );
    }

    // Predict future daily demand
    public double forecastDemand(
            String storeId,
            String productCode,
            int days) {

        return demandForecast.forecastDailyDemand(
                storeId, productCode, days
        );
    }

    // Access analysis services
    public DemandAnalysisService getDemandAnalysis() {
        return demandAnalysis;
    }

    public DemandForecastService getDemandForecast() {
        return demandForecast;
    }

    public int getPendingRequests() {
        return requestManager.getPendingRequestCount();
    }

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

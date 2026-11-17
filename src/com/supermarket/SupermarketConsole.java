
package com.supermarket;

import com.supermarket.model.DistributionCentre;
import com.supermarket.model.Product;
import com.supermarket.model.ReplenishmentRequest;
import com.supermarket.model.Store;
import com.supermarket.service.SupermarketSystem;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Scanner;

public class SupermarketConsole {

    private static final SupermarketSystem system =
            new SupermarketSystem();

    private static final Scanner scanner =
            new Scanner(System.in);

    public static void main(String[] args) {

        boolean running = true;

        while (running) {
            displayMenu();

            int choice = readInt("Choose an option: ");

            try {
                switch (choice) {
                    case 1 -> addProduct();
                    case 2 -> addStore();
                    case 3 -> addWarehouse();
                    case 4 -> receiveDelivery();
                    case 5 -> submitRequest();
                    case 6 -> processBasic();
                    case 7 -> processAdvanced();
                    case 8 -> checkStock();
                    case 9 -> cancelRequest();
                    case 10 -> changePriority();
                    case 11 -> recordSale();
                    case 12 -> recordDailySales();
                    case 13 -> viewDemandAnalysis();
                    case 14 -> forecastDemand();
                    case 15 -> showStatistics();
                    case 0 -> {
                        System.out.println("Goodbye!");
                        running = false;
                    }
                    default -> System.out.println(
                            "Invalid option."
                    );
                }
            } catch (IllegalArgumentException
                     | IllegalStateException e) {
                System.out.println(
                        "Error: " + e.getMessage()
                );
            }
        }
    }

    private static void displayMenu() {

        System.out.println();
        System.out.println("==================================");
        System.out.println(" SUPERMARKET STOCK OPTIMISATION");
        System.out.println("==================================");
        System.out.println("1. Add Product");
        System.out.println("2. Add Store");
        System.out.println("3. Add Distribution Centre");
        System.out.println("4. Receive Warehouse Delivery");
        System.out.println("5. Submit Replenishment Request");
        System.out.println("6. Basic Stock Allocation");
        System.out.println("7. Advanced Stock Allocation");
        System.out.println("8. Check Stock");
        System.out.println("9. Cancel Request");
        System.out.println("10. Update Request Priority");
        System.out.println("11. Record Sale");
        System.out.println("12. Record Daily Sales");
        System.out.println("13. Demand Analysis");
        System.out.println("14. Demand Forecast");
        System.out.println("15. System Statistics");
        System.out.println("0. Exit");
        System.out.println("==================================");
    }

    private static void addProduct() {

        String code = readText("Product code: ");
        String name = readText("Name: ");
        String category = readText("Category: ");
        String supplier = readText("Supplier: ");

        system.addProduct(
                new Product(code, name, category, supplier)
        );

        System.out.println("Product registered.");
    }

    private static void addStore() {

        String id = readText("Store ID: ");
        String name = readText("Store name: ");
        String location = readText("Location: ");

        system.addStore(
                new Store(id, name, location)
        );

        System.out.println("Store registered.");
    }

    private static void addWarehouse() {

        String id = readText("Warehouse ID: ");
        String name = readText("Warehouse name: ");
        String location = readText("Location: ");

        system.addDistributionCentre(
                new DistributionCentre(id, name, location)
        );

        System.out.println("Warehouse registered.");
    }

    private static void receiveDelivery() {

        String centreId = readText("Warehouse ID: ");
        String productCode = readText("Product code: ");
        int quantity = readInt("Quantity: ");

        system.receiveDelivery(
                centreId, productCode, quantity
        );

        System.out.println("Delivery received.");
    }

    private static void submitRequest() {

        String requestId = readText("Request ID: ");
        String storeId = readText("Store ID: ");
        String productCode = readText("Product code: ");

        int quantity = readInt("Quantity required: ");

        Store store = system.findStore(storeId);

        if (store == null) {
            System.out.println("Store not found.");
            return;
        }

        int currentStock = store.getStock(productCode);

        int priority = readInt("Priority (1-5): ");
        int demand = readInt("Expected demand: ");
        int hours = readInt("Deadline in hours: ");

        ReplenishmentRequest request =
                new ReplenishmentRequest(
                        requestId,
                        storeId,
                        productCode,
                        quantity,
                        currentStock,
                        priority,
                        demand,
                        LocalDateTime.now().plusHours(hours)
                );

        system.submitRequest(request);

        System.out.println("Request submitted.");
    }

    private static void processBasic() {

        String centreId = readText("Warehouse ID: ");

        int allocated = system.processRequests(centreId);

        System.out.println(
                "Total stock allocated: " + allocated
        );
    }

    private static void processAdvanced() {

        String centreId = readText("Warehouse ID: ");

        Map<String, Integer> results =
                system.processAdvancedRequests(centreId);

        int total = 0;

        System.out.println("ADVANCED ALLOCATION RESULTS");

        for (Map.Entry<String, Integer> entry
                : results.entrySet()) {

            System.out.println(
                    "Request " + entry.getKey()
                            + " | Allocated: " + entry.getValue()
            );

            total += entry.getValue();
        }

        System.out.println("Total allocated: " + total);
        System.out.println(
                "Pending requests: "
                        + system.getPendingRequests()
        );
    }

    private static void checkStock() {

        String type = readText("Store or Warehouse (S/W): ");
        String id = readText("Location ID: ");
        String productCode = readText("Product code: ");

        if (type.equalsIgnoreCase("S")) {

            Store store = system.findStore(id);

            if (store == null) {
                System.out.println("Store not found.");
                return;
            }

            System.out.println(
                    "Stock: " + store.getStock(productCode)
            );

        } else if (type.equalsIgnoreCase("W")) {

            DistributionCentre centre =
                    system.findCentre(id);

            if (centre == null) {
                System.out.println("Warehouse not found.");
                return;
            }

            System.out.println(
                    "Stock: " + centre.getStock(productCode)
            );

        } else {
            System.out.println("Invalid location type.");
        }
    }

    private static void cancelRequest() {

        String id = readText("Request ID: ");

        boolean cancelled = system.cancelRequest(id);

        System.out.println(
                cancelled
                        ? "Request cancelled."
                        : "Request not found."
        );
    }

    private static void changePriority() {

        String id = readText("Request ID: ");
        int priority = readInt("New priority (1-5): ");

        system.updateRequestPriority(id, priority);

        System.out.println("Priority updated.");
    }

    private static void recordSale() {

        String storeId = readText("Store ID: ");
        String productCode = readText("Product code: ");
        int quantity = readInt("Units sold: ");

        system.recordSale(
                storeId, productCode, quantity
        );

        System.out.println("Sale recorded.");
    }

    private static void recordDailySales() {

        String storeId = readText("Store ID: ");
        String productCode = readText("Product code: ");
        int quantity = readInt("Units sold today: ");

        system.recordDailySales(
                storeId, productCode, quantity
        );

        System.out.println("Daily sales recorded.");
    }

    private static void viewDemandAnalysis() {

        System.out.println("=== PRODUCT DEMAND RANKING ===");

        for (String product : system.getDemandAnalysis()
                .rankProductsByDemand()) {

            System.out.println(
                    product + " | Sold: "
                            + system.getDemandAnalysis()
                            .getProductDemand(product)
            );
        }

        System.out.println("=== STORE DEMAND RANKING ===");

        for (String store : system.getDemandAnalysis()
                .rankStoresByDemand()) {

            System.out.println(
                    store + " | Sold: "
                            + system.getDemandAnalysis()
                            .getStoreDemand(store)
            );
        }
    }

    private static void forecastDemand() {

        String storeId = readText("Store ID: ");
        String productCode = readText("Product code: ");
        int days = readInt("Moving average window (days): ");

        double predicted = system.forecastDemand(
                storeId, productCode, days
        );

        System.out.printf(
                "Forecast daily demand: %.2f units%n",
                predicted
        );

        String trend = system.getDemandForecast()
                .identifyTrend(storeId, productCode, days);

        System.out.println("Demand trend: " + trend);
    }

    private static void showStatistics() {

        System.out.println("=== SYSTEM STATISTICS ===");

        System.out.println(
                "Products: " + system.getTotalProducts()
        );

        System.out.println(
                "Stores: " + system.getTotalStores()
        );

        System.out.println(
                "Warehouses: " + system.getTotalCentres()
        );

        System.out.println(
                "Pending requests: " + system.getPendingRequests()
        );
    }

    private static String readText(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }

    private static int readInt(String message) {

        while (true) {

            System.out.print(message);

            try {
                return Integer.parseInt(
                        scanner.nextLine().trim()
                );

            } catch (NumberFormatException e) {
                System.out.println(
                        "Please enter a valid number."
                );
            }
        }
    }
}


package com.supermarket;

import com.supermarket.model.DistributionCentre;
import com.supermarket.model.Product;
import com.supermarket.model.ReplenishmentRequest;
import com.supermarket.model.Store;
import com.supermarket.service.SupermarketSystem;

import java.time.LocalDateTime;
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
                    case 6 -> processRequests();
                    case 7 -> checkStock();
                    case 8 -> showStatistics();
                    case 0 -> {
                        System.out.println("Goodbye!");
                        running = false;
                    }
                    default -> System.out.println(
                            "Invalid option. Try again."
                    );
                }

            } catch (IllegalArgumentException
                     | IllegalStateException e) {

                System.out.println("Error: " + e.getMessage());
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
        System.out.println("6. Process Stock Requests");
        System.out.println("7. Check Stock");
        System.out.println("8. View System Statistics");
        System.out.println("0. Exit");
        System.out.println("==================================");
    }

    private static void addProduct() {

        String code = readText("Product code: ");
        String name = readText("Product name: ");
        String category = readText("Category: ");
        String supplier = readText("Supplier: ");

        system.addProduct(
                new Product(code, name, category, supplier)
        );

        System.out.println("Product added successfully.");
    }

    private static void addStore() {

        String id = readText("Store ID: ");
        String name = readText("Store name: ");
        String location = readText("Location: ");

        system.addStore(new Store(id, name, location));

        System.out.println("Store added successfully.");
    }

    private static void addWarehouse() {

        String id = readText("Warehouse ID: ");
        String name = readText("Warehouse name: ");
        String location = readText("Location: ");

        system.addDistributionCentre(
                new DistributionCentre(id, name, location)
        );

        System.out.println("Warehouse added successfully.");
    }

    private static void receiveDelivery() {

        String centreId = readText("Warehouse ID: ");
        String productCode = readText("Product code: ");
        int quantity = readInt("Quantity delivered: ");

        system.receiveDelivery(
                centreId, productCode, quantity
        );

        System.out.println("Delivery recorded successfully.");
    }

    private static void submitRequest() {

        String requestId = readText("Request ID: ");
        String storeId = readText("Store ID: ");
        String productCode = readText("Product code: ");

        int quantity = readInt("Quantity required: ");
        int currentStock = readInt("Current stock: ");
        int priority = readInt("Priority (1-5): ");
        int expectedDemand = readInt("Expected demand: ");
        int deadlineHours = readInt(
                "Delivery deadline (hours from now): "
        );

        ReplenishmentRequest request =
                new ReplenishmentRequest(
                        requestId,
                        storeId,
                        productCode,
                        quantity,
                        currentStock,
                        priority,
                        expectedDemand,
                        LocalDateTime.now().plusHours(
                                deadlineHours
                        )
                );

        system.submitRequest(request);

        System.out.println("Request submitted successfully.");
    }

    private static void processRequests() {

        String centreId = readText("Warehouse ID: ");

        int allocated = system.processRequests(centreId);

        System.out.println(
                "Total allocated: " + allocated
        );
    }

    private static void checkStock() {

        String locationType =
                readText("Store or warehouse? (S/W): ");

        String id = readText("Location ID: ");
        String productCode = readText("Product code: ");

        if (locationType.equalsIgnoreCase("S")) {

            Store store = system.findStore(id);

            if (store == null) {
                System.out.println("Store not found.");
                return;
            }

            System.out.println(
                    "Available stock: "
                            + store.getStock(productCode)
            );

        } else if (locationType.equalsIgnoreCase("W")) {

            DistributionCentre centre =
                    system.findCentre(id);

            if (centre == null) {
                System.out.println("Warehouse not found.");
                return;
            }

            System.out.println(
                    "Available stock: "
                            + centre.getStock(productCode)
            );

        } else {
            System.out.println("Invalid location type.");
        }
    }

    private static void showStatistics() {

        System.out.println(
                "Total products: " + system.getTotalProducts()
        );

        System.out.println(
                "Total stores: " + system.getTotalStores()
        );

        System.out.println(
                "Total warehouses: " + system.getTotalCentres()
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

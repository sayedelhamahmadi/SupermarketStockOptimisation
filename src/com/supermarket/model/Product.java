package com.supermarket.model;

/**
 * Represents a supermarket product identified by its product code.
 * The model stores the descriptive information used for inventory lookup.
 */
public class Product {

    private String productCode;
    private String name;
    private String category;
    private String supplier;


    public Product(String productCode, String name,
                   String category, String supplier) {

        this.productCode = productCode;
        this.name = name;
        this.category = category;
        this.supplier = supplier;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getSupplier() {
        return supplier;
    }
}

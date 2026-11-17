package com.supermarket.model;

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

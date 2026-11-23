# Supermarket Distribution and Stock Optimisation System

## Project Overview

This project is a Java-based supermarket stock management and
distribution system developed for the CPS5001 Algorithms and
Data Structures module.

The system manages supermarket products, stores, distribution
centres, stock replenishment requests and inventory allocation.

It demonstrates the application of data structures and algorithms
to supermarket stock distribution problems.

## Technologies Used

- Java 21+
- IntelliJ IDEA
- Git version control
- Java Collections Framework

## Main Features

### Inventory Management
- Register supermarket products
- Manage stores and distribution centres
- Receive stock deliveries
- Track inventory quantities
- Search products and identify low stock

### Replenishment Management
- Submit stock requests
- Prioritise urgent requests
- Update request priorities
- Cancel pending requests
- Support partially fulfilled requests

### Stock Allocation
Two allocation strategies are implemented:

**Basic Allocation**
Processes stock requests in priority order.

**Advanced Allocation**
Uses weighted scoring based on:
- Request priority
- Expected demand
- Current stock shortage
- Delivery deadline

### Demand Analysis
- Record simulated sales
- Rank products by demand
- Rank stores by demand
- Identify high-demand and low-demand products
- Detect stock shortages

### Demand Forecasting
- Moving average forecasting
- Shortage prediction
- Demand spike detection
- Increasing and decreasing demand trends

## Data Structures and Algorithms

| Structure / Algorithm | Purpose |
|---|---|
| HashMap | Product, store and inventory lookup |
| PriorityQueue | Urgent replenishment requests |
| Merge Sort | Ranking products and stores |
| Linear Search | Identifying products meeting conditions |
| Weighted Allocation | Distributing limited warehouse stock |
| Moving Average | Forecasting product demand |

## Project Structure

```text
src/
└── com/supermarket/
    ├── Main.java
    ├── SupermarketConsole.java
    ├── PerformanceBenchmark.java
    ├── model/
    │   ├── Product.java
    │   ├── Store.java
    │   ├── DistributionCentre.java
    │   └── ReplenishmentRequest.java
    ├── service/
    │   ├── RequestManager.java
    │   ├── StockAllocationService.java
    │   ├── AdvancedAllocationService.java
    │   ├── InventorySearchService.java
    │   └── SupermarketSystem.java
    └── analysis/
        ├── DemandAnalysisService.java
        └── DemandForecastService.java
```

## How to Run

1. Open the project using IntelliJ IDEA.
2. Configure JDK 21 or newer.
3. Open `src/com/supermarket/SupermarketConsole.java`.
4. Run the `main()` method.
5. Follow the console menu to operate the system.

## Example Workflow

1. Register a product.
2. Register a supermarket store.
3. Register a distribution centre.
4. Add inventory to the distribution centre.
5. Submit replenishment requests.
6. Process requests using basic or advanced allocation.
7. Check updated stock quantities.
8. Record sales and analyse demand.

## Performance Benchmarking

`PerformanceBenchmark.java` compares the basic and advanced
allocation algorithms using simulated workloads.

Dataset sizes:
- 100 requests
- 500 requests
- 1,000 requests
- 5,000 requests
- 10,000 requests

Execution time is measured using `System.nanoTime()`.

The initial results are preliminary single-run measurements.
Repeated runs and warm-up would improve measurement reliability.

## Known Limitations

- Inventory is stored in memory and is not saved between runs.
- Demand forecasting assumes sales observations are recorded
  in chronological order.
- Advanced allocation uses a heuristic rather than guaranteeing
  a globally optimal solution.
- The console interface is designed for demonstration purposes.
- Additional integration and performance validation is needed.

## Academic Context

Module: CPS5001 - Algorithms and Data Structures

Project: Supermarket Distribution and Stock Optimisation System

The project demonstrates practical application of Java data
structures and algorithms to inventory and distribution problems.
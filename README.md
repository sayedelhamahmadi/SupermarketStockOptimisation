
# Supermarket Distribution and Stock Optimisation System

## 1. Project Overview

This project was developed for CPS5001 Algorithms and Data Structures.

It is a Java 21 application that manages supermarket inventory, stock replenishment requests and distribution centre operations.

The main objective is to demonstrate how data structures and algorithms can improve stock allocation, prioritise urgent requests and analyse supermarket demand.

The system includes two stock allocation approaches: basic priority-based allocation and advanced weighted allocation.

## 2. Technologies Used

- Java 21
- IntelliJ IDEA
- Java Collections Framework
- Git and GitHub
- Java console interface

No external libraries are required.

## 3. Main Features

### Inventory Management

- Register products, stores and distribution centres.
- Receive stock deliveries.
- Track warehouse and store inventory.
- Search inventory records.
- Detect low stock and shortages.
- Prevent integer overflow during store stock additions.

### Replenishment Management

- Create replenishment requests.
- Manage requests using a PriorityQueue.
- Apply dynamic priority scores.
- Change priorities manually.
- Cancel pending requests.
- Handle partially fulfilled requests.
- Prioritise requests using demand, shortages and deadlines.

### Basic Stock Allocation

The basic algorithm processes replenishment requests according to their effective priority.

It allocates available warehouse inventory to each request until the request is fulfilled or the warehouse has insufficient stock.

Partial allocations are supported.

### Advanced Stock Allocation

The advanced algorithm uses weighted urgency scores based on:

- Effective request priority.
- Expected product demand.
- Current store stock shortage.
- Delivery deadline.

Requests are grouped by product and sorted by weighted urgency.

The algorithm performs two allocation stages:

1. Distribute warehouse stock proportionally using urgency scores.
2. Redistribute remaining stock to outstanding requests in score order.

Stock quantities and request fulfilment are updated after successful transfers.

Historical sales data can also increase the effective priority used by advanced allocation.

## 4. Demand Analysis

The system supports:

- Recording product sales.
- Ranking products by demand.
- Ranking stores by demand.
- Identifying high-demand products.
- Comparing demand across stores.

The demand analysis service uses a custom merge sort implementation for ranking.

## 5. Demand Forecasting

Demand forecasting includes:

- Moving-average demand prediction.
- Shortage prediction.
- Demand spike detection.
- Increasing, decreasing and stable demand trends.
- Detection of recurring weekly demand patterns.
- Calculation of average demand for each position in a seven-day cycle.

The weekly-pattern detector compares recurring high-demand positions across two weeks.

This is a simple heuristic intended for simulated supermarket data, not a statistically validated seasonal forecasting model.

## 6. Data Structures and Algorithms

| Structure or Algorithm | Purpose |
|---|---|
| HashMap | Fast lookup of products, stores, requests and inventory |
| PriorityQueue | Process urgent replenishment requests |
| Comparator | Order requests by priority and deadlines |
| Merge Sort | Rank products and stores by demand |
| Weighted Scoring | Calculate advanced allocation urgency |
| Proportional Allocation | Share limited warehouse stock |
| Moving Average | Forecast demand from historical sales |
| Weekly Pattern Detection | Identify repeated high-demand periods |

## 7. Project Structure

```text
SupermarketStockOptimisation/
├── README.md
└── src/
    └── com/
        └── supermarket/
            ├── Main.java
            ├── SupermarketConsole.java
            ├── PerformanceBenchmark.java
            ├── AllocationTests.java
            │
            ├── model/
            │   ├── Product.java
            │   ├── Store.java
            │   ├── DistributionCentre.java
            │   └── ReplenishmentRequest.java
            │
            ├── service/
            │   ├── RequestManager.java
            │   ├── StockAllocationService.java
            │   ├── AdvancedAllocationService.java
            │   ├── InventorySearchService.java
            │   └── SupermarketSystem.java
            │
            └── analysis/
                ├── DemandAnalysisService.java
                └── DemandForecastService.java
```

## 8. How to Run the Project

### Requirements

- Java Development Kit (JDK) 21 or later.
- IntelliJ IDEA or another Java IDE.

### Run the Console Application

1. Open the project in IntelliJ IDEA.
2. Configure the project SDK to Java 21.
3. Open `SupermarketConsole.java`.
4. Run its `main()` method.
5. Use the numbered console menu.

### Run the Allocation Demonstration

Open `Main.java` and run its `main()` method.

This demonstrates basic and advanced stock allocation using predefined supermarket requests.

### Run Automated Tests

Open `AllocationTests.java` and run its `main()` method.

The program reports passed and failed tests and returns a non-zero exit status if any test fails.

### Run Performance Benchmarks

Open `PerformanceBenchmark.java` and run its `main()` method.

The benchmark compares basic and advanced allocation using different request volumes.

## 9. Automated Testing

The project includes a standalone Java test runner containing 12 tests.

| Test | Description |
|---|---|
| 1 | Full stock allocation |
| 2 | Partial stock allocation |
| 3 | Warehouse stock conservation |
| 4 | No warehouse stock available |
| 5 | Wrong-store validation |
| 6 | Dynamic priority ordering |
| 7 | Advanced allocation conservation |
| 8 | Store integer overflow protection |
| 9 | Basic allocation overflow protection |
| 10 | Advanced allocation overflow protection |
| 11 | Weekly demand-pattern detection |
| 12 | Historical demand influencing allocation |

### Latest Test Results

Passed: 12

Failed: 0

Total: 12

All 12 tests passed during the latest recorded execution.

The tests use a custom Java runner rather than JUnit.

## 10. Performance Benchmarking

The benchmark evaluates request volumes of:

- 100 requests
- 500 requests
- 1,000 requests
- 5,000 requests
- 10,000 requests

The updated methodology uses:

- Three JVM warm-up runs.
- Five measured runs.
- Fresh, identically generated input data.
- Alternating execution order.
- Average execution time.
- Standard deviation.
- Checks that expected quantities were allocated.

### Example Results

| Requests | Basic Average (ms) | Advanced Average (ms) |
|---:|---:|---:|
| 100 | 0.697 | 0.384 |
| 500 | 1.952 | 1.481 |
| 1,000 | 2.875 | 2.247 |
| 5,000 | 11.203 | 4.646 |
| 10,000 | 22.868 | 10.257 |

These results were recorded on the development computer and represent one benchmark execution containing five measured runs per request size.

The advanced approach was faster in this execution.

However, the benchmark measures two different workflows: basic request scheduling plus allocation, and advanced grouping, scoring, sorting and allocation. It does not establish that advanced allocation is universally faster.

Execution times can vary due to JVM optimisation, memory management and system activity.

## 11. Algorithm Complexity

Typical complexity characteristics include:

| Operation | Complexity |
|---|---|
| HashMap lookup | O(1) average |
| PriorityQueue insertion | O(log n) |
| PriorityQueue removal of highest-priority element | O(log n) |
| Full queue reprioritisation and rebuilding | O(n log n) conservative upper bound |
| Merge Sort | O(n log n) |
| Moving-average calculation | O(k), for a window of k days |
| Weekly-pattern detection | O(1) for the final two weeks |
| Advanced request sorting | O(n log n) |

These are operation-level complexity descriptions. Overall algorithm complexity depends on the number of requests, products and allocation passes.

## 12. Reliability and Validation

The system includes:

- Validation of allocation inputs.
- Protection against invalid stock quantities.
- Validation that requests belong to the correct store.
- Prevention of integer overflow in store stock additions.
- Stock-transfer checks before warehouse inventory removal.
- Rollback handling for selected allocation failures.
- Conservation checks in automated tests.

These safeguards improve reliability but do not constitute full database transaction support.

## 13. Limitations

- Data is stored in memory and is not persisted between application runs.
- The application uses simulated supermarket data.
- Demand forecasting uses simple historical methods.
- Weekly-pattern detection requires consecutively recorded daily observations.
- The current test runner is not based on JUnit.
- Benchmark results depend on hardware and workload.
- The application is single-threaded and does not provide concurrent inventory transactions.

## 14. Future Improvements

Possible extensions include:

- Persistent inventory storage.
- Additional forecasting models.
- More sophisticated seasonal analysis.
- Automated inventory import.
- Multi-warehouse allocation.
- Expanded automated test coverage.
- Concurrent transaction handling.

## 15. Academic Context

This project demonstrates the application of data structures, algorithms, complexity analysis, performance benchmarking and software testing to supermarket inventory distribution.

Developed for CPS5001 Algorithms and Data Structures.

## 16. Project Verification and Submission

### Verification Procedure

Before preparing a release, the following checks should be completed:

1. Open the project using IntelliJ IDEA with JDK 21.
2. Run `Main.java` to demonstrate basic and advanced stock allocation.
3. Run `AllocationTests.java` to check the allocation algorithms,
   stock conservation, priority management and demand analysis.
4. Run `PerformanceBenchmark.java` to compare the two allocation
   workflows at different request volumes.
5. Review the console output for exceptions or failed tests.
6. Confirm that the Git working tree contains no unintended changes.

### Test Evidence

The latest recorded test run completed with:

- Total tests: 12
- Passed: 12
- Failed: 0
- Exit code: 0

The test suite uses a standalone Java runner rather than JUnit.

### Performance Evaluation

Performance measurements use three warm-up runs and five
measured runs for each workload size.

The benchmark reports average execution time and standard
deviation. Timing results may vary depending on JVM
optimisation and system load.

The basic and advanced workflows are not identical, so their
execution times should not be interpreted as a universally
fair comparison of algorithm speed.

### Version Control

Git is used to track implementation changes and maintain
project history.

Documentation updates are developed on separate branches
and submitted through GitHub pull requests.

Each pull request records the proposed changes before
they are merged into the main development branch.

### Source Code Packaging

The source submission should include:

- Java source files in `src/`
- `README.md`
- `.gitignore`
- Git history where required by the assessment

Generated compiled files and IntelliJ build output should
not be included in the submission archive.

The technical report is submitted separately as a PDF.


package com.supermarket.service;

import com.supermarket.model.ReplenishmentRequest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class RequestManager {

    private final PriorityQueue<ReplenishmentRequest> requestQueue;

    public RequestManager() {
        Comparator<ReplenishmentRequest> comparator =
                Comparator.comparingInt(
                                ReplenishmentRequest::getPriority
                        ).reversed()
                        .thenComparing(
                                ReplenishmentRequest::getDeadline
                        )
                        .thenComparing(
                                ReplenishmentRequest::getRequestTime
                        )
                        .thenComparing(
                                ReplenishmentRequest::getRequestId
                        );

        requestQueue = new PriorityQueue<>(comparator);
    }

    // Register a replenishment request
    public void addRequest(ReplenishmentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Request cannot be null"
            );
        }

        requestQueue.add(request);
    }

    // Remove and return the highest-priority request
    public ReplenishmentRequest getNextRequest() {
        return requestQueue.poll();
    }

    // View the next request without removing it
    public ReplenishmentRequest peekNextRequest() {
        return requestQueue.peek();
    }

    // Count pending requests
    public int getPendingRequestCount() {
        return requestQueue.size();
    }

    public boolean hasPendingRequests() {
        return !requestQueue.isEmpty();
    }

    // Get all requests in priority order
    public List<ReplenishmentRequest> getAllRequests() {
        PriorityQueue<ReplenishmentRequest> copy =
                new PriorityQueue<>(requestQueue);

        List<ReplenishmentRequest> sortedRequests =
                new ArrayList<>();

        while (!copy.isEmpty()) {
            sortedRequests.add(copy.poll());
        }

        return sortedRequests;
    }
}

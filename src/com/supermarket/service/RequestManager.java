
package com.supermarket.service;

import com.supermarket.model.ReplenishmentRequest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class RequestManager {

    private final Map<String, ReplenishmentRequest> requests;
    private final PriorityQueue<ReplenishmentRequest> requestQueue;
    private final Map<String, Integer> dynamicPriorities;

    public RequestManager() {
        requests = new HashMap<>();
        dynamicPriorities = new HashMap<>();

        Comparator<ReplenishmentRequest> comparator =
                Comparator.comparingInt(
                                (ReplenishmentRequest request) ->
                                        dynamicPriorities.getOrDefault(
                                                request.getRequestId(),
                                                request.getPriority()
                                        )
                        ).reversed()
                        .thenComparing(ReplenishmentRequest::getDeadline)
                        .thenComparing(ReplenishmentRequest::getRequestTime)
                        .thenComparing(ReplenishmentRequest::getRequestId);

        requestQueue = new PriorityQueue<>(comparator);
    }

    // Register a new request
    public void addRequest(ReplenishmentRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Request cannot be null"
            );
        }

        if (requests.containsKey(request.getRequestId())) {
            throw new IllegalArgumentException(
                    "Duplicate request ID"
            );
        }

        if (!request.isFulfilled()) {
            requests.put(request.getRequestId(), request);
            dynamicPriorities.put(
                    request.getRequestId(),
                    request.getPriority()
            );

            requestQueue.add(request);
        }
    }

    // Cancel a pending request
    public boolean cancelRequest(String requestId) {

        ReplenishmentRequest request = requests.remove(requestId);

        if (request == null) {
            return false;
        }

        requestQueue.remove(request);
        dynamicPriorities.remove(requestId);

        return true;
    }

    // Change a request's effective priority
    public void updatePriority(String requestId, int newPriority) {

        if (newPriority < 1 || newPriority > 5) {
            throw new IllegalArgumentException(
                    "Priority must be between 1 and 5"
            );
        }

        if (!requests.containsKey(requestId)) {
            throw new IllegalArgumentException(
                    "Request not found"
            );
        }

        dynamicPriorities.put(requestId, newPriority);
        rebuildQueue();
    }

    // Automatically update priorities using demand and deadlines
    public void reprioritiseRequests() {

        LocalDateTime now = LocalDateTime.now();

        for (ReplenishmentRequest request : requests.values()) {

            int score = request.getPriority();

            if (request.getExpectedDemand() > 200) {
                score += 1;
            }

            if (request.getCurrentStock() < 10) {
                score += 1;
            }

            long hours = ChronoUnit.HOURS.between(
                    now, request.getDeadline()
            );

            if (hours <= 24) {
                score += 1;
            }

            dynamicPriorities.put(
                    request.getRequestId(),
                    Math.min(score, 8)
            );
        }

        rebuildQueue();
    }

    // Rebuild priority queue after changing priorities
    private void rebuildQueue() {

        requestQueue.clear();

        for (ReplenishmentRequest request : requests.values()) {
            if (!request.isFulfilled()) {
                requestQueue.add(request);
            }
        }
    }

    // Get and remove the next pending request
    public ReplenishmentRequest getNextRequest() {

        ReplenishmentRequest request = requestQueue.poll();

        if (request != null) {
            requests.remove(request.getRequestId());
            dynamicPriorities.remove(request.getRequestId());
        }

        return request;
    }

    public ReplenishmentRequest peekNextRequest() {
        return requestQueue.peek();
    }

    public boolean hasPendingRequests() {
        return !requestQueue.isEmpty();
    }

    public int getPendingRequestCount() {
        return requestQueue.size();
    }

    public List<ReplenishmentRequest> getAllRequests() {

        PriorityQueue<ReplenishmentRequest> copy =
                new PriorityQueue<>(requestQueue);

        List<ReplenishmentRequest> result = new ArrayList<>();

        while (!copy.isEmpty()) {
            result.add(copy.poll());
        }

        return result;
    }

    // Put a partially fulfilled request back into the queue
    public void requeueRequest(ReplenishmentRequest request) {

        if (request == null || request.isFulfilled()) {
            return;
        }

        if (requests.containsKey(request.getRequestId())) {
            throw new IllegalArgumentException(
                    "Request is already pending"
            );
        }

        addRequest(request);
    }
}

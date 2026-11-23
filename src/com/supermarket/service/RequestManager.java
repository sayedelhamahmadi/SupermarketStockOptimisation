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

    private final Map<String, ReplenishmentRequest> requests =
            new HashMap<>();

    private final Map<String, ReplenishmentRequest> inProgress =
            new HashMap<>();

    private final Map<String, Integer> manualPriorities =
            new HashMap<>();

    private final Map<String, Integer> effectivePriorities =
            new HashMap<>();

    private final PriorityQueue<ReplenishmentRequest> queue;

    public RequestManager() {

        Comparator<ReplenishmentRequest> comparator =
                Comparator.comparingInt(
                                (ReplenishmentRequest request) ->
                                        getEffectivePriority(request)
                        ).reversed()
                        .thenComparing(ReplenishmentRequest::getDeadline)
                        .thenComparing(ReplenishmentRequest::getRequestTime)
                        .thenComparing(ReplenishmentRequest::getRequestId);

        queue = new PriorityQueue<>(comparator);
    }


    public void addRequest(ReplenishmentRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Request cannot be null"
            );
        }

        String id = request.getRequestId();

        if (requests.containsKey(id)
                || inProgress.containsKey(id)) {
            throw new IllegalArgumentException(
                    "Duplicate request ID"
            );
        }

        if (request.isFulfilled()) {
            return;
        }

        requests.put(id, request);

        // Recalculate priorities and rebuild the queue.
        reprioritiseRequests();
    }

    public boolean cancelRequest(String requestId) {

        ReplenishmentRequest request =
                requests.remove(requestId);

        if (request == null) {
            return false;
        }

        queue.remove(request);
        manualPriorities.remove(requestId);
        effectivePriorities.remove(requestId);

        return true;
    }

    public void updatePriority(
            String requestId, int priority) {

        if (priority < 1 || priority > 5) {
            throw new IllegalArgumentException(
                    "Priority must be between 1 and 5"
            );
        }

        if (!requests.containsKey(requestId)) {
            throw new IllegalArgumentException(
                    "Request not found"
            );
        }

        manualPriorities.put(requestId, priority);

        reprioritiseRequests();
    }

    public int getEffectivePriority(
            ReplenishmentRequest request) {

        String id = request.getRequestId();

        return effectivePriorities.getOrDefault(
                id,
                manualPriorities.getOrDefault(
                        id,
                        request.getPriority()
                )
        );
    }

    public void reprioritiseRequests() {

        LocalDateTime now = LocalDateTime.now();

        for (ReplenishmentRequest request : requests.values()) {

            String id = request.getRequestId();

            int score = manualPriorities.getOrDefault(
                    id,
                    request.getPriority()
            );

            if (request.getExpectedDemand() > 200) {
                score += 1;
            }

            if (request.getCurrentStock() < 10) {
                score += 1;
            }

            long hours = ChronoUnit.HOURS.between(
                    now,
                    request.getDeadline()
            );

            if (hours <= 24) {
                score += 1;
            }

            effectivePriorities.put(
                    id,
                    Math.min(score, 8)
            );
        }

        rebuildQueue();
    }

    private void rebuildQueue() {

        queue.clear();

        for (ReplenishmentRequest request : requests.values()) {

            if (!request.isFulfilled()) {
                queue.offer(request);
            }
        }
    }

    public ReplenishmentRequest getNextRequest() {

        ReplenishmentRequest request = queue.poll();

        if (request != null) {

            String id = request.getRequestId();

            requests.remove(id);
            inProgress.put(id, request);
        }

        return request;
    }

    public ReplenishmentRequest peekNextRequest() {
        return queue.peek();
    }

    public int getPendingRequestCount() {
        return queue.size();
    }

    public boolean hasPendingRequests() {
        return !queue.isEmpty();
    }

    public List<ReplenishmentRequest> getAllRequests() {

        PriorityQueue<ReplenishmentRequest> copy =
                new PriorityQueue<>(queue);

        List<ReplenishmentRequest> result =
                new ArrayList<>();

        while (!copy.isEmpty()) {
            result.add(copy.poll());
        }

        return result;
    }

    public Map<String, Integer> getPrioritySnapshot() {

        Map<String, Integer> snapshot =
                new HashMap<>();

        for (ReplenishmentRequest request : requests.values()) {

            snapshot.put(
                    request.getRequestId(),
                    getEffectivePriority(request)
            );
        }

        return snapshot;
    }

    public void requeueRequest(
            ReplenishmentRequest request) {

        if (request == null) {
            return;
        }

        String id = request.getRequestId();

        if (requests.containsKey(id)) {
            throw new IllegalArgumentException(
                    "Request is already pending"
            );
        }

        if (request.isFulfilled()) {
            completeRequest(request);
            return;
        }

        ReplenishmentRequest existing =
                inProgress.get(id);

        if (existing != null && existing != request) {
            throw new IllegalArgumentException(
                    "Request ID already in progress"
            );
        }

        inProgress.remove(id);

        requests.put(id, request);
        queue.offer(request);
    }

    public void completeRequest(
            ReplenishmentRequest request) {

        if (request == null) {
            return;
        }

        String id = request.getRequestId();

        if (!request.isFulfilled()) {
            throw new IllegalStateException(
                    "Request is not fully allocated"
            );
        }

        ReplenishmentRequest pending = requests.get(id);
        ReplenishmentRequest active = inProgress.get(id);

        if (pending != null && pending != request
                || active != null && active != request) {
            throw new IllegalArgumentException(
                    "Request ID belongs to another request"
            );
        }

        if (pending == request) {
            queue.remove(request);
            requests.remove(id);
        }

        inProgress.remove(id);
        manualPriorities.remove(id);
        effectivePriorities.remove(id);
    }
}
package ru.practicum.shareit.request.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemRequestRepository implements ItemRequestRepository {

    private final Map<Long, ItemRequest> requests = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public ItemRequest create(ItemRequest r) {
        r.setId(nextId.getAndIncrement());
        requests.put(r.getId(), r);
        return r;
    }

    @Override
    public Collection<ItemRequest> findByRequestorId(Long id) {
        return requests.values().stream()
                .filter(r -> r.getRequestor().getId().equals(id))
                .sorted(Comparator.comparing(ItemRequest::getCreated).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemRequest> findAllOrderByCreatedDesc() {
        return requests.values().stream()
                .sorted(Comparator.comparing(ItemRequest::getCreated).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequest findById(Long id) {
        return requests.get(id);
    }
}
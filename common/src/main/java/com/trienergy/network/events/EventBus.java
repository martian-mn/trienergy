package com.trienergy.network.events;

import java.util.*;
import java.util.function.Consumer;

/**
 * Minimal type-keyed event bus. Used internally by the engine to dispatch
 * {@link com.trienergy.api.events.NetworkChangedEvent} and friends to
 * subscribers (capability bridges in neoforge/ and fabric/, or addon code).
 */
public final class EventBus {
    private final Map<Class<?>, List<Consumer<?>>> subscribers = new HashMap<>();

    public <T> void subscribe(Class<T> eventType, Consumer<T> handler) {
        subscribers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(handler);
    }

    @SuppressWarnings("unchecked")
    public <T> void publish(T event) {
        List<Consumer<?>> handlers = subscribers.get(event.getClass());
        if (handlers == null) return;
        for (Consumer<?> handler : handlers) {
            ((Consumer<T>) handler).accept(event);
        }
    }
}

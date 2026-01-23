package com.studenttracker.service;

import com.google.common.eventbus.EventBus;
import com.studenttracker.service.event.Event;

public class EventBusService 
{
    private static final EventBusService instance = new EventBusService();
    private static final EventBus eventBus = new EventBus();

    public static synchronized EventBusService getInstance() {
        return instance;
    }

    // Publish event
    public void publish(Object event) {
        if (event == null || event instanceof Event == false) {
            return;
        }
        eventBus.post(event);
    }
    
    // Register subscriber
    public void register(Object subscriber) {
        if (subscriber == null) {
            return;
        }
        eventBus.register(subscriber);
    }
    
    // Unregister subscriber
    public void unregister(Object subscriber) {
        if (subscriber == null) {
            return;
        }
        eventBus.unregister(subscriber);
    }
}

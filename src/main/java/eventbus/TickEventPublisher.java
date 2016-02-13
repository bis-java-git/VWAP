package eventbus;

import com.google.common.eventbus.EventBus;
import eventbus.events.TickEvent;

public class TickEventPublisher {

    public EventBus getEventBus() {
        return eventBus;
    }

    private final EventBus eventBus;

    public TickEventPublisher(final String exchange) {
        eventBus = new EventBus(exchange);
    }

    public void postTickEvent(final TickEvent event) {
        eventBus.post(event);
    }
}

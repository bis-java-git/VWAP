package eventbus.subsciber;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import eventbus.events.TickEvent;

import java.util.Queue;

public class TickSubscriber {

    private final Queue<TickEvent> queue;

    public TickSubscriber(
            final EventBus eventBus,
            final Queue<TickEvent> queue) {
        this.queue = queue;
        eventBus.register(this);
    }

    @Subscribe
    public void listen(final TickEvent event) {
        queue.offer(event);
    }
}

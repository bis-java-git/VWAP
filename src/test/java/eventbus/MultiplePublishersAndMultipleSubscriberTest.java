package eventbus;

import controller.TickController;
import controller.TickControllerImpl;
import eventbus.events.EventType;
import eventbus.events.TickEvent;
import eventbus.subsciber.TickSubscriber;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

import static junit.framework.Assert.assertEquals;

public class MultiplePublishersAndMultipleSubscriberTest {

    private final ExecutorService cachedPool = Executors.newCachedThreadPool();
    
    private static final String RIC="BBVA.MC";

    private final TickEvent[] ldnEventArray = {
            new TickEvent(RIC, new BigDecimal("11.1"), 100_00_00, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.2"), 50_00_00, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.3"), 100_00_00, EventType.BUY, System.nanoTime())
    };


    private final TickEvent[] nyEventArray = {
            new TickEvent(RIC, new BigDecimal("11.1"), 100_00_00, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.2"), 50_00_00, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.3"), 100_00_00, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.1"), 100_00_00, EventType.SELL, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.2"), 50_00_00, EventType.SELL, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.3"), 100_00_00, EventType.SELL, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.1"), 100_00_00, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.2"), 50_00_00, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.3"), 100_00_00, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.1"), 100_00_00, EventType.SELL, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.2"), 50_00_00, EventType.SELL, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.3"), 100_00_00, EventType.SELL, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.1"), 100_00_00, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.2"), 50_00_00, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.3"), 100_00_00, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.1"), 100_00_00, EventType.SELL, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.2"), 50_00_00, EventType.SELL, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.3"), 100_00_00, EventType.SELL, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.1"), 100_00_00, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.2"), 50_00_00, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.3"), 100_00_00, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.1"), 100_00_00, EventType.SELL, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.2"), 50_00_00, EventType.SELL, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.3"), 100_00_00, EventType.SELL, System.nanoTime())
    };

    private final TickEvent[] hkEventArray = {
            new TickEvent(RIC, new BigDecimal("11.1"), 100_00_00, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.2"), 50_00_00, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.3"), 100_00_00, EventType.BUY, System.nanoTime())
    };

    private class CallableThread implements Callable<Void> {

        private final TickEventPublisher tickEventPublisher;

        private final List<TickEvent> eventList;

        public CallableThread(final TickEventPublisher tickEventPublisher, final List<TickEvent> eventList) {
            this.tickEventPublisher = tickEventPublisher;
            this.eventList = eventList;
        }

        @Override
        public Void call() {
            eventList.forEach(tickEventPublisher::postTickEvent);
            return null;
        }
    }

    @Test
    public void shouldReceiveEvent() throws Exception {

        //given
        Queue<TickEvent> queue = new ConcurrentLinkedQueue<>();
        // given with multiple publishers and multiple subscribers

        //NW24 Publisher
        TickEventPublisher nw24Publisher = new TickEventPublisher("MARKITS");
        new TickSubscriber(nw24Publisher.getEventBus(), queue);

        //Bloomeberg Publisher
        TickEventPublisher bloombergPublisher = new TickEventPublisher("BLOOMBERG");
        new TickSubscriber(bloombergPublisher.getEventBus(), queue);

        //Reuters Publisher
        TickEventPublisher reutersPublisher = new TickEventPublisher("REUTERS");
        new TickSubscriber(reutersPublisher.getEventBus(), queue);

        // when
        List<Callable<Void>> tasks = new ArrayList<>();

        tasks.add(new CallableThread(nw24Publisher, Arrays.asList(ldnEventArray)));
        tasks.add(new CallableThread(bloombergPublisher, Arrays.asList(nyEventArray)));
        tasks.add(new CallableThread(reutersPublisher, Arrays.asList(hkEventArray)));


        cachedPool.invokeAll(tasks);

        TickController tickController = new TickControllerImpl(queue);
        tickController.process();

        //then
        assertEquals(new Integer(30), tickController.getAtomicCounter());

        cachedPool.shutdown();
    }
}
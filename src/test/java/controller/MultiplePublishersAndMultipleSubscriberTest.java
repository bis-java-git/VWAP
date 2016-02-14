package controller;

import eventbus.TickEventPublisher;
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
import static junit.framework.Assert.assertFalse;

public class MultiplePublishersAndMultipleSubscriberTest {

    private final ExecutorService cachedPool = Executors.newCachedThreadPool();

    private static final String RIC = "BBVA.MC";

    private final TickEvent[] ldnEventArray = {
            new TickEvent(RIC, new BigDecimal("11.1234"), 100_00_00L, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.2345"), 50_00_00L, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.3456"), 100_00_00L, EventType.BUY, System.nanoTime())
    };


    private final TickEvent[] nyEventArray = {
            new TickEvent(RIC, new BigDecimal("11.1234"), 100_00_00L, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.2345"), 50_00_00L, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.3456"), 100_00_00L, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.1234"), 100_00_00L, EventType.SELL, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.2345"), 50_00_00L, EventType.SELL, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.3456"), 100_00_00L, EventType.SELL, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("12.1234"), 100_00_00L, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("12.2345"), 50_00_00L, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("12.3456"), 100_00_00L, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("12.0897"), 100_00_00L, EventType.SELL, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.2345"), 50_00_00L, EventType.SELL, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.3456"), 100_00_00L, EventType.SELL, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.1234"), 100_00_00L, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("12.2345"), 50_00_00L, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.3456"), 100_00_00L, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("13.1344"), 100_00_00L, EventType.SELL, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("12.2456"), 50_00_00L, EventType.SELL, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.3345"), 100_00_00L, EventType.SELL, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.1234"), 100_00_00L, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.2456"), 50_00_00L, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.3234"), 100_00_00L, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.1234"), 100_00_00L, EventType.SELL, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("12.22222"), 50_00_00L, EventType.SELL, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.33333"), 100_00_00L, EventType.SELL, System.nanoTime())
    };

    private final TickEvent[] hkEventArray = {
            new TickEvent(RIC, new BigDecimal("11.123456"), 100_00_00L, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.234567"), 50_00_00L, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("11.345678"), 100_00_00L, EventType.BUY, System.nanoTime())
    };

    private class CallableThread implements Callable<Void> {

        private final TickEventPublisher tickEventPublisher;

        private final List<TickEvent> eventList;

        public CallableThread(final TickEventPublisher tickEventPublisher,
                              final List<TickEvent> eventList) {
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
    public void shouldReceiveTickEvent() throws InterruptedException {

        //Given
        Queue<TickEvent> queue = new ConcurrentLinkedQueue<>();
        // given with multiple publishers and multiple subscribers
        final TickController tickController = new TickControllerImpl(queue);
        tickController.start();

        //Markits Publisher
        final TickEventPublisher markits = new TickEventPublisher("MARKITS");
        new TickSubscriber(markits.getEventBus(), queue);

        //Bloomeberg Publisher
        final TickEventPublisher bloomsbergPublisher = new TickEventPublisher("BLOOMBERG");
        new TickSubscriber(bloomsbergPublisher.getEventBus(), queue);

        //Reuters Publisher
        final TickEventPublisher reutersPublisher = new TickEventPublisher("REUTERS");
        new TickSubscriber(reutersPublisher.getEventBus(), queue);

        // When
        final List<Callable<Void>> tasks = new ArrayList<>();

        tasks.add(new CallableThread(markits, Arrays.asList(ldnEventArray)));
        tasks.add(new CallableThread(bloomsbergPublisher, Arrays.asList(nyEventArray)));
        tasks.add(new CallableThread(reutersPublisher, Arrays.asList(hkEventArray)));

        cachedPool.invokeAll(tasks);

        tickController.stop();

        //Then
        assertEquals(new Integer(ldnEventArray.length + nyEventArray.length + hkEventArray.length),
                tickController.getTotalMarketDepthPrices());
        assertFalse(tickController.isRunning());
        cachedPool.shutdown();
    }
}
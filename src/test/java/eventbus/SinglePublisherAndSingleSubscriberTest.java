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
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static junit.framework.Assert.assertEquals;

public class SinglePublisherAndSingleSubscriberTest {


    private static final String RIC_BBVA = "BBVA.MC";

    private static final String RIC_HSBC = "HSBA.L";

    private static final String RIC_BT = "BT.L";

    private final ExecutorService cachedPool = Executors.newCachedThreadPool();

    private final TickEvent[] nw24EventArray = {
            new TickEvent(RIC_BBVA, new BigDecimal("11.1"), 100_00_00, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC_BBVA, new BigDecimal("10.2"), 50_00_00, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC_BBVA, new BigDecimal("9.3"), 100_00_00, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC_BBVA, new BigDecimal("8.1"), 100_00_00, EventType.SELL, System.nanoTime()),
            new TickEvent(RIC_BBVA, new BigDecimal("7.2"), 50_00_00, EventType.SELL, System.nanoTime()),
            new TickEvent(RIC_BBVA, new BigDecimal("15.3"), 100_00_00, EventType.SELL, System.nanoTime()),
            new TickEvent(RIC_BBVA, new BigDecimal("20.1"), 0, EventType.NONE, System.nanoTime()),
            new TickEvent(RIC_BBVA, new BigDecimal("5.2"), 0, EventType.NONE, System.nanoTime()),
            new TickEvent(RIC_BBVA, new BigDecimal("30.3"), 0, EventType.NONE, System.nanoTime())
    };


    private final TickEvent[] bloombergEventArray = {
            new TickEvent(RIC_HSBC, new BigDecimal("10.1"), 100_00_00, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC_HSBC, new BigDecimal("15.2"), 50_00_00, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC_HSBC, new BigDecimal("20.3"), 100_00_00, EventType.BUY, System.nanoTime())
    };

    private final TickEvent[] reutersEventArray = {
            new TickEvent(RIC_BT, new BigDecimal("20"), 100_0, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC_BT, new BigDecimal("30"), 100_0, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC_BT, new BigDecimal("50"), 100_0, EventType.BUY, System.nanoTime())
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
        final Queue<TickEvent> queue = new ConcurrentLinkedQueue<>();
        final TickEventPublisher nw24PublisherWithLdInstrument = new TickEventPublisher("MARKITS");
        final TickEventPublisher nw24PublisherWithBtInstrument = new TickEventPublisher("BLOOMSBERG");
        final TickEventPublisher nw24PublisherWithBgInstrument = new TickEventPublisher("REUTERS");
        new TickSubscriber(nw24PublisherWithLdInstrument.getEventBus(), queue);
        new TickSubscriber(nw24PublisherWithBtInstrument.getEventBus(), queue);
        new TickSubscriber(nw24PublisherWithBgInstrument.getEventBus(), queue);

        // when
        final List<CallableThread> tasks = new ArrayList<>();

        tasks.add(new CallableThread(nw24PublisherWithLdInstrument, Arrays.asList(nw24EventArray)));
        tasks.add(new CallableThread(nw24PublisherWithBtInstrument, Arrays.asList(bloombergEventArray)));
        tasks.add(new CallableThread(nw24PublisherWithBgInstrument, Arrays.asList(reutersEventArray)));

        cachedPool.invokeAll(tasks);

        TickController tickController = new TickControllerImpl(queue);
        tickController.process();

        //then
        assertEquals(new Integer(15), tickController.getAtomicCounter());
        cachedPool.shutdown();
    }
}

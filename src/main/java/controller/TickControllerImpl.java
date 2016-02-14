package controller;

import eventbus.events.TickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.VWAPServiceImpl;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TickControllerImpl implements TickController {

    final static Logger logger = (Logger) LoggerFactory.getLogger(TickControllerImpl.class);

    private final Queue<TickEvent> queue;

    private final AtomicInteger atomicCounter = new AtomicInteger(0);

    private final AtomicBoolean keepRunning = new AtomicBoolean(true);

    //Assumption is that in the future it will be injected by Spring/JEE/Guice
    private final VWAPServiceImpl vwapService = new VWAPServiceImpl();

    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    public Integer getTotalMarketDepthPrices() {
        return atomicCounter.get();
    }

    public TickControllerImpl(final Queue<TickEvent> queue) {
        this.queue = queue;
    }

    public void start() throws InterruptedException {
        process();
    }

    @Override
    public void stop() {
        keepRunning.set(false);
        executor.shutdown();
    }

    @Override
    public Boolean isRunning() {
        return keepRunning.get();
    }

    private void process() throws InterruptedException {
        Callable<Void> task = () -> {
            while (keepRunning.get()) {
                if (queue.size() != 0) {
                    final TickEvent event = queue.poll();
                    vwapService.addTick(event);
                    atomicCounter.getAndIncrement();
                    logger.info(event.toString());
                    logger.info(vwapService.getVWAPPrice(event.getInstrument()).toString());
                }
            }
            return null;
        };
        executor.submit(task);
    }
}

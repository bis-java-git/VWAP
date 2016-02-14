package controller;

import eventbus.events.TickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.VWAPServiceImpl;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TickControllerImpl implements TickController {

    private final static Logger logger = (Logger) LoggerFactory.getLogger(TickControllerImpl.class);

    private final Queue<TickEvent> queue;

    private final AtomicInteger atomicCounter = new AtomicInteger(0);

    private final AtomicBoolean keepRunning = new AtomicBoolean(true);

    //Assumption is that in the future it will be injected by Spring/JEE/Guice autowired
    private final VWAPServiceImpl vwapService = new VWAPServiceImpl();

    private ExecutorService mainExecutor;

    private ExecutorService tickEventExecutor;

    public Integer getTotalMarketDepthPrices() {
        return atomicCounter.get();
    }

    public TickControllerImpl(final Queue<TickEvent> queue) {
        this.queue = queue;
        mainExecutor = Executors.newFixedThreadPool(1);
        tickEventExecutor = Executors.newFixedThreadPool(1);
    }

    public void start() throws InterruptedException {
        process();
    }

    @Override
    public void stop() throws InterruptedException {
        mainExecutor.shutdown();
        mainExecutor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        keepRunning.set(false);
        tickEventExecutor.shutdown();
    }

    @Override
    public Boolean isRunning() {
        return keepRunning.get();
    }

    //Assumption here is that we may want to do further execution of trade
    //can be easily expanded to take other processes with little modification
    //hence further thread is spawn per event
    private void tickEventProcess(final TickEvent tickEvent) {

        Callable<Boolean> tickEventTask = () -> {
            vwapService.addTick(tickEvent);
            atomicCounter.getAndIncrement();
            logger.info(tickEvent.toString());
            logger.info(String.valueOf(vwapService.getVWAPPrice(tickEvent.getInstrument())));
            return true;
        };
        tickEventExecutor.submit(tickEventTask);
    }

    //Keep streaming
    private void process() throws InterruptedException {

        Callable<Boolean> task = () -> {
            while (keepRunning.get()) {
                if (queue.size() != 0) {
                    tickEventProcess(queue.poll());
                }
            }
            return true;
        };
        mainExecutor.submit(task);
    }
}

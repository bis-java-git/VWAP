package controller;

public interface TickController {

    void start() throws InterruptedException;

    void stop() throws InterruptedException;

    Boolean isRunning();

    Integer getTotalMarketDepthPrices();
}

package controller;

public interface TickController {

    void start() throws InterruptedException;

    void stop();

    Boolean isRunning();

    Integer getTotalMarketDepthPrices();
}

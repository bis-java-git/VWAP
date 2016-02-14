package controller;

public interface TickController {

    void start() throws InterruptedException;

    void stop();

    String getStatus();

    Integer getAtomicCounter();
}

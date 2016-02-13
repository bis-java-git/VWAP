package controller;

public interface TickController {

    void stop();

    void process() throws InterruptedException;

    Integer getAtomicCounter();
}

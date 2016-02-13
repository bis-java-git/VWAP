package service;


import domain.VWAPPrice;
import eventbus.events.TickEvent;

public interface VWAPService {

    VWAPPrice getVWAPPrice(final String instrument);

    void addTick(final TickEvent event);
}

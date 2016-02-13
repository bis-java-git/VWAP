package service;

import domain.VWAPPrice;
import eventbus.events.EventType;
import eventbus.events.TickEvent;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class VWAPServiceTest {

    private static final double DELTA = 1e-5;

    private static String RIC = "bbva.mc";

    private static Integer VOLUME  = 100_0;

    private VWAPService vwapService = new VWAPServiceImpl();

    private final TickEvent[] reutersBuyEventArray = {
            new TickEvent(RIC, new BigDecimal("20"), VOLUME, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("30"), VOLUME, EventType.BUY, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("50"), VOLUME, EventType.BUY, System.nanoTime())
    };


    private final TickEvent[] reutersSellEventArray = {
            new TickEvent(RIC, new BigDecimal("100"), VOLUME, EventType.SELL, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("200"), VOLUME, EventType.SELL, System.nanoTime()),
            new TickEvent(RIC, new BigDecimal("300"), VOLUME, EventType.SELL, System.nanoTime())
    };

    private final double[] EXPECTED_BUY_PRICES = {20.00, 25.00, 33.333333};

    private final double[] EXPECTED_SELL_PRICES = {100.00, 150.00, 200.00};

    @Test
    public void getVWAPPriceBuyTest() {
        int i = 0;
        for (TickEvent event : reutersBuyEventArray) {
            vwapService.addTick(event);
            VWAPPrice price = vwapService.getVWAPPrice(event.getInstrument());
            assertEquals(RIC, price.getTicker());
            assertEquals(EXPECTED_BUY_PRICES[i++], price.getBuyPrice().doubleValue(), DELTA);
        }
    }

    @Test
    public void getVWAPPriceSellTest() {
        int i = 0;
        for (TickEvent event : reutersSellEventArray) {
            vwapService.addTick(event);
            VWAPPrice price = vwapService.getVWAPPrice(event.getInstrument());
            assertEquals(RIC, price.getTicker());
            assertEquals(EXPECTED_SELL_PRICES[i++], price.getSellPrice().doubleValue(), DELTA);
        }
    }


}

package service;

import domain.VWAPPrice;
import eventbus.events.EventType;
import eventbus.events.TickEvent;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

public class VWAPServiceImpl implements VWAPService {

    private static final int DECIMAL_PLACES = 6;

    //Assumptions made that all the prices in the map
    //Map needs to be cleared at some time
    //price expiry period needs to be set, which will be enhancement later.
    private final ConcurrentMap<String, Queue<TickEvent>> marketDepthMap = new ConcurrentHashMap<>();

    private List<TickEvent> getALlEvents(final String instrument,
                                         final EventType eventType) {
        return marketDepthMap.get(instrument).parallelStream().filter(instrumentPredicate(instrument, eventType)).collect(toList());
    }

    private Predicate<TickEvent> instrumentPredicate(final String instrument,
                                                     final EventType eventType) {
        return tickEvent -> tickEvent.getEventType() == eventType && Objects.equals(tickEvent
                .getInstrument(), instrument);
    }

    //Following assumptions made
    //Both prices are calculated at the same time buy and sell
    //Initial price for buy/or sale is zero, needs to be fixed later.
    private BigDecimal getPrice(final String instrument,
                                final EventType event) {
        final BigDecimal totalVolumePrice = getALlEvents(instrument, event).
                parallelStream().map((tickEvent) ->
                new BigDecimal(String.valueOf(tickEvent.getVolume())).multiply(tickEvent.getPrice())).
                reduce(BigDecimal.ZERO, BigDecimal::add);

        final Long totalVolume = getTotalVolume(instrument, event);

        if (totalVolume == 0) {
            return BigDecimal.ZERO;
        }
        return totalVolumePrice.divide(new BigDecimal(totalVolume), MathContext.DECIMAL128).
                setScale(DECIMAL_PLACES, BigDecimal.ROUND_HALF_EVEN);
    }

    private Long getTotalVolume(final String instrument,
                                final EventType event) {
        return getALlEvents(instrument, event).
                parallelStream().mapToLong(TickEvent::getVolume).sum();
    }

    @Override
    public VWAPPrice getVWAPPrice(final String instrument) {
        final BigDecimal buyVolumePrice = getPrice(instrument, EventType.BUY);
        final BigDecimal sellVolumePrice = getPrice(instrument, EventType.SELL);
        return new VWAPPrice(buyVolumePrice, sellVolumePrice, instrument);
    }

    @Override
    public void addTick(final TickEvent event) {
        Queue<TickEvent> list = marketDepthMap.get(event.getInstrument());
        if (list == null) {
            list = new ConcurrentLinkedQueue<>();
            marketDepthMap.put(event.getInstrument(), list);
        }
        list.add(event);
    }
}

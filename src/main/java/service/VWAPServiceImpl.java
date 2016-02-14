package service;

import domain.VWAPPrice;
import eventbus.events.EventType;
import eventbus.events.TickEvent;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

public class VWAPServiceImpl implements VWAPService {

    private static final int DECIMAL_PLACES = 6;

    private final ReentrantReadWriteLock vwapLock = new ReentrantReadWriteLock();

    private final ReentrantReadWriteLock.ReadLock vwapReadLock = vwapLock.readLock();

    private final ReentrantReadWriteLock.WriteLock vwapWriteLock = vwapLock.writeLock();

    //Assumptions made that all the prices in the map
    //Map needs to be cleared or pricing routine needs to check for time between say 16.00 and 17.00
    //not taken in account
    //price expiry period needs to be set, which will be enhancement later.
    private final ConcurrentMap<String, List<TickEvent>> marketDepthMap = new ConcurrentHashMap<>();

    private List<TickEvent> getALlEvents(final String instrument, final EventType eventType) {
        return marketDepthMap.get(instrument).parallelStream().filter(instrumentPredicate(instrument, eventType)).collect(toList());
    }

    private Predicate<TickEvent> instrumentPredicate(final String instrument, final EventType eventType) {
        return i -> i.getEventType() == eventType && Objects.equals(i.getInstrument(), instrument);
    }

    //Following assumptions made
    //Both prices are calculated at the same time buy and sell
    //Initial price for buy/or sale is zero, needs to be fixed later.
    private BigDecimal getPrice(final String instrument, final EventType event) {
        BigDecimal totalVolumePrice = getALlEvents(instrument, event).
                parallelStream().map((i) -> new BigDecimal((i.getVolume() * i.getPrice().doubleValue()))).
                reduce(BigDecimal.ZERO, BigDecimal::add);

        Long totalVolume = getTotalVolume(instrument, event);

        if (totalVolume==0) {
            return BigDecimal.ZERO;
        }
        return totalVolumePrice.divide(BigDecimal.valueOf(getTotalVolume(instrument, event)), MathContext.DECIMAL128).
                setScale(DECIMAL_PLACES, BigDecimal.ROUND_HALF_EVEN);
    }

    private Long getTotalVolume(final String instrument, final EventType event) {
        return getALlEvents(instrument, event).
                parallelStream().mapToLong(TickEvent::getVolume).sum();
    }

    @Override
    public VWAPPrice getVWAPPrice(final String instrument) {
        vwapReadLock.lock();
        final BigDecimal buyVolumePrice = getPrice(instrument, EventType.BUY);
        final BigDecimal sellVolumePrice = getPrice(instrument, EventType.SELL);
        VWAPPrice price = new VWAPPrice(buyVolumePrice, sellVolumePrice, instrument);
        vwapReadLock.unlock();
        return price;
    }

    @Override
    public void addTick(TickEvent event) {
        vwapWriteLock.lock();
        List<TickEvent> list = marketDepthMap.get(event.getInstrument());
        if (list == null) {
            list = new LinkedList<>();
            marketDepthMap.put(event.getInstrument(), list);
        }
        list.add(event);
        vwapWriteLock.unlock();
    }
}

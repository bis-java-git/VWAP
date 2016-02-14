package eventbus.events;

import java.math.BigDecimal;
import java.util.Date;

public final class TickEvent {

    private final BigDecimal price;

    private final Long timeStamp;

    private final EventType eventType;

    private final Integer volume;

    private final String instrument;

    public BigDecimal getPrice() {
        return price;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Integer getVolume() {
        return volume;
    }

    public String getInstrument() {
        return instrument;
    }

    public TickEvent(
            final String instrument,
            final BigDecimal price,
            final Integer volume,
            final EventType eventType,
            final Long timeStamp) {
        this.price = price;
        this.volume = volume;
        this.timeStamp = timeStamp;
        this.eventType = eventType;
        this.instrument = instrument;
    }

    @Override
    public String toString() {
        final String volumeString = (eventType == EventType.NONE) ? "" : "volume = " + volume;
        StringBuilder builder = new StringBuilder();
        builder.append(instrument).append(" ");
        if (eventType == EventType.BUY) {
            builder.append(" Buy ").append(volumeString).append(" ").append(price).append(" ");
        } else if (eventType == EventType.SELL) {
            builder.append(" ").append(price).append(" Sell ").append(volumeString).append(" ");
        } else if (eventType == EventType.NONE) {
            builder.append(price).append(" ");
        }
        builder.append(new Date(timeStamp));
        return builder.toString();
    }
}

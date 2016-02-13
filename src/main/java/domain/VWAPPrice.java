package domain;

import java.math.BigDecimal;
import java.util.Date;

public final class VWAPPrice {

    private final BigDecimal buyPrice;

    private final BigDecimal sellPrice;

    private final String ticker;

    private final Long timeStamp;

    public VWAPPrice(final BigDecimal buyPrice,
                     final BigDecimal sellPrice,
                     final String ticker) {
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.ticker = ticker;
        timeStamp = System.nanoTime();
    }

    public BigDecimal getBuyPrice() {
        return buyPrice;
    }

    public BigDecimal getSellPrice() {
        return sellPrice;
    }

    public String getTicker() {
        return ticker;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("VWAPPrice{").append("buyPrice=").append(buyPrice).append(", sellPrice=").append(sellPrice).append(", ticker='").append(ticker);
        builder.append(", Time= ").append(new Date(timeStamp));
        return builder.toString();
    }
}

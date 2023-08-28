package works.weave.socks.orders.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class OrdersHelper {
    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private Map<String, Integer> eventToCount = new HashMap<>();

    public double applyDiscountIfRelevant(double orderPrice){
        if (orderPrice >= 10 && orderPrice <= 50){
            LOG.info("#### Applying discount");
            return 0.9*orderPrice;
        }

        LOG.info("#### Didn't apply discount");
        return orderPrice;
    }

    public void collectStatistics(String event){
        Integer count = eventToCount.get(event);
        if (count == null)
            count =0;
        count++;

        LOG.info("#### Collecting Stats. EventName:'" + event + "', count: " +  count);
        eventToCount.put(event, count);
    }
}

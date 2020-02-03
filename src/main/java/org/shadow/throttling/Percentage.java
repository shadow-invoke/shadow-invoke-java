package org.shadow.throttling;

import java.util.concurrent.ThreadLocalRandom;

public class Percentage implements Throttle {
    private final double percentage;

    public Percentage(double percentage) {
        if(percentage <= 1.0D && percentage >= 0.0D) {
            this.percentage = percentage;
        } else if(percentage <= 100.0D && percentage >= 0.0D) {
            this.percentage = percentage / 100.0D;
        } else {
            throw new IllegalArgumentException(String.format("Not a percentage: %f", percentage));
        }
    }

    @Override
    public boolean reject() {
        return ThreadLocalRandom.current().nextDouble() >= this.percentage;
    }
}

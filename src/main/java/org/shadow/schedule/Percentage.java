package org.shadow.schedule;

import java.util.Random;

public class Percentage implements Throttle {
    private final double percentage;
    private final Random random = new Random();

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
    public boolean accept() {
        return this.random.nextDouble() > this.percentage;
    }
}

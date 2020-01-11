package org.shadow.schedule;

import org.shadow.invocation.Transcript;

import java.util.Random;

public class PercentageSchedule implements Schedule {
    private final double percentage;
    private final Random random = new Random();

    public PercentageSchedule(double percentage) {
        if(percentage <= 1.0D && percentage >= 0.0D) {
            this.percentage = percentage;
        } else if(percentage <= 100.0D && percentage >= 0.0D) {
            this.percentage = percentage / 100.0D;
        } else {
            throw new IllegalArgumentException(String.format("Not a percentage: %d", percentage));
        }
    }

    @Override
    public boolean accept(Transcript transcript) {
        return this.random.nextDouble() > this.percentage;
    }
}

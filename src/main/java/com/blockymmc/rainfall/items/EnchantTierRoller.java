package com.blockymmc.rainfall.items;

import java.util.concurrent.ThreadLocalRandom;

public final class EnchantTierRoller {

    // Index 0 = tier 1, index 1 = tier 2, etc.
    private static final int[] WEIGHTS_5  = {15, 36, 36, 10,  3};
    private static final int[] WEIGHTS_10 = {10, 17, 17, 17, 17,  8,  6,  5,  4,  2};

    private EnchantTierRoller() {}

    public static int roll(int maxTier) {
        int[] weights = switch (maxTier) {
            case 5  -> WEIGHTS_5;
            case 10 -> WEIGHTS_10;
            default -> throw new IllegalArgumentException("No odds table defined for max tier: " + maxTier);
        };
        return weightedRoll(weights);
    }

    private static int weightedRoll(int[] weights) {
        int total = 0;
        for (int w : weights) total += w;
        int roll = ThreadLocalRandom.current().nextInt(total);
        int cumulative = 0;
        for (int i = 0; i < weights.length; i++) {
            cumulative += weights[i];
            if (roll < cumulative) return i + 1;
        }
        return weights.length;
    }
}

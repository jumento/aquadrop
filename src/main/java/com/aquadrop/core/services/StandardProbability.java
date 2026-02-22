package com.aquadrop.core.services;

import java.util.concurrent.ThreadLocalRandom;

public class StandardProbability implements ProbabilityService {
    @Override
    public boolean shouldDrop(float probability) {
        if (probability <= 0.0f)
            return false;
        if (probability >= 100.0f)
            return true;

        // Uso de ThreadLocalRandom por rendimiento y seguridad en multihilo concurrente
        return (ThreadLocalRandom.current().nextFloat() * 100.0f) <= probability;
    }
}

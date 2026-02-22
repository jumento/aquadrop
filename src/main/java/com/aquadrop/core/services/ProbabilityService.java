package com.aquadrop.core.services;

public interface ProbabilityService {
    /**
     * Evalúa la probabilidad dada y retorna true si un dropeo se cumple.
     * 
     * @param probability Valor entre 0.0f y 100.0f.
     */
    boolean shouldDrop(float probability);
}

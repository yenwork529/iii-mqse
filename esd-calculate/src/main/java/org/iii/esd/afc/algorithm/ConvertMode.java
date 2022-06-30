package org.iii.esd.afc.algorithm;

public class ConvertMode {

    private Strategy strategy;

    public ConvertMode(Strategy strategy) {
        this.strategy = strategy;
    }

    public Double execute() {
        return strategy.execute();
    }
}

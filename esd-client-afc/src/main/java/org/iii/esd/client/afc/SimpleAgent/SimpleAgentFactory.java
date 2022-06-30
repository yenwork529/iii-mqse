package org.iii.esd.client.afc.SimpleAgent;

public class SimpleAgentFactory {
    public static ISimpleAgent<Integer> getDefault() {
        return new SimpleAgent();
    }
}

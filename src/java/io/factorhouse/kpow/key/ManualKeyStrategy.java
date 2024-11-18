package io.factorhouse.kpow.key;

public class ManualKeyStrategy implements KeyStrategy {
    private final String envName;

    public ManualKeyStrategy(String envName) {
        this.envName = envName;
    }

    @Override
    public Taxon getTaxon(String clientId, String applicationId) {
        return new Taxon("env", envName, "streams-agent-m", clientId);
    }
}

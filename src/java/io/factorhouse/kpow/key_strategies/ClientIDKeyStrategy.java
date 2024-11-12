package io.factorhouse.kpow.key_strategies;

public class ClientIDKeyStrategy implements KeyStrategy {
    public ClientIDKeyStrategy() {}

    @Override
    public Taxon getTaxon(String clientId, String applicationId) {
        return new Taxon("streams", clientId, "streams-agent", null);
    }
}

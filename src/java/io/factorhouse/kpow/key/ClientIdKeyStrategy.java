package io.factorhouse.kpow.key;

public class ClientIdKeyStrategy implements KeyStrategy {
    public ClientIdKeyStrategy() {}

    @Override
    public Taxon getTaxon(String clientId, String applicationId) {
        return new Taxon("streams", clientId, "streams-agent", null);
    }
}

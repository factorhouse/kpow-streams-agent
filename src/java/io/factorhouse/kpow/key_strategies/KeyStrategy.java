package io.factorhouse.kpow.key_strategies;

public interface KeyStrategy {
    Taxon getTaxon(String clientId, String applicationId);
}

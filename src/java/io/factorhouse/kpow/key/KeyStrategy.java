package io.factorhouse.kpow.key;

public interface KeyStrategy {
    Taxon getTaxon(String clientId, String applicationId);
}

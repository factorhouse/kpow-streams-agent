package io.factorhouse.kpow.key;

/**
 * Defines the key strategy used by Kpow's streams agent.
 *
 * This interface specifies how metrics data should be keyed when writing to Kpow's internal snapshots topic.
 */
public interface KeyStrategy {
    /**
     * Resolves the unique key for metric records based on the provided clientID and applicationID of the registered Kafka Streams application.
     *
     * @param clientId   The client ID of the registered streams application.
     * @param applicationId The application ID of the registered Kafka streams application.
     * @return The unique Taxon object representing the key.
     */
    Taxon getTaxon(String clientId, String applicationId);
}

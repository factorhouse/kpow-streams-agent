package io.factorhouse.kpow.key;

/**
 * This key strategy relies on the client ID and application ID from the active KafkaStreams instance, eliminating the need for an AdminClient.
 * <p>
 * However, in a multi-cluster Kpow deployment where the same application ID is used across multiple environments (e.g., staging, dev, prod),
 * Kpow cannot determine which cluster the Kafka Streams instance is associated with.
 * </p>
*/
public class ClientIdKeyStrategy implements KeyStrategy {
    public ClientIdKeyStrategy() {}

    @Override
    public Taxon getTaxon(String clientId, String applicationId) {
        return new Taxon("streams", clientId, "streams-agent", null);
    }
}

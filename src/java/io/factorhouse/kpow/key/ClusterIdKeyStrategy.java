package io.factorhouse.kpow.key;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * The default key strategy uses the cluster ID, obtained via an {@link org.apache.kafka.clients.admin.Admin#describeCluster()}  call.
 * This AdminClient is created once during registry initialization and then closed.
 */
public class ClusterIdKeyStrategy implements KeyStrategy {
    private final String clusterId;

    public ClusterIdKeyStrategy(Properties props) throws InterruptedException, ExecutionException {
        try (AdminClient adminClient = AdminClient.create(props)) {
            DescribeClusterResult describeClusterResult = adminClient.describeCluster();
            this.clusterId = describeClusterResult.clusterId().get();
        }
    }

    @Override
    public Taxon getTaxon(String clientId, String applicationId) {
        return new Taxon("cluster", clusterId, "streams-agent", clientId);
    }
}

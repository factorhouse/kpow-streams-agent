package io.factorhouse.kpow.key;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

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

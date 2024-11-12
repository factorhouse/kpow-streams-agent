package io.factorhouse.kpow.key_strategies;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class ClusterIDKeyStrategy implements KeyStrategy {
    private final String clusterID;

    public ClusterIDKeyStrategy(Properties props) throws InterruptedException, ExecutionException {
        try (AdminClient adminClient = AdminClient.create(props)) {
            DescribeClusterResult describeClusterResult = adminClient.describeCluster();
            this.clusterID = describeClusterResult.clusterId().get();
        }
    }

    @Override
    public Taxon getTaxon(String clientId, String applicationId) {
        return new Taxon("streams", clusterID, "streams-agent-cid", clientId);
    }
}

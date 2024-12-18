package io.factorhouse.kpow;

import org.apache.kafka.common.MetricName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class MetricFilter {

    private String filterId = null;

    public String getFilterId() {
        return filterId;
    }

    public enum FilterType {
        ACCEPT, DENY
    }

    public static class FilterCriteria {
        private final Predicate<MetricName> predicate;
        private final FilterType filterType;

        // Constructor to initialize both fields
        private FilterCriteria(Predicate<MetricName> predicate, FilterType filterType) {
            this.predicate = predicate;
            this.filterType = filterType;
        }

        public Predicate<MetricName> getPredicate() {
            return predicate;
        }

        public FilterType getFilterType() {
            return filterType;
        }
    }

    private final List<FilterCriteria> filters;

    public MetricFilter() {
        this.filters = new ArrayList<>();
        this.filterId = "custom";
    }

    private MetricFilter(String id) {
        this.filters = new ArrayList<>();
        this.filterId = id;
    }

    /**
     * Returns a metrics filter that accepts all numeric metrics from the running Streams application.
     *
     * @return accept all metric filter
     */
    public static MetricFilter acceptAllMetricFilter() {
        return new MetricFilter("acceptAll").accept();
    }

    /**
     * Returns a metrics filter that denies all metrics, only sending across the Kafka Streams topology + state
     * on every observation.
     *
     * @return deny all metric filter
     */
    public static MetricFilter denyAllMetricFilter() {
        return new MetricFilter("denyAll").deny();
    }

    /**
     * Returns a metrics filter that includes only state store metrics.
     *
     * @return state store metrics only filter
     */
    public static MetricFilter stateStoreMetricsOnlyFilter() {
        Predicate<MetricName> stateStoreMetricsOnly = m ->
                m.tags().containsKey("store") ||
                        m.tags().containsKey("in-memory-state-id") ||
                        m.tags().containsKey("in-memory-window-state-id") ||
                        m.tags().containsKey("in-memory-session-state-id") ||
                        m.tags().containsKey("rocksdb-session-state-id") ||
                        m.tags().containsKey("rocksdb-state-id") ||
                        m.tags().containsKey("rocksdb-window-state-id");
        return new MetricFilter("stateStoreMetricsOnly")
                .accept(stateStoreMetricsOnly);
    }

    /**
     * Returns the default metricsFilter used by the streams agent.
     * By default, Kpow's streams agent will only send across a few key Kafka Streams metrics:
     *  Latency:
     *  - commit-latency-avg
     *  - process-latency-avg
     *  - poll-latecny-avg
     *  Throughput:
     *  - process-rate
     *  - records-processed-rate
     * Lag:
     * - commit-rate
     * - records-lag-max
     * - records-lag
     * Stability:
     * - failed-stream-threads
     * - rebalances
     * State store health:
     * - put-rate
     * - get-rate
     * - flush-rate
     *
     * @return the default metrics filter
     */
    public static MetricFilter defaultMetricFilter() {
        return new MetricFilter("default")
                // Latency
                .acceptNameStartsWith("commit-latency-avg")
                .acceptNameStartsWith("process-latency-avg")
                .acceptNameStartsWith("poll-latency-avg")
                // Throughput
                .acceptNameStartsWith("process-rate")
                .acceptNameStartsWith("records-processed-rate")
                // Lag
                .acceptNameStartsWith("commit-rate")
                .acceptNameStartsWith("records-lag-max")
                .acceptNameStartsWith("records-lag")
                // Stability
                .acceptNameStartsWith("failed-stream-threads")
                .acceptNameStartsWith("rebalances")
                // State store health
                .acceptNameStartsWith("put-rate")
                .acceptNameStartsWith("get-rate")
                .acceptNameStartsWith("flush-rate");
    }

    public List<FilterCriteria> getFilters() {
        return Collections.unmodifiableList(filters);
    }

    public MetricFilter accept() {
        Predicate<MetricName> acceptPredicate = (_filter) -> {
            return true;
        };
        FilterCriteria criteria = new FilterCriteria(acceptPredicate, FilterType.ACCEPT);
        this.filters.add(criteria);
        return this;
    }

    public MetricFilter accept(Predicate<MetricName> acceptFilter) {
        FilterCriteria criteria = new FilterCriteria(acceptFilter, FilterType.ACCEPT);
        this.filters.add(criteria);
        return this;
    }

    public MetricFilter deny() {
        Predicate<MetricName> denyFilter = (_filter) -> {
            return true;
        };
        FilterCriteria criteria = new FilterCriteria(denyFilter, FilterType.DENY);
        this.filters.add(criteria);
        return this;
    }

    public MetricFilter deny(Predicate<MetricName> denyFilter) {
        FilterCriteria criteria = new FilterCriteria(denyFilter, FilterType.DENY);
        this.filters.add(criteria);
        return this;
    }

    public MetricFilter acceptNameStartsWith(String prefix) {
        Predicate<MetricName> acceptFilter = (metricName) -> {
            return metricName.name().startsWith(prefix);
        };
        FilterCriteria criteria = new FilterCriteria(acceptFilter, FilterType.ACCEPT);
        this.filters.add(criteria);
        return this;
    }

    public MetricFilter denyNameStartsWith(String prefix) {
        Predicate<MetricName> denyFilter = (metricName) -> {
            return metricName.name().startsWith(prefix);
        };
        FilterCriteria criteria = new FilterCriteria(denyFilter, FilterType.DENY);
        this.filters.add(criteria);
        return this;
    }
}


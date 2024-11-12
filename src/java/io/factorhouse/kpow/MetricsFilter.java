package io.factorhouse.kpow;

import org.apache.kafka.common.MetricName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class MetricsFilter {
    private final List<Predicate<MetricName>> filters;

    private MetricsFilter(List<Predicate<MetricName>> filters) {
        this.filters = new ArrayList<>(filters);
    }

    public List<Predicate<MetricName>> getFilters() {
        return Collections.unmodifiableList(filters);
    }

    public static Builder defaultMetricsFilter() {
        return new Builder().addFilter(metricName -> "streams.state".equals(metricName.name()));
    }

    public static Builder emptyMetricsFilter() {
        return new Builder();
    }

    public static class Builder {
        private final List<Predicate<MetricName>> filters = new ArrayList<>();

        public Builder addFilter(Predicate<MetricName> filter) {
            filters.add(filter);
            return this;
        }

        public MetricsFilter build() {
            return new MetricsFilter(filters);
        }
    }
}
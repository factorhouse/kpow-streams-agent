package io.factorhouse.kpow;

import org.apache.kafka.common.MetricName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class MetricFilter {
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
    }

    public List<FilterCriteria> getFilters() {
        return Collections.unmodifiableList(filters);
    }

    public MetricFilter accept() {
        Predicate<MetricName> acceptPredicate = (_filter) -> { return true; };
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
        Predicate<MetricName> denyFilter = (_filter) -> { return true; };
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
        Predicate<MetricName> acceptFilter = (metricName) -> { return metricName.name().startsWith(prefix); };
        FilterCriteria criteria = new FilterCriteria(acceptFilter, FilterType.ACCEPT);
        this.filters.add(criteria);
        return this;
    }

    public MetricFilter denyNameStartsWith(String prefix) {
        Predicate<MetricName> denyFilter = (metricName) -> { return metricName.name().startsWith(prefix); };
        FilterCriteria criteria = new FilterCriteria(denyFilter, FilterType.DENY);
        this.filters.add(criteria);
        return this;
    }
}
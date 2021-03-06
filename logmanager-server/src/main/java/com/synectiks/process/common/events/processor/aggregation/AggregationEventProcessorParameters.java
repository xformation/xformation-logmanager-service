/*
 * */
package com.synectiks.process.common.events.processor.aggregation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.synectiks.process.common.events.processor.EventProcessorParametersWithTimerange;
import com.synectiks.process.server.plugin.indexer.searches.timeranges.AbsoluteRange;
import com.synectiks.process.server.plugin.indexer.searches.timeranges.InvalidRangeParametersException;
import com.synectiks.process.server.plugin.indexer.searches.timeranges.RelativeRange;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@AutoValue
@JsonTypeName(AggregationEventProcessorConfig.TYPE_NAME)
@JsonDeserialize(builder = AggregationEventProcessorParameters.Builder.class)
public abstract class AggregationEventProcessorParameters implements EventProcessorParametersWithTimerange {
    private static final String FIELD_STREAMS = "streams";
    private static final String FIELD_BATCH_SIZE = "batch_size";

    @JsonProperty(FIELD_STREAMS)
    public abstract ImmutableSet<String> streams();

    @JsonProperty(FIELD_BATCH_SIZE)
    public abstract int batchSize();

    @Override
    public EventProcessorParametersWithTimerange withTimerange(DateTime from, DateTime to) {
        requireNonNull(from, "from cannot be null");
        requireNonNull(to, "to cannot be null");
        checkArgument(to.isAfter(from), "to must be after from");

        return toBuilder().timerange(AbsoluteRange.create(from, to)).build();
    }

    public abstract Builder toBuilder();

    public static Builder builder() {
        return Builder.create();
    }

    @AutoValue.Builder
    public static abstract class Builder implements EventProcessorParametersWithTimerange.Builder<Builder> {
        @JsonCreator
        public static Builder create() {
            final RelativeRange timerange;
            try {
                timerange = RelativeRange.create(3600);
            } catch (InvalidRangeParametersException e) {
                // This should not happen!
                throw new RuntimeException(e);
            }

            return new AutoValue_AggregationEventProcessorParameters.Builder()
                    .type(AggregationEventProcessorConfig.TYPE_NAME)
                    .timerange(timerange)
                    .streams(Collections.emptySet())
                    .batchSize(500);
        }

        @JsonProperty(FIELD_STREAMS)
        public abstract Builder streams(Set<String> streams);

        @JsonProperty(FIELD_BATCH_SIZE)
        public abstract Builder batchSize(int batchSize);

        public abstract AggregationEventProcessorParameters build();
    }
}

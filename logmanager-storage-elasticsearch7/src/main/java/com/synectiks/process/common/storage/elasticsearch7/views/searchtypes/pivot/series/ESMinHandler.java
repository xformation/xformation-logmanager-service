/*
 * Copyright (C) 2020 Graylog, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */
package com.synectiks.process.common.storage.elasticsearch7.views.searchtypes.pivot.series;

import com.synectiks.process.common.plugins.views.search.searchtypes.pivot.Pivot;
import com.synectiks.process.common.plugins.views.search.searchtypes.pivot.series.Min;
import com.synectiks.process.common.storage.elasticsearch7.views.ESGeneratedQueryContext;
import com.synectiks.process.common.storage.elasticsearch7.views.searchtypes.pivot.ESPivot;
import com.synectiks.process.common.storage.elasticsearch7.views.searchtypes.pivot.ESPivotSeriesSpecHandler;

import org.graylog.shaded.elasticsearch7.org.elasticsearch.action.search.SearchResponse;
import org.graylog.shaded.elasticsearch7.org.elasticsearch.search.aggregations.AggregationBuilder;
import org.graylog.shaded.elasticsearch7.org.elasticsearch.search.aggregations.AggregationBuilders;
import org.graylog.shaded.elasticsearch7.org.elasticsearch.search.aggregations.metrics.MinAggregationBuilder;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.stream.Stream;

public class ESMinHandler extends ESPivotSeriesSpecHandler<Min, org.graylog.shaded.elasticsearch7.org.elasticsearch.search.aggregations.metrics.Min> {
    @Nonnull
    @Override
    public Optional<AggregationBuilder> doCreateAggregation(String name, Pivot pivot, Min minSpec, ESPivot searchTypeHandler, ESGeneratedQueryContext queryContext) {
        final MinAggregationBuilder min = AggregationBuilders.min(name).field(minSpec.field());
        record(queryContext, pivot, minSpec, name, org.graylog.shaded.elasticsearch7.org.elasticsearch.search.aggregations.metrics.Min.class);
        return Optional.of(min);
    }

    @Override
    public Stream<Value> doHandleResult(Pivot pivot,
                                        Min pivotSpec,
                                        SearchResponse searchResult,
                                        org.graylog.shaded.elasticsearch7.org.elasticsearch.search.aggregations.metrics.Min minAggregation,
                                        ESPivot searchTypeHandler,
                                        ESGeneratedQueryContext esGeneratedQueryContext) {
        return Stream.of(ESPivotSeriesSpecHandler.Value.create(pivotSpec.id(), Min.NAME, minAggregation.getValue()));
    }
}
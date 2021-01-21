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
package com.synectiks.process.common.storage.elasticsearch7.views.searchtypes;

import com.synectiks.process.common.plugins.views.search.Query;
import com.synectiks.process.common.plugins.views.search.SearchJob;
import com.synectiks.process.common.plugins.views.search.SearchType;
import com.synectiks.process.common.plugins.views.search.engine.SearchTypeHandler;
import com.synectiks.process.common.storage.elasticsearch7.views.ESGeneratedQueryContext;

import org.graylog.shaded.elasticsearch7.org.elasticsearch.action.search.SearchResponse;
import org.graylog.shaded.elasticsearch7.org.elasticsearch.search.aggregations.Aggregations;

/**
 * Signature of search type handlers the elasticsearch backend takes.
 * All of these take a {@link ESGeneratedQueryContext} as input.
 *
 * @param <S> the {@link SearchType SearchType} this handler deals with
 */
public interface ESSearchTypeHandler<S extends SearchType> extends SearchTypeHandler<S, ESGeneratedQueryContext, SearchResponse> {
    @Override
    default SearchType.Result doExtractResultImpl(SearchJob job, Query query, S searchType, SearchResponse queryResult, ESGeneratedQueryContext queryContext) {
        // if the search type was filtered, extract the sub-aggregation before passing it to the handler
        // this way we don't have to duplicate this step everywhere
        final Aggregations aggregations = queryResult.getAggregations();
        return doExtractResult(job, query, searchType, queryResult, aggregations, queryContext);
    }

    SearchType.Result doExtractResult(SearchJob job, Query query, S searchType, SearchResponse queryResult, Aggregations aggregations, ESGeneratedQueryContext queryContext);
}

/*
 *
 */
package com.synectiks.process.common.storage.elasticsearch6.views;

import com.google.common.collect.Maps;
import com.google.inject.name.Named;
import com.synectiks.process.common.plugins.views.search.Filter;
import com.synectiks.process.common.plugins.views.search.GlobalOverride;
import com.synectiks.process.common.plugins.views.search.Query;
import com.synectiks.process.common.plugins.views.search.QueryResult;
import com.synectiks.process.common.plugins.views.search.SearchJob;
import com.synectiks.process.common.plugins.views.search.SearchType;
import com.synectiks.process.common.plugins.views.search.elasticsearch.ElasticsearchQueryString;
import com.synectiks.process.common.plugins.views.search.elasticsearch.IndexLookup;
import com.synectiks.process.common.plugins.views.search.elasticsearch.QueryStringDecorators;
import com.synectiks.process.common.plugins.views.search.engine.QueryBackend;
import com.synectiks.process.common.plugins.views.search.errors.SearchTypeError;
import com.synectiks.process.common.plugins.views.search.errors.SearchTypeErrorParser;
import com.synectiks.process.common.plugins.views.search.filter.AndFilter;
import com.synectiks.process.common.plugins.views.search.filter.OrFilter;
import com.synectiks.process.common.plugins.views.search.filter.QueryStringFilter;
import com.synectiks.process.common.plugins.views.search.filter.StreamFilter;
import com.synectiks.process.common.storage.elasticsearch6.TimeRangeQueryFactory;
import com.synectiks.process.common.storage.elasticsearch6.jest.JestUtils;
import com.synectiks.process.common.storage.elasticsearch6.views.searchtypes.ESSearchTypeHandler;
import com.synectiks.process.server.indexer.ElasticsearchException;
import com.synectiks.process.server.indexer.IndexMapping;
import com.synectiks.process.server.plugin.Message;

import io.searchbox.client.JestClient;
import io.searchbox.core.MultiSearch;
import io.searchbox.core.MultiSearchResult;
import io.searchbox.core.Search;

import org.graylog.shaded.elasticsearch5.org.elasticsearch.index.query.BoolQueryBuilder;
import org.graylog.shaded.elasticsearch5.org.elasticsearch.index.query.QueryBuilder;
import org.graylog.shaded.elasticsearch5.org.elasticsearch.index.query.QueryBuilders;
import org.graylog.shaded.elasticsearch5.org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.synectiks.process.common.storage.elasticsearch6.jest.JestUtils.checkForFailedShards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ElasticsearchBackend implements QueryBackend<ESGeneratedQueryContext> {
    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchBackend.class);

    private final Map<String, Provider<ESSearchTypeHandler<? extends SearchType>>> elasticsearchSearchTypeHandlers;
    private final JestClient jestClient;
    private final IndexLookup indexLookup;
    private final QueryStringDecorators queryStringDecorators;
    private final ESGeneratedQueryContext.Factory queryContextFactory;
    private final boolean allowLeadingWildcard;

    @Inject
    public ElasticsearchBackend(Map<String, Provider<ESSearchTypeHandler<? extends SearchType>>> elasticsearchSearchTypeHandlers,
                                JestClient jestClient,
                                IndexLookup indexLookup,
                                QueryStringDecorators queryStringDecorators,
                                ESGeneratedQueryContext.Factory queryContextFactory,
                                @Named("allow_leading_wildcard_searches") boolean allowLeadingWildcard) {
        this.elasticsearchSearchTypeHandlers = elasticsearchSearchTypeHandlers;
        this.jestClient = jestClient;
        this.indexLookup = indexLookup;

        this.queryStringDecorators = queryStringDecorators;
        this.queryContextFactory = queryContextFactory;
        this.allowLeadingWildcard = allowLeadingWildcard;
    }

    private QueryBuilder normalizeQueryString(String queryString) {
        return (queryString.isEmpty() || queryString.trim().equals("*"))
                ? QueryBuilders.matchAllQuery()
                : QueryBuilders.queryStringQuery(queryString).allowLeadingWildcard(allowLeadingWildcard);
    }

    @Override
    public ESGeneratedQueryContext generate(SearchJob job, Query query, Set<QueryResult> results) {
        final ElasticsearchQueryString backendQuery = (ElasticsearchQueryString) query.query();

        final Set<SearchType> searchTypes = query.searchTypes();

        final String queryString = this.queryStringDecorators.decorate(backendQuery.queryString(), job, query, results);
        final QueryBuilder normalizedRootQuery = normalizeQueryString(queryString);

        final BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .filter(normalizedRootQuery);

        // add the optional root query filters
        generateFilterClause(query.filter(), job, query, results)
                .map(boolQuery::filter);

        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(boolQuery)
                .from(0)
                .size(0);

        final ESGeneratedQueryContext queryContext = queryContextFactory.create(this, searchSourceBuilder, job, query, results);
        for (SearchType searchType : searchTypes) {
            final SearchSourceBuilder searchTypeSourceBuilder = queryContext.searchSourceBuilder(searchType);

            final Set<String> effectiveStreamIds = searchType.effectiveStreams().isEmpty()
                    ? query.usedStreamIds()
                    : searchType.effectiveStreams();

            final BoolQueryBuilder searchTypeOverrides = QueryBuilders.boolQuery()
                    .must(searchTypeSourceBuilder.query())
                    .must(
                            Objects.requireNonNull(
                                    TimeRangeQueryFactory.create(
                                            query.effectiveTimeRange(searchType)
                                    ),
                                    "Timerange for search type " + searchType.id() + " cannot be found in query or search type."
                            )
                    )
                    .must(QueryBuilders.termsQuery(Message.FIELD_STREAMS, effectiveStreamIds));

            searchType.query().ifPresent(q -> {
                final ElasticsearchQueryString searchTypeBackendQuery = (ElasticsearchQueryString) q;
                final String searchTypeQueryString = this.queryStringDecorators.decorate(searchTypeBackendQuery.queryString(), job, query, results);
                final QueryBuilder normalizedSearchTypeQuery = normalizeQueryString(searchTypeQueryString);
                searchTypeOverrides.must(normalizedSearchTypeQuery);
            });

            searchTypeSourceBuilder.query(searchTypeOverrides);

            final String type = searchType.type();
            final Provider<ESSearchTypeHandler<? extends SearchType>> searchTypeHandler = elasticsearchSearchTypeHandlers.get(type);
            if (searchTypeHandler == null) {
                LOG.error("Unknown search type {} for elasticsearch backend, cannot generate query part. Skipping this search type.", type);
                queryContext.addError(new SearchTypeError(query, searchType.id(), "Unknown search type '" + type + "' for elasticsearch backend, cannot generate query"));
                continue;
            }

            searchTypeHandler.get().generateQueryPart(job, query, searchType, queryContext);
        }

        return queryContext;
    }

    // TODO make pluggable
    public Optional<QueryBuilder> generateFilterClause(Filter filter, SearchJob job, Query query, Set<QueryResult> results) {
        if (filter == null) {
            return Optional.empty();
        }

        switch (filter.type()) {
            case AndFilter.NAME:
                final BoolQueryBuilder andBuilder = QueryBuilders.boolQuery();
                filter.filters().stream()
                        .map(filter1 -> generateFilterClause(filter1, job, query, results))
                        .forEach(optQueryBuilder -> optQueryBuilder.ifPresent(andBuilder::must));
                return Optional.of(andBuilder);
            case OrFilter.NAME:
                final BoolQueryBuilder orBuilder = QueryBuilders.boolQuery();
                // TODO for the common case "any of these streams" we can optimize the filter into
                // a single "termsQuery" instead of "termQuery OR termQuery" if all direct children are "StreamFilter"
                filter.filters().stream()
                        .map(filter1 -> generateFilterClause(filter1, job, query, results))
                        .forEach(optQueryBuilder -> optQueryBuilder.ifPresent(orBuilder::should));
                return Optional.of(orBuilder);
            case StreamFilter.NAME:
                // Skipping stream filter, will be extracted elsewhere
                return Optional.empty();
            case QueryStringFilter.NAME:
                return Optional.of(QueryBuilders.queryStringQuery(this.queryStringDecorators.decorate(((QueryStringFilter) filter).query(), job, query, results)));
        }
        return Optional.empty();
    }

    @Override
    public QueryResult doRun(SearchJob job, Query query, ESGeneratedQueryContext queryContext, Set<QueryResult> predecessorResults) {
        if (query.searchTypes().isEmpty()) {
            return QueryResult.builder()
                    .query(query)
                    .searchTypes(Collections.emptyMap())
                    .errors(new HashSet<>(queryContext.errors()))
                    .build();
        }
        LOG.debug("Running query {} for job {}", query.id(), job.getId());
        final HashMap<String, SearchType.Result> resultsMap = Maps.newHashMap();

        final Set<String> affectedIndices = indexLookup.indexNamesForStreamsInTimeRange(query.usedStreamIds(), query.timerange());

        final Map<String, SearchSourceBuilder> searchTypeQueries = queryContext.searchTypeQueries();
        final List<String> searchTypeIds = new ArrayList<>(searchTypeQueries.keySet());
        final List<Search> searches = searchTypeIds
                .stream()
                .map(searchTypeId -> {
                    final Set<String> affectedIndicesForSearchType = query.searchTypes().stream()
                            .filter(s -> s.id().equalsIgnoreCase(searchTypeId)).findFirst()
                            .flatMap(searchType -> {
                                if (searchType.effectiveStreams().isEmpty()
                                        && !query.globalOverride().flatMap(GlobalOverride::timerange).isPresent()
                                        && !searchType.timerange().isPresent()) {
                                    return Optional.empty();
                                }
                                final Set<String> usedStreamIds = searchType.effectiveStreams().isEmpty()
                                        ? query.usedStreamIds()
                                        : searchType.effectiveStreams();

                                return Optional.of(indexLookup.indexNamesForStreamsInTimeRange(usedStreamIds, query.effectiveTimeRange(searchType)));
                            })
                            .orElse(affectedIndices);

                    return new Search.Builder(searchTypeQueries.get(searchTypeId).toString())
                            .addType(IndexMapping.TYPE_MESSAGE)
                            .addIndex(affectedIndicesForSearchType.isEmpty() ? Collections.singleton("") : affectedIndicesForSearchType)
                            .allowNoIndices(false)
                            .ignoreUnavailable(false)
                            .build();
                })
                .collect(Collectors.toList());
        final MultiSearch.Builder multiSearchBuilder = new MultiSearch.Builder(searches);
        final MultiSearchResult result = JestUtils.execute(jestClient, multiSearchBuilder.build(), () -> "Unable to perform search query: ");

        for (SearchType searchType : query.searchTypes()) {
            final String searchTypeId = searchType.id();
            final Provider<ESSearchTypeHandler<? extends SearchType>> handlerProvider = elasticsearchSearchTypeHandlers.get(searchType.type());
            if (handlerProvider == null) {
                LOG.error("Unknown search type '{}', cannot convert query result.", searchType.type());
                // no need to add another error here, as the query generation code will have added the error about the missing handler already
                continue;
            }
            // we create a new instance because some search type handlers might need to track information between generating the query and
            // processing its result, such as aggregations, which depend on the name and type
            final ESSearchTypeHandler<? extends SearchType> handler = handlerProvider.get();
            final int searchTypeIndex = searchTypeIds.indexOf(searchTypeId);
            final MultiSearchResult.MultiSearchResponse multiSearchResponse = result.getResponses().get(searchTypeIndex);
            if (multiSearchResponse.isError) {
                ElasticsearchException e = JestUtils.specificException(() -> "Search type returned error: ", multiSearchResponse.error);
                queryContext.addError(SearchTypeErrorParser.parse(query, searchTypeId, e));
            } else if (checkForFailedShards(multiSearchResponse.searchResult).isPresent()) {
                ElasticsearchException e = checkForFailedShards(multiSearchResponse.searchResult).get();
                queryContext.addError(SearchTypeErrorParser.parse(query, searchTypeId, e));
            } else {
                final SearchType.Result searchTypeResult = handler.extractResult(job, query, searchType, multiSearchResponse.searchResult, queryContext);
                if (searchTypeResult != null) {
                    resultsMap.put(searchTypeId, searchTypeResult);
                }
            }
        }

        LOG.debug("Query {} ran for job {}", query.id(), job.getId());
        return QueryResult.builder()
                .query(query)
                .searchTypes(resultsMap)
                .errors(new HashSet<>(queryContext.errors()))
                .build();
    }
}

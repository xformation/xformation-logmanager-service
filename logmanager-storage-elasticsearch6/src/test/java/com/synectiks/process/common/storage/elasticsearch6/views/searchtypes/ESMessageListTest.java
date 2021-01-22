/*
 *
 */
package com.synectiks.process.common.storage.elasticsearch6.views.searchtypes;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.revinate.assertj.json.JsonPathAssert;
import com.synectiks.process.common.plugins.views.search.LegacyDecoratorProcessor;
import com.synectiks.process.common.plugins.views.search.Query;
import com.synectiks.process.common.plugins.views.search.QueryResult;
import com.synectiks.process.common.plugins.views.search.SearchJob;
import com.synectiks.process.common.plugins.views.search.SearchType;
import com.synectiks.process.common.plugins.views.search.elasticsearch.ElasticsearchQueryString;
import com.synectiks.process.common.plugins.views.search.elasticsearch.QueryStringDecorators;
import com.synectiks.process.common.plugins.views.search.engine.QueryStringDecorator;
import com.synectiks.process.common.plugins.views.search.searchtypes.MessageList;
import com.synectiks.process.common.plugins.views.search.searchtypes.Sort;
import com.synectiks.process.common.storage.elasticsearch6.views.ESGeneratedQueryContext;
import com.synectiks.process.common.storage.elasticsearch6.views.searchtypes.ESMessageList;
import com.synectiks.process.server.plugin.indexer.searches.timeranges.InvalidRangeParametersException;
import com.synectiks.process.server.plugin.indexer.searches.timeranges.RelativeRange;

import io.searchbox.core.SearchResult;
import org.graylog.shaded.elasticsearch5.org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ESMessageListTest {

    @Test
    public void includesCustomNameInResultIfPresent() {
        final ESMessageList esMessageList = new ESMessageList(new QueryStringDecorators(Collections.emptySet()));
        final MessageList messageList = someMessageList().toBuilder().name("customResult").build();

        final SearchResult result = new MockSearchResult(Collections.emptyList(), (long)0);

        final SearchType.Result searchTypeResult = esMessageList.doExtractResult(null, someQuery(), messageList, result, null, null);

        assertThat(searchTypeResult.name()).contains("customResult");
    }

    @Test
    public void usesHighlightingIfActivatedInConfig() {
        MessageList messageList = someMessageList();

        ESGeneratedQueryContext context = generateQueryPartWithHighlighting(messageList);

        assertThat(context.searchSourceBuilder(messageList).highlighter()).isNotNull();
    }

    @Test
    public void doesNotUseHighlightingIfDeactivatedInConfig() {
        MessageList messageList = someMessageList();

        ESGeneratedQueryContext context = generateQueryPartWithoutHighlighting(messageList);

        assertThat(context.searchSourceBuilder(messageList).highlighter())
                .as("there should be no highlighter configured")
                .isNull();
    }

    @Test
    public void appliesDecoratorsToQueryStringIfHighlightingActivated() {
        final QueryStringDecorator queryStringDecorator = (String queryString, SearchJob job, Query query, Set<QueryResult> results) -> "Foobar!";

        final MessageList messageList = someMessageList();

        ESGeneratedQueryContext queryContext = generateQueryPartWithHighlighting(messageList, Collections.singleton(queryStringDecorator));

        final DocumentContext doc = JsonPath.parse(queryContext.searchSourceBuilder(messageList).toString());
        JsonPathAssert.assertThat(doc).jsonPathAsString("$.highlight.highlight_query.query_string.query").isEqualTo("Foobar!");
    }

    @Test
    public void passesTypeOfSortingFieldAsUnmappedType() {
        final MessageList messageList = someMessageListWithSorting("stream1", "somefield");
        final ESGeneratedQueryContext context = mockQueryContext(messageList);
        when(context.fieldType(Collections.singleton("stream1"), "somefield")).thenReturn(Optional.of("long"));

        final ESGeneratedQueryContext queryContext = generateQueryPartWithContextFor(messageList, true, Collections.emptySet(), context);

        final DocumentContext doc = JsonPath.parse(queryContext.searchSourceBuilder(messageList).toString());
        JsonPathAssert.assertThat(doc).jsonPathAsString("$.sort[0].somefield.unmapped_type").isEqualTo("long");
    }

    @Test
    public void passesNullForUnmappedTypeIfTypeIsNotFound() {
        final MessageList messageList = someMessageListWithSorting("stream1", "somefield");
        final ESGeneratedQueryContext context = mockQueryContext(messageList);
        when(context.fieldType(Collections.singleton("stream1"), "somefield")).thenReturn(Optional.empty());

        final ESGeneratedQueryContext queryContext = generateQueryPartWithContextFor(messageList, true, Collections.emptySet(), context);

        final DocumentContext doc = JsonPath.parse(queryContext.searchSourceBuilder(messageList).toString());
        assertThat(doc.read("$.sort[0].somefield", Map.class)).doesNotContainKey("unmapped_type");
    }

    private Query someQuery() {
        return Query.builder()
                .id("deadbeef")
                .query(ElasticsearchQueryString.builder()
                        .queryString("Something else")
                        .build())
                .timerange(someTimeRange())
                .searchTypes(Collections.emptySet())
                .build();
    }

    private RelativeRange someTimeRange() {
        try {
            return RelativeRange.create(300);
        } catch (InvalidRangeParametersException e) {
            throw new RuntimeException("invalid query time range spec");
        }
    }

    private MessageList someMessageList() {
        return MessageList.builder()
                .id("amessagelist")
                .limit(100)
                .offset(0)
                .build();
    }

    private MessageList someMessageListWithSorting(String stream, String sortField) {
        return MessageList.builder()
                .id("amessagelist")
                .limit(100)
                .offset(0)
                .streams(Collections.singleton(stream))
                .sort(Collections.singletonList(Sort.create(sortField, Sort.Order.ASC)))
                .build();
    }

    private ESGeneratedQueryContext generateQueryPartWithHighlighting(MessageList messageList) {
        return generateQueryPartFor(messageList, true, Collections.emptySet());
    }

    private ESGeneratedQueryContext generateQueryPartWithHighlighting(MessageList messageList, Set<QueryStringDecorator> decorators) {
        return generateQueryPartFor(messageList, true, decorators);
    }

    private ESGeneratedQueryContext generateQueryPartWithoutHighlighting(MessageList messageList) {
        return generateQueryPartFor(messageList, false, Collections.emptySet());
    }

    private ESGeneratedQueryContext generateQueryPartFor(MessageList messageList, boolean allowHighlighting, Set<QueryStringDecorator> decorators) {
        final ESGeneratedQueryContext context = mockQueryContext(messageList);

        return generateQueryPartWithContextFor(messageList, allowHighlighting, decorators, context);
    }

    private ESGeneratedQueryContext mockQueryContext(MessageList messageList) {
        ESGeneratedQueryContext context = mock(ESGeneratedQueryContext.class);

        when(context.searchSourceBuilder(messageList)).thenReturn(new SearchSourceBuilder());

        return context;
    }

    private ESGeneratedQueryContext generateQueryPartWithContextFor(MessageList messageList,
                                                                    boolean allowHighlighting,
                                                                    Set<QueryStringDecorator> decorators,
                                                                    ESGeneratedQueryContext context) {
        ESMessageList sut = new ESMessageList(
                new QueryStringDecorators(decorators),
                new LegacyDecoratorProcessor.Fake(),
                allowHighlighting);

        sut.doGenerateQueryPart(mock(SearchJob.class), someQuery(), messageList, context);

        return context;
    }
}

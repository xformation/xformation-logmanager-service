/*
 * */
package com.synectiks.process.common.plugins.views.search.timeranges;

import com.google.common.collect.ImmutableSet;
import com.synectiks.process.common.plugins.views.search.Query;
import com.synectiks.process.common.plugins.views.search.SearchType;
import com.synectiks.process.common.plugins.views.search.timeranges.DerivedTimeRange;
import com.synectiks.process.common.plugins.views.search.timeranges.OffsetRange;
import com.synectiks.process.server.plugin.indexer.searches.timeranges.AbsoluteRange;
import com.synectiks.process.server.plugin.indexer.searches.timeranges.TimeRange;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OffsetRangeTest {
    @Test
    public void returnsCorrectRangeForTimeRangeOfQuery() throws Exception {
        final OffsetRange offsetRange = constructRange("300", "query", "");
        final TimeRange sourceRange = mock(TimeRange.class);
        when(sourceRange.getFrom()).thenReturn(DateTime.parse("2019-11-18T10:00:00.000Z"));
        when(sourceRange.getTo()).thenReturn(DateTime.parse("2019-11-21T12:00:00.000Z"));

        final Query query = queryWithTimeRange(sourceRange);

        final TimeRange result = offsetRange.deriveTimeRange(query, null);

        assertThat(result).isEqualTo(AbsoluteRange.create("2019-11-18T09:55:00.000Z", "2019-11-21T11:55:00.000Z"));
    }

    @Test
    public void returnsCorrectRangeForTimeRangeOfQueryWithOffsetInUnits() throws Exception {
        final OffsetRange offsetRange = constructRange("3i", "query", "");
        final TimeRange sourceRange = mock(TimeRange.class);
        when(sourceRange.getFrom()).thenReturn(DateTime.parse("2019-11-18T10:00:00.000Z"));
        when(sourceRange.getTo()).thenReturn(DateTime.parse("2019-11-21T12:00:00.000Z"));

        final Query query = queryWithTimeRange(sourceRange);

        final TimeRange result = offsetRange.deriveTimeRange(query, null);

        assertThat(result).isEqualTo(AbsoluteRange.create("2019-11-09T04:00:00.000Z", "2019-11-12T06:00:00.000Z"));
    }

    @Test
    public void returnsCorrectRangeForTimeRangeOfSearchType() throws Exception {
        final OffsetRange offsetRange = constructRange("300", "search_type", "searchTypeId");
        final TimeRange sourceRange = mock(TimeRange.class);
        when(sourceRange.getFrom()).thenReturn(DateTime.parse("2019-11-18T10:00:00.000Z"));
        when(sourceRange.getTo()).thenReturn(DateTime.parse("2019-11-21T12:00:00.000Z"));

        final Query query = queryWithSearchTypeTimeRange(sourceRange, "searchTypeId");

        final TimeRange result = offsetRange.deriveTimeRange(query, null);

        assertThat(result).isEqualTo(AbsoluteRange.create("2019-11-18T09:55:00.000Z", "2019-11-21T11:55:00.000Z"));
    }

    @Test
    public void returnsCorrectRangeForTimeRangeOfSearchTypeWithOffsetInUnits() throws Exception {
        final OffsetRange offsetRange = constructRange("2i", "search_type", "searchTypeId");
        final TimeRange sourceRange = mock(TimeRange.class);
        when(sourceRange.getFrom()).thenReturn(DateTime.parse("2019-11-18T10:00:00.000Z"));
        when(sourceRange.getTo()).thenReturn(DateTime.parse("2019-11-21T12:00:00.000Z"));

        final Query query = queryWithSearchTypeTimeRange(sourceRange, "searchTypeId");

        final TimeRange result = offsetRange.deriveTimeRange(query, null);

        assertThat(result).isEqualTo(AbsoluteRange.create("2019-11-12T06:00:00.000Z", "2019-11-15T08:00:00.000Z"));
    }

    @Test
    public void returnsCorrectRangeWithZeroUnitOffset() throws Exception {
        final OffsetRange offsetRange = constructRange("0i", "search_type", "searchTypeId");
        final TimeRange sourceRange = mock(TimeRange.class);
        when(sourceRange.getFrom()).thenReturn(DateTime.parse("2019-11-18T10:00:00.000Z"));
        when(sourceRange.getTo()).thenReturn(DateTime.parse("2019-11-21T12:00:00.000Z"));

        final Query query = queryWithSearchTypeTimeRange(sourceRange, "searchTypeId");

        final TimeRange result = offsetRange.deriveTimeRange(query, null);

        assertThat(result).isEqualTo(AbsoluteRange.create("2019-11-18T10:00:00.000Z", "2019-11-21T12:00:00.000Z"));
    }

    @Test
    public void throwsExceptionIfInvalidSearchTypeIsReferenced() throws Exception {
        final OffsetRange offsetRange = constructRange("300", "search_type", "invalidSearchType");
        final TimeRange sourceRange = mock(TimeRange.class);
        when(sourceRange.getFrom()).thenReturn(DateTime.parse("2019-11-18T10:00:00.000Z"));
        when(sourceRange.getTo()).thenReturn(DateTime.parse("2019-11-21T12:00:00.000Z"));

        final Query query = mock(Query.class);
        final SearchType searchType = mock(SearchType.class);
        when(searchType.id()).thenReturn("searchTypeId");
        when(searchType.timerange()).thenReturn(Optional.of(DerivedTimeRange.of(sourceRange)));
        when(query.searchTypes()).thenReturn(ImmutableSet.of(searchType));
        when(query.effectiveTimeRange(searchType)).thenReturn(sourceRange);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> offsetRange.deriveTimeRange(query, searchType))
                .withMessage("Search type searchTypeId has offset timerange referencing invalid search type: invalidSearchType");
    }

    @Test
    public void throwsExceptionIfNoSearchTypeIsReferenced() throws Exception {
        final OffsetRange offsetRange = constructRange("300", "search_type", null);
        final TimeRange sourceRange = mock(TimeRange.class);
        when(sourceRange.getFrom()).thenReturn(DateTime.parse("2019-11-18T10:00:00.000Z"));
        when(sourceRange.getTo()).thenReturn(DateTime.parse("2019-11-21T12:00:00.000Z"));

        final Query query = mock(Query.class);
        final SearchType searchType = mock(SearchType.class);
        when(searchType.id()).thenReturn("searchTypeId");
        when(searchType.timerange()).thenReturn(Optional.of(DerivedTimeRange.of(sourceRange)));
        when(query.searchTypes()).thenReturn(ImmutableSet.of(searchType));
        when(query.effectiveTimeRange(searchType)).thenReturn(sourceRange);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> offsetRange.deriveTimeRange(query, searchType))
                .withMessage("Search type searchTypeId has offset timerange referencing search type but id is missing!");
    }

    private OffsetRange constructRange(String offset, String source, String id) {
        return OffsetRange.Builder.builder()
                .source(source)
                .id(id)
                .offset(offset)
                .build();
    }

    private Query queryWithTimeRange(TimeRange timeRange) {
        final Query query = mock(Query.class);
        when(query.timerange()).thenReturn(timeRange);

        return query;
    }

    private Query queryWithSearchTypeTimeRange(TimeRange timerange, String searchTypeId) {
        final Query query = mock(Query.class);
        final SearchType searchType = mock(SearchType.class);
        when(searchType.id()).thenReturn(searchTypeId);
        when(searchType.timerange()).thenReturn(Optional.of(DerivedTimeRange.of(timerange)));
        when(query.searchTypes()).thenReturn(ImmutableSet.of(searchType));
        when(query.effectiveTimeRange(searchType)).thenReturn(timerange);

        return query;
    }
}

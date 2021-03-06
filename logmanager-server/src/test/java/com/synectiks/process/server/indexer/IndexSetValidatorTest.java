/*
 * */
package com.synectiks.process.server.indexer;

import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.synectiks.process.server.indexer.IndexSet;
import com.synectiks.process.server.indexer.IndexSetRegistry;
import com.synectiks.process.server.indexer.IndexSetValidator;
import com.synectiks.process.server.indexer.indexset.IndexSetConfig;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IndexSetValidatorTest {
    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Mock
    private IndexSetRegistry indexSetRegistry;

    private IndexSetValidator validator;

    @Before
    public void setUp() throws Exception {
        this.validator = new IndexSetValidator(indexSetRegistry);
    }

    @Test
    public void validate() throws Exception {
        final String prefix = "graylog_index";
        final Duration fieldTypeRefreshInterval = Duration.standardSeconds(1L);
        final IndexSetConfig newConfig = mock(IndexSetConfig.class);
        final IndexSet indexSet = mock(IndexSet.class);

        when(indexSet.getIndexPrefix()).thenReturn("foo");
        when(indexSetRegistry.iterator()).thenReturn(Collections.singleton(indexSet).iterator());
        when(newConfig.indexPrefix()).thenReturn(prefix);
        when(newConfig.fieldTypeRefreshInterval()).thenReturn(fieldTypeRefreshInterval);

        final Optional<IndexSetValidator.Violation> violation = validator.validate(newConfig);

        assertThat(violation).isNotPresent();
    }

    @Test
    public void validateWhenAlreadyManaged() throws Exception {
        final String prefix = "graylog_index";
        final IndexSetConfig newConfig = mock(IndexSetConfig.class);
        final IndexSet indexSet = mock(IndexSet.class);

        when(indexSet.getIndexPrefix()).thenReturn("foo");
        when(indexSetRegistry.isManagedIndex("graylog_index_0")).thenReturn(true);
        when(indexSetRegistry.iterator()).thenReturn(Collections.singleton(indexSet).iterator());
        when(newConfig.indexPrefix()).thenReturn(prefix);

        final Optional<IndexSetValidator.Violation> violation = validator.validate(newConfig);

        assertThat(violation).isPresent();
    }

    @Test
    public void validateWithConflict() throws Exception {
        final IndexSetConfig newConfig = mock(IndexSetConfig.class);
        final IndexSet indexSet = mock(IndexSet.class);

        when(indexSetRegistry.iterator()).thenReturn(Collections.singleton(indexSet).iterator());

        // New index prefix starts with existing index prefix
        when(indexSet.getIndexPrefix()).thenReturn("graylog");
        when(newConfig.indexPrefix()).thenReturn("graylog_index");

        final Optional<IndexSetValidator.Violation> violation = validator.validate(newConfig);

        assertThat(violation).isPresent();
    }

    @Test
    public void validateWithConflict2() throws Exception {
        final IndexSetConfig newConfig = mock(IndexSetConfig.class);
        final IndexSet indexSet = mock(IndexSet.class);

        when(indexSetRegistry.iterator()).thenReturn(Collections.singleton(indexSet).iterator());

        // Existing index prefix starts with new index prefix
        when(indexSet.getIndexPrefix()).thenReturn("graylog");
        when(newConfig.indexPrefix()).thenReturn("gray");

        final Optional<IndexSetValidator.Violation> violation = validator.validate(newConfig);

        assertThat(violation).isPresent();
    }

    @Test
    public void validateWithInvalidFieldTypeRefreshInterval() throws Exception {
        final Duration fieldTypeRefreshInterval = Duration.millis(999);
        final IndexSetConfig newConfig = mock(IndexSetConfig.class);
        final IndexSet indexSet = mock(IndexSet.class);

        when(indexSetRegistry.iterator()).thenReturn(Collections.singleton(indexSet).iterator());
        when(indexSet.getIndexPrefix()).thenReturn("foo");
        when(newConfig.indexPrefix()).thenReturn("graylog_index");

        when(newConfig.fieldTypeRefreshInterval()).thenReturn(fieldTypeRefreshInterval);

        final Optional<IndexSetValidator.Violation> violation = validator.validate(newConfig);

        assertThat(violation).isPresent();
    }
}
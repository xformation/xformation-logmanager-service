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
package com.synectiks.process.common.storage.elasticsearch7;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.graylog.shaded.elasticsearch7.org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.graylog.shaded.elasticsearch7.org.elasticsearch.action.index.IndexRequest;
import org.graylog.shaded.elasticsearch7.org.elasticsearch.action.index.IndexResponse;
import org.graylog.shaded.elasticsearch7.org.elasticsearch.client.core.CountRequest;
import org.graylog.shaded.elasticsearch7.org.elasticsearch.client.core.CountResponse;
import org.graylog.shaded.elasticsearch7.org.elasticsearch.rest.RestStatus;

import com.synectiks.process.common.storage.elasticsearch7.MessagesAdapterES7;
import com.synectiks.process.common.storage.elasticsearch7.testing.ElasticsearchInstanceES7;
import com.synectiks.process.common.testing.elasticsearch.ElasticsearchInstance;
import com.synectiks.process.server.indexer.messages.ChunkedBulkIndexer;
import com.synectiks.process.server.indexer.messages.MessagesAdapter;
import com.synectiks.process.server.indexer.messages.MessagesIT;
import com.synectiks.process.server.shared.bindings.providers.ObjectMapperProvider;
import org.junit.Rule;

import java.util.Map;

public class MessagesES7IT extends MessagesIT {
    @Rule
    public final ElasticsearchInstanceES7 elasticsearch = ElasticsearchInstanceES7.create();

    private final ObjectMapper objectMapper = new ObjectMapperProvider().get();

    @Override
    protected ElasticsearchInstance elasticsearch() {
        return this.elasticsearch;
    }

    @Override
    protected MessagesAdapter createMessagesAdapter(MetricRegistry metricRegistry) {
        return new MessagesAdapterES7(this.elasticsearch.elasticsearchClient(), metricRegistry, new ChunkedBulkIndexer(), objectMapper);
    }

    @Override
    protected long messageCount(String indexName) {
        this.elasticsearch.elasticsearchClient().execute((c, requestOptions) -> c.indices().refresh(new RefreshRequest(), requestOptions));

        final CountRequest countRequest = new CountRequest(indexName);
        final CountResponse result = this.elasticsearch.elasticsearchClient().execute((c, requestOptions) -> c.count(countRequest, requestOptions));

        return result.getCount();
    }

    @Override
    protected boolean indexMessage(String index, Map<String, Object> source, String id) {
        final IndexRequest indexRequest = new IndexRequest(index)
                .source(source)
                .id(id);

        final IndexResponse result = this.elasticsearch.elasticsearchClient().execute((c, requestOptions) -> c.index(indexRequest, requestOptions));

        return result.status().equals(RestStatus.CREATED);
    }
}

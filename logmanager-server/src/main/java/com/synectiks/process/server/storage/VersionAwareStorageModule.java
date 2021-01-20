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
package com.synectiks.process.server.storage;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.synectiks.process.common.events.search.MoreSearchAdapter;
import com.synectiks.process.common.plugins.views.migrations.V20200730000000_AddGl2MessageIdFieldAliasForEvents;
import com.synectiks.process.common.plugins.views.search.engine.GeneratedQueryContext;
import com.synectiks.process.common.plugins.views.search.engine.QueryBackend;
import com.synectiks.process.server.indexer.IndexToolsAdapter;
import com.synectiks.process.server.indexer.cluster.ClusterAdapter;
import com.synectiks.process.server.indexer.cluster.NodeAdapter;
import com.synectiks.process.server.indexer.counts.CountsAdapter;
import com.synectiks.process.server.indexer.fieldtypes.IndexFieldTypePollerAdapter;
import com.synectiks.process.server.indexer.indices.IndicesAdapter;
import com.synectiks.process.server.indexer.messages.MessagesAdapter;
import com.synectiks.process.server.indexer.searches.SearchesAdapter;
import com.synectiks.process.server.migrations.V20170607164210_MigrateReopenedIndicesToAliases;
import com.synectiks.process.server.storage.providers.ClusterAdapterProvider;
import com.synectiks.process.server.storage.providers.CountsAdapterProvider;
import com.synectiks.process.server.storage.providers.ElasticsearchBackendProvider;
import com.synectiks.process.server.storage.providers.IndexFieldTypePollerAdapterProvider;
import com.synectiks.process.server.storage.providers.IndexToolsAdapterProvider;
import com.synectiks.process.server.storage.providers.IndicesAdapterProvider;
import com.synectiks.process.server.storage.providers.MessagesAdapterProvider;
import com.synectiks.process.server.storage.providers.MoreSearchAdapterProvider;
import com.synectiks.process.server.storage.providers.NodeAdapterProvider;
import com.synectiks.process.server.storage.providers.SearchesAdapterProvider;
import com.synectiks.process.server.storage.providers.V20170607164210_MigrateReopenedIndicesToAliasesClusterStateAdapterProvider;
import com.synectiks.process.server.storage.providers.V20200730000000_AddGl2MessageIdFieldAliasForEventsElasticsearchAdapterProvider;


public class VersionAwareStorageModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(CountsAdapter.class).toProvider(CountsAdapterProvider.class);
        bind(IndicesAdapter.class).toProvider(IndicesAdapterProvider.class);
        bind(SearchesAdapter.class).toProvider(SearchesAdapterProvider.class);
        bind(MoreSearchAdapter.class).toProvider(MoreSearchAdapterProvider.class);
        bind(MessagesAdapter.class).toProvider(MessagesAdapterProvider.class);
        bind(ClusterAdapter.class).toProvider(ClusterAdapterProvider.class);
        bind(NodeAdapter.class).toProvider(NodeAdapterProvider.class);
        bind(IndexFieldTypePollerAdapter.class).toProvider(IndexFieldTypePollerAdapterProvider.class);
        bind(IndexToolsAdapter.class).toProvider(IndexToolsAdapterProvider.class);
        bind(V20170607164210_MigrateReopenedIndicesToAliases.ClusterState.class)
                .toProvider(V20170607164210_MigrateReopenedIndicesToAliasesClusterStateAdapterProvider.class);
        bind(V20200730000000_AddGl2MessageIdFieldAliasForEvents.ElasticsearchAdapter.class)
                .toProvider(V20200730000000_AddGl2MessageIdFieldAliasForEventsElasticsearchAdapterProvider.class);

        bindQueryBackend();
    }

    private void bindQueryBackend() {
        bind(new TypeLiteral<QueryBackend<? extends GeneratedQueryContext>>() {})
                .toProvider(ElasticsearchBackendProvider.class);
    }
}

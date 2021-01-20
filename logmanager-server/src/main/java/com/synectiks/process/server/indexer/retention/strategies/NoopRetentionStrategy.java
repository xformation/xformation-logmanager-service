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
package com.synectiks.process.server.indexer.retention.strategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synectiks.process.server.indexer.IndexSet;
import com.synectiks.process.server.indexer.indices.Indices;
import com.synectiks.process.server.plugin.indexer.retention.RetentionStrategyConfig;
import com.synectiks.process.server.shared.system.activities.ActivityWriter;

import javax.inject.Inject;
import java.util.Optional;

public class NoopRetentionStrategy extends AbstractIndexCountBasedRetentionStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(NoopRetentionStrategy.class);

    @Inject
    public NoopRetentionStrategy(Indices indices, ActivityWriter activityWriter) {
        super(indices, activityWriter);
    }

    @Override
    protected Optional<Integer> getMaxNumberOfIndices(IndexSet indexSet) {
        return Optional.of(Integer.MAX_VALUE);
    }

    @Override
    protected void retain(String indexName, IndexSet indexSet) {
        LOG.info("Not running any index retention. This is the no-op index rotation strategy.");
    }

    @Override
    public Class<? extends RetentionStrategyConfig> configurationClass() {
        return NoopRetentionStrategyConfig.class;
    }

    @Override
    public RetentionStrategyConfig defaultConfiguration() {
        return NoopRetentionStrategyConfig.createDefault();
    }
}

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
package com.synectiks.process.server.migrations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synectiks.process.common.events.configuration.EventsConfiguration;
import com.synectiks.process.common.events.configuration.EventsConfigurationProvider;
import com.synectiks.process.server.plugin.cluster.ClusterConfigService;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.Optional;

public class V20190730000000_CreateDefaultEventsConfiguration extends Migration {
    private static final Logger LOG = LoggerFactory.getLogger(V20190730000000_CreateDefaultEventsConfiguration.class);

    private final EventsConfigurationProvider configProvider;
    private final ClusterConfigService clusterConfigService;

    @Inject
    public V20190730000000_CreateDefaultEventsConfiguration(EventsConfigurationProvider configProvider,
                                                            ClusterConfigService clusterConfigService) {
        this.configProvider = configProvider;
        this.clusterConfigService = clusterConfigService;
    }

    @Override
    public ZonedDateTime createdAt() {
        return ZonedDateTime.parse("2019-07-30T00:00:00Z");
    }

    @Override
    public void upgrade() {
        final Optional<EventsConfiguration> config = configProvider.loadFromDatabase();
        if (config.isPresent()) {
            LOG.debug("Found events configuration, no migration necessary.");
            return;
        }
        try {
            final EventsConfiguration defaultConfig = configProvider.getDefaultConfig();
            clusterConfigService.write(defaultConfig);
            LOG.debug("Create default events configuration: {}", defaultConfig);
        } catch (Exception e) {
            LOG.error("Unable to write default events configuration", e);
        }
    }
}

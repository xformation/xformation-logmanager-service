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
package com.synectiks.process.common.plugins.pipelineprocessor.db;

import com.synectiks.process.common.plugins.pipelineprocessor.events.RuleMetricsConfigChangedEvent;
import com.synectiks.process.server.events.ClusterEventBus;
import com.synectiks.process.server.plugin.cluster.ClusterConfigService;

import javax.inject.Inject;

public class RuleMetricsConfigService {
    private final ClusterConfigService clusterConfigService;
    private final ClusterEventBus clusterEventBus;

    @Inject
    public RuleMetricsConfigService(ClusterConfigService clusterConfigService,
                                    ClusterEventBus clusterEventBus) {
        this.clusterConfigService = clusterConfigService;
        this.clusterEventBus = clusterEventBus;
    }

    public RuleMetricsConfigDto save(RuleMetricsConfigDto config) {
        clusterConfigService.write(config);
        clusterEventBus.post(RuleMetricsConfigChangedEvent.create(config.metricsEnabled()));
        return get();
    }

    public RuleMetricsConfigDto get() {
        return clusterConfigService.getOrDefault(RuleMetricsConfigDto.class, RuleMetricsConfigDto.createDefault());
    }
}

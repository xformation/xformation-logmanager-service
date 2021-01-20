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
package com.synectiks.process.server.bindings;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.synectiks.process.common.scheduler.periodicals.ScheduleTriggerCleanUp;
import com.synectiks.process.server.events.ClusterEventCleanupPeriodical;
import com.synectiks.process.server.events.ClusterEventPeriodical;
import com.synectiks.process.server.indexer.fieldtypes.IndexFieldTypePollerPeriodical;
import com.synectiks.process.server.periodical.AlertScannerThread;
import com.synectiks.process.server.periodical.BatchedElasticSearchOutputFlushThread;
import com.synectiks.process.server.periodical.ClusterHealthCheckThread;
import com.synectiks.process.server.periodical.ClusterIdGeneratorPeriodical;
import com.synectiks.process.server.periodical.ConfigurationManagementPeriodical;
import com.synectiks.process.server.periodical.ContentPackLoaderPeriodical;
import com.synectiks.process.server.periodical.ESVersionCheckPeriodical;
import com.synectiks.process.server.periodical.GarbageCollectionWarningThread;
import com.synectiks.process.server.periodical.IndexFailuresPeriodical;
import com.synectiks.process.server.periodical.IndexRangesCleanupPeriodical;
import com.synectiks.process.server.periodical.IndexRangesMigrationPeriodical;
import com.synectiks.process.server.periodical.IndexRetentionThread;
import com.synectiks.process.server.periodical.IndexRotationThread;
import com.synectiks.process.server.periodical.IndexerClusterCheckerThread;
import com.synectiks.process.server.periodical.NodePingThread;
import com.synectiks.process.server.periodical.ThrottleStateUpdaterThread;
import com.synectiks.process.server.periodical.TrafficCounterCalculator;
import com.synectiks.process.server.periodical.UserPermissionMigrationPeriodical;
import com.synectiks.process.server.periodical.VersionCheckThread;
import com.synectiks.process.server.plugin.periodical.Periodical;

public class PeriodicalBindings extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<Periodical> periodicalBinder = Multibinder.newSetBinder(binder(), Periodical.class);
        periodicalBinder.addBinding().to(AlertScannerThread.class);
        periodicalBinder.addBinding().to(BatchedElasticSearchOutputFlushThread.class);
        periodicalBinder.addBinding().to(ClusterHealthCheckThread.class);
        periodicalBinder.addBinding().to(ContentPackLoaderPeriodical.class);
        periodicalBinder.addBinding().to(GarbageCollectionWarningThread.class);
        periodicalBinder.addBinding().to(IndexerClusterCheckerThread.class);
        periodicalBinder.addBinding().to(IndexRetentionThread.class);
        periodicalBinder.addBinding().to(IndexRotationThread.class);
        periodicalBinder.addBinding().to(NodePingThread.class);
        periodicalBinder.addBinding().to(VersionCheckThread.class);
        periodicalBinder.addBinding().to(ThrottleStateUpdaterThread.class);
        periodicalBinder.addBinding().to(ClusterEventPeriodical.class);
        periodicalBinder.addBinding().to(ClusterEventCleanupPeriodical.class);
        periodicalBinder.addBinding().to(ClusterIdGeneratorPeriodical.class);
        periodicalBinder.addBinding().to(IndexRangesMigrationPeriodical.class);
        periodicalBinder.addBinding().to(IndexRangesCleanupPeriodical.class);
        periodicalBinder.addBinding().to(UserPermissionMigrationPeriodical.class);
        periodicalBinder.addBinding().to(ConfigurationManagementPeriodical.class);
        periodicalBinder.addBinding().to(IndexFailuresPeriodical.class);
        periodicalBinder.addBinding().to(TrafficCounterCalculator.class);
        periodicalBinder.addBinding().to(IndexFieldTypePollerPeriodical.class);
        periodicalBinder.addBinding().to(ScheduleTriggerCleanUp.class);
        periodicalBinder.addBinding().to(ESVersionCheckPeriodical.class);
    }
}

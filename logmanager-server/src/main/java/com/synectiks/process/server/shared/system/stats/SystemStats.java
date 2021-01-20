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
package com.synectiks.process.server.shared.system.stats;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.synectiks.process.server.shared.system.stats.fs.FsStats;
import com.synectiks.process.server.shared.system.stats.jvm.JvmStats;
import com.synectiks.process.server.shared.system.stats.network.NetworkStats;
import com.synectiks.process.server.shared.system.stats.os.OsStats;
import com.synectiks.process.server.shared.system.stats.process.ProcessStats;

import org.graylog.autovalue.WithBeanGetter;

@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class SystemStats {
    @JsonProperty("fs")
    public abstract FsStats fsStats();

    @JsonProperty("jvm")
    public abstract JvmStats jvmStats();

    @JsonProperty("network")
    public abstract NetworkStats networkStats();

    @JsonProperty("os")
    public abstract OsStats osStats();

    @JsonProperty("process")
    public abstract ProcessStats processStats();

    public static SystemStats create(FsStats fsStats,
                                     JvmStats jvmStats,
                                     NetworkStats networkStats,
                                     OsStats osStats,
                                     ProcessStats processStats) {
        return new AutoValue_SystemStats(fsStats, jvmStats, networkStats, osStats, processStats);
    }
}

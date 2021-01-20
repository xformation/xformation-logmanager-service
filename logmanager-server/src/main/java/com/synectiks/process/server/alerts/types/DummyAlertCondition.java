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
package com.synectiks.process.server.alerts.types;

import org.joda.time.DateTime;

import com.synectiks.process.server.alerts.AbstractAlertCondition;
import com.synectiks.process.server.plugin.Tools;
import com.synectiks.process.server.plugin.streams.Stream;

import java.util.Map;

public class DummyAlertCondition extends AbstractAlertCondition {
    final String description = "Dummy alert to test notifications";

    public DummyAlertCondition(Stream stream, String id, DateTime createdAt, String creatorUserId, Map<String, Object> parameters, String title) {
        super(stream, id, Type.DUMMY.toString(), createdAt, creatorUserId, parameters, title);
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public CheckResult runCheck() {
        return new CheckResult(true, this, this.description, Tools.nowUTC(), null);
    }
}

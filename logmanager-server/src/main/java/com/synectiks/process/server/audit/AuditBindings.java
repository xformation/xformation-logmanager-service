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
package com.synectiks.process.server.audit;

import com.fasterxml.jackson.databind.JsonNode;
import com.synectiks.process.server.audit.formatter.AuditEventFormatter;
import com.synectiks.process.server.audit.formatter.FormattedAuditEvent;
import com.synectiks.process.server.plugin.PluginModule;

public class AuditBindings extends PluginModule {
    @Override
    protected void configure() {
        // Make sure there is a default binding
        auditEventSenderBinder().setDefault().to(NullAuditEventSender.class);

        addAuditEventTypes(AuditEventTypes.class);

        // Needed to avoid binding errors when there are no implementations of AuditEventFormatter.
        addAuditEventFormatter(AuditEventType.create("__ignore__:__ignore__:__ignore__"), NullAuditEventFormatter.class);
    }

    private static class NullAuditEventFormatter implements AuditEventFormatter {
        @Override
        public FormattedAuditEvent format(AuditActor actor, AuditEventType type, JsonNode jsonNodeContext) {
            return null;
        }
    }
}

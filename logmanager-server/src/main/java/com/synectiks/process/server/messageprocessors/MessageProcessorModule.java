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
package com.synectiks.process.server.messageprocessors;

import com.google.inject.Scopes;
import com.synectiks.process.common.plugins.pipelineprocessor.PipelineProcessorModule;
import com.synectiks.process.common.plugins.pipelineprocessor.db.mongodb.MongoDbServicesModule;
import com.synectiks.process.server.plugin.PluginModule;

public class MessageProcessorModule extends PluginModule {
    @Override
    protected void configure() {
        addMessageProcessor(MessageFilterChainProcessor.class, MessageFilterChainProcessor.Descriptor.class);
        // must not be a singleton, because each thread should get an isolated copy of the processors
        bind(OrderedMessageProcessors.class).in(Scopes.NO_SCOPE);

        install(new PipelineProcessorModule());
        install(new MongoDbServicesModule());
    }
}

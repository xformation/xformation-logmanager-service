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
import com.synectiks.process.server.filters.ExtractorFilter;
import com.synectiks.process.server.filters.StaticFieldFilter;
import com.synectiks.process.server.filters.StreamMatcherFilter;
import com.synectiks.process.server.plugin.filters.MessageFilter;

public class MessageFilterBindings extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<MessageFilter> messageFilters = Multibinder.newSetBinder(binder(), MessageFilter.class);
        messageFilters.addBinding().to(StaticFieldFilter.class);
        messageFilters.addBinding().to(ExtractorFilter.class);
        messageFilters.addBinding().to(StreamMatcherFilter.class);
    }
}

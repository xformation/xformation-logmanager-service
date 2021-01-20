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
package com.synectiks.process.common.plugins.views.search.rest;

import com.google.common.collect.ImmutableSet;
import com.synectiks.process.server.streams.StreamService;

import javax.inject.Inject;
import java.util.Set;
import java.util.function.Predicate;

import static com.synectiks.process.server.plugin.streams.Stream.DEFAULT_EVENT_STREAM_IDS;
import static java.util.stream.Collectors.toSet;

public class PermittedStreams {
    private final StreamService streamService;

    @Inject
    public PermittedStreams(StreamService streamService) {
        this.streamService = streamService;
    }

    public ImmutableSet<String> load(Predicate<String> isStreamIdPermitted) {
        final Set<String> result = streamService.loadAll().stream()
                .map(com.synectiks.process.server.plugin.streams.Stream::getId)
                // Unless explicitly queried, exclude event indices by default
                // Having the event indices in every search, makes sorting almost impossible
                // because it triggers https://github.com/Graylog2/graylog2-server/issues/6378
                // TODO: this filter can be removed, once we implement https://github.com/Graylog2/graylog2-server/issues/6490
                .filter(id -> !DEFAULT_EVENT_STREAM_IDS.contains(id))
                .filter(isStreamIdPermitted)
                .collect(toSet());

        return ImmutableSet.copyOf(result);
    }
}

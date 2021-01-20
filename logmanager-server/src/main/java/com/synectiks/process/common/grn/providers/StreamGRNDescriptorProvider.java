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
package com.synectiks.process.common.grn.providers;

import com.synectiks.process.common.grn.GRN;
import com.synectiks.process.common.grn.GRNDescriptor;
import com.synectiks.process.common.grn.GRNDescriptorProvider;
import com.synectiks.process.server.database.NotFoundException;
import com.synectiks.process.server.plugin.streams.Stream;
import com.synectiks.process.server.streams.StreamService;

import javax.inject.Inject;

public class StreamGRNDescriptorProvider implements GRNDescriptorProvider {
    private final StreamService streamService;

    @Inject
    public StreamGRNDescriptorProvider(StreamService streamService) {
        this.streamService = streamService;
    }

    @Override
    public GRNDescriptor get(GRN grn) {
        try {
            final Stream stream = streamService.load(grn.entity());
            return GRNDescriptor.create(grn, stream.getTitle());
        } catch (NotFoundException e) {
            return GRNDescriptor.create(grn, "ERROR: Stream for <" + grn.toString() + "> not found!");
        }
    }
}

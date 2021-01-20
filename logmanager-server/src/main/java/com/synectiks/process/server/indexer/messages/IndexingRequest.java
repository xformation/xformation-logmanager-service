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
package com.synectiks.process.server.indexer.messages;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.google.auto.value.AutoValue;
import com.synectiks.process.server.indexer.IndexSet;

import javax.validation.constraints.NotNull;

@AutoValue
@JsonAutoDetect
public abstract class IndexingRequest {
    public abstract IndexSet indexSet();
    public abstract Indexable message();

    public static IndexingRequest create(@NotNull IndexSet indexSet, @NotNull Indexable message) {
        return new AutoValue_IndexingRequest(indexSet, message);
    }

}

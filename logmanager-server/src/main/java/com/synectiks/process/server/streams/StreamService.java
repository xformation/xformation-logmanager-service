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
package com.synectiks.process.server.streams;

import org.bson.types.ObjectId;

import com.synectiks.process.server.database.NotFoundException;
import com.synectiks.process.server.plugin.alarms.AlertCondition;
import com.synectiks.process.server.plugin.database.PersistedService;
import com.synectiks.process.server.plugin.database.ValidationException;
import com.synectiks.process.server.plugin.database.users.User;
import com.synectiks.process.server.plugin.streams.Output;
import com.synectiks.process.server.plugin.streams.Stream;
import com.synectiks.process.server.plugin.streams.StreamRule;
import com.synectiks.process.server.rest.resources.streams.requests.CreateStreamRequest;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface StreamService extends PersistedService {
    Stream create(Map<String, Object> fields);

    Stream create(CreateStreamRequest request, String userId);

    String save(Stream stream) throws ValidationException;

    String saveWithRulesAndOwnership(Stream stream, Collection<StreamRule> streamRules, User user) throws ValidationException;

    Stream load(String id) throws NotFoundException;

    void destroy(Stream stream) throws NotFoundException;

    List<Stream> loadAll();

    Set<Stream> loadByIds(Collection<String> streamIds);

    Set<String> indexSetIdsByIds(Collection<String> streamIds);

    List<Stream> loadAllEnabled();

    /**
     * @return the total number of streams
     */
    long count();

    void pause(Stream stream) throws ValidationException;

    void resume(Stream stream) throws ValidationException;

    List<StreamRule> getStreamRules(Stream stream) throws NotFoundException;

    List<Stream> loadAllWithConfiguredAlertConditions();

    List<AlertCondition> getAlertConditions(Stream stream);

    AlertCondition getAlertCondition(Stream stream, String conditionId) throws NotFoundException;

    void addAlertCondition(Stream stream, AlertCondition condition) throws ValidationException;

    void updateAlertCondition(Stream stream, AlertCondition condition) throws ValidationException;

    void removeAlertCondition(Stream stream, String conditionId);

    @Deprecated
    void addAlertReceiver(Stream stream, String type, String name);

    @Deprecated
    void removeAlertReceiver(Stream stream, String type, String name);

    void addOutput(Stream stream, Output output);

    void addOutputs(ObjectId streamId, Collection<ObjectId> outputIds);

    void removeOutput(Stream stream, Output output);

    void removeOutputFromAllStreams(Output output);

    List<Stream> loadAllWithIndexSet(String indexSetId);
}

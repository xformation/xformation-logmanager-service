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
package com.synectiks.process.server.plugin.alarms;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.synectiks.process.server.plugin.DescriptorWithHumanName;
import com.synectiks.process.server.plugin.MessageSummary;
import com.synectiks.process.server.plugin.configuration.ConfigurationRequest;
import com.synectiks.process.server.plugin.streams.Stream;

import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public interface AlertCondition {
    @JsonIgnore
    String getDescription();

    String getId();

    DateTime getCreatedAt();

    String getCreatorUserId();

    Stream getStream();

    Map<String, Object> getParameters();

    @JsonIgnore
    Integer getBacklog();

    @JsonIgnore
    int getGrace();

    String getType();

    String getTitle();

    boolean shouldRepeatNotifications();

    AlertCondition.CheckResult runCheck();

    interface CheckResult {
        boolean isTriggered();
        String getResultDescription();
        AlertCondition getTriggeredCondition();
        DateTime getTriggeredAt();

        /**
         * The limited list of messages that matched the alert in the corresponding stream.
         *
         * @return list of message summaries
         */
        List<MessageSummary> getMatchingMessages();
    }

    interface Factory {
        AlertCondition create(Stream stream,
                              String id,
                              DateTime createdAt,
                              String creatorUserId,
                              Map<String, Object> parameters,
                              @Nullable String title);
        Config config();
        Descriptor descriptor();
    }

    abstract class Descriptor extends DescriptorWithHumanName {
        public Descriptor(String name, String linkToDocs, String humanName) {
            super(name, false, linkToDocs, humanName);
        }
    }

    interface Config {
        ConfigurationRequest getRequestedConfiguration();
    }
}

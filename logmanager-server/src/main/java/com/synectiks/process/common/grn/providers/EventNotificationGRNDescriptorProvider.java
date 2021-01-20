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

import com.synectiks.process.common.events.notifications.DBNotificationService;
import com.synectiks.process.common.events.notifications.NotificationDto;
import com.synectiks.process.common.grn.GRN;
import com.synectiks.process.common.grn.GRNDescriptor;
import com.synectiks.process.common.grn.GRNDescriptorProvider;

import javax.inject.Inject;
import java.util.Optional;

public class EventNotificationGRNDescriptorProvider implements GRNDescriptorProvider {
    private final DBNotificationService dbNotificationService;

    @Inject
    public EventNotificationGRNDescriptorProvider(DBNotificationService dbNotificationService) {
        this.dbNotificationService = dbNotificationService;
    }

    @Override
    public GRNDescriptor get(GRN grn) {
        final Optional<String> title = dbNotificationService.get(grn.entity()).map(NotificationDto::title);
        return GRNDescriptor.create(grn, title.orElse("ERROR: EventNotification for <" + grn.toString() + "> not found!"));
    }
}

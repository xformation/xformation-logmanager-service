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
package com.synectiks.process.common.security.authservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synectiks.process.common.security.authservice.test.AuthServiceBackendTestResult;

import javax.annotation.Nullable;
import java.util.Optional;

public interface AuthServiceBackend {
    Logger log = LoggerFactory.getLogger(AuthServiceBackend.class);

    String INTERNAL_BACKEND_ID = "000000000000000000000001";

    interface Factory<TYPE extends AuthServiceBackend> {
        TYPE create(AuthServiceBackendDTO backend);
    }

    default Optional<AuthenticationDetails> authenticateAndProvision(AuthServiceCredentials authCredentials,
            ProvisionerService provisionerService) {
        log.debug("Cannot authenticate by username/password. Username/password authentication is not supported by " +
                "auth service backend type <" + backendType() + ">.");
        return Optional.empty();
    }

    default Optional<AuthenticationDetails> authenticateAndProvision(AuthServiceToken token,
            ProvisionerService provisionerService) {
        log.debug("Cannot authenticate by token. Token-based authentication is not supported by auth service backend " +
                "type <" + backendTitle() + ">.");
        return Optional.empty();
    }

    String backendType();

    String backendId();

    String backendTitle();

    AuthServiceBackendDTO prepareConfigUpdate(AuthServiceBackendDTO existingBackend, AuthServiceBackendDTO newBackend);

    AuthServiceBackendTestResult testConnection(@Nullable AuthServiceBackendDTO existingConfig);

    AuthServiceBackendTestResult testLogin(AuthServiceCredentials credentials, @Nullable AuthServiceBackendDTO existingConfig);
}

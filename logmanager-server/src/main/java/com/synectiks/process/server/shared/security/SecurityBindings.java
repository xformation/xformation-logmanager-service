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
package com.synectiks.process.server.shared.security;

import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.OptionalBinder;
import com.synectiks.process.server.plugin.PluginModule;
import com.synectiks.process.server.rest.models.system.sessions.responses.DefaultSessionResponseFactory;
import com.synectiks.process.server.rest.models.system.sessions.responses.SessionResponseFactory;
import com.synectiks.process.server.security.DefaultX509TrustManager;
import com.synectiks.process.server.security.TrustManagerProvider;
import com.synectiks.process.server.security.UserSessionTerminationListener;
import com.synectiks.process.server.security.encryption.EncryptedValueService;

import javax.net.ssl.TrustManager;

public class SecurityBindings extends PluginModule {
    @Override
    protected void configure() {
        bind(EncryptedValueService.class).asEagerSingleton();
        bind(UserSessionTerminationListener.class).asEagerSingleton();
        bind(Permissions.class).asEagerSingleton();
        bind(SessionCreator.class).in(Scopes.SINGLETON);
        addPermissions(RestPermissions.class);

        install(new FactoryModuleBuilder()
                .implement(TrustManager.class, DefaultX509TrustManager.class)
                .build(TrustManagerProvider.class));

        OptionalBinder.newOptionalBinder(binder(), ActorAwareAuthenticationTokenFactory.class)
                .setDefault().to(ActorAwareUsernamePasswordTokenFactory.class);
        OptionalBinder.newOptionalBinder(binder(), SessionResponseFactory.class)
                .setDefault().to(DefaultSessionResponseFactory.class);
    }
}

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
package com.synectiks.process.common.plugins.beats;

import org.junit.Test;

import com.synectiks.process.common.plugins.beats.BeatsTransport;
import com.synectiks.process.server.plugin.configuration.ConfigurationRequest;
import com.synectiks.process.server.plugin.inputs.transports.NettyTransport;

import static org.assertj.core.api.Assertions.assertThat;

public class BeatsTransportConfigTest {
    @Test
    public void getRequestedConfigurationOverridesDefaultPort() throws Exception {
        final BeatsTransport.Config config = new BeatsTransport.Config();
        final ConfigurationRequest requestedConfiguration = config.getRequestedConfiguration();

        assertThat(requestedConfiguration.containsField(NettyTransport.CK_PORT)).isTrue();
        assertThat(requestedConfiguration.getField(NettyTransport.CK_PORT).getDefaultValue()).isEqualTo(5044);
    }
}
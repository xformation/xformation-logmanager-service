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
package com.synectiks.process.common.plugins.netflow;

import com.synectiks.process.common.plugins.netflow.codecs.NetFlowCodec;
import com.synectiks.process.common.plugins.netflow.inputs.NetFlowUdpInput;
import com.synectiks.process.common.plugins.netflow.transport.NetFlowUdpTransport;
import com.synectiks.process.server.plugin.PluginConfigBean;
import com.synectiks.process.server.plugin.PluginModule;

import java.util.Collections;
import java.util.Set;

public class NetFlowPluginModule extends PluginModule {
    @Override
    public Set<? extends PluginConfigBean> getConfigBeans() {
        return Collections.emptySet();
    }

    @Override
    protected void configure() {
        addMessageInput(NetFlowUdpInput.class);
        addCodec("netflow", NetFlowCodec.class);
        addTransport("netflow-udp", NetFlowUdpTransport.class);
    }
}

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
package com.synectiks.process.server.rest.resources;

import org.junit.Before;
import org.junit.Test;

import com.synectiks.process.server.configuration.HttpConfiguration;
import com.synectiks.process.server.plugin.cluster.ClusterConfigService;
import com.synectiks.process.server.plugin.cluster.ClusterId;
import com.synectiks.process.server.plugin.system.NodeId;
import com.synectiks.process.server.rest.models.HelloWorldResponse;
import com.synectiks.process.server.rest.resources.HelloWorldResource;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HelloWorldResourceTest extends RestResourceBaseTest {
    private static final String CK_CLUSTER_ID = "dummyclusterid";
    private static final String CK_NODE_ID = "dummynodeid";

    private HelloWorldResource helloWorldResource;
    private NodeId nodeId;
    private ClusterConfigService clusterConfigService;

    @Before
    public void setUp() throws Exception {
        this.nodeId = mock(NodeId.class);
        this.clusterConfigService = mock(ClusterConfigService.class);
        this.helloWorldResource = new HelloWorldResource(nodeId, clusterConfigService);

        when(clusterConfigService.getOrDefault(eq(ClusterId.class), any(ClusterId.class))).thenReturn(ClusterId.create(CK_CLUSTER_ID));
        when(nodeId.toString()).thenReturn(CK_NODE_ID);
    }

    @Test
    public void rootResourceShouldReturnGeneralStats() throws Exception {
        final HelloWorldResponse helloWorldResponse = this.helloWorldResource.helloWorld();

        assertThat(helloWorldResponse).isNotNull();

        assertThat(helloWorldResponse.clusterId()).isEqualTo(CK_CLUSTER_ID);
        assertThat(helloWorldResponse.nodeId()).isEqualTo(CK_NODE_ID);
    }

    @Test
    public void rootResourceShouldRedirectToWebInterfaceIfHtmlIsRequested() throws Exception {
        final Response response = helloWorldResource.redirectToWebConsole();

        assertThat(response).isNotNull();

        final String locationHeader = response.getHeaderString("Location");
        assertThat(locationHeader).isNotNull().isEqualTo(HttpConfiguration.PATH_WEB);
    }
}

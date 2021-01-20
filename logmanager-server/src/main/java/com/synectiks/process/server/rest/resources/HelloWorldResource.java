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

import com.codahale.metrics.annotation.Timed;
import com.synectiks.process.server.configuration.HttpConfiguration;
import com.synectiks.process.server.plugin.Version;
import com.synectiks.process.server.plugin.cluster.ClusterConfigService;
import com.synectiks.process.server.plugin.cluster.ClusterId;
import com.synectiks.process.server.plugin.system.NodeId;
import com.synectiks.process.server.rest.models.HelloWorldResponse;
import com.synectiks.process.server.shared.rest.resources.RestResource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

import static java.util.Objects.requireNonNull;

@Api(value = "Hello World", description = "A friendly hello world message")
@Path("/")
public class HelloWorldResource extends RestResource {
    private final NodeId nodeId;
    private final ClusterConfigService clusterConfigService;

    @Inject
    public HelloWorldResource(NodeId nodeId,
                              ClusterConfigService clusterConfigService) {
        this.nodeId = requireNonNull(nodeId);
        this.clusterConfigService = requireNonNull(clusterConfigService);
    }

    @GET
    @Timed
    @ApiOperation(value = "A few details about the Graylog node.")
    @Produces(MediaType.APPLICATION_JSON)
    public HelloWorldResponse helloWorld() {
        final ClusterId clusterId = clusterConfigService.getOrDefault(ClusterId.class, ClusterId.create("UNKNOWN"));
        return HelloWorldResponse.create(
            clusterId.clusterId(),
            nodeId.toString(),
            Version.CURRENT_CLASSPATH.toString(),
            "Manage your logs in the dark and have lasers going and make it look like you're from space!"
        );
    }

    @GET
    @Timed
    @ApiOperation(value = "Redirecting to web console if it runs on same port.")
    @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_XHTML_XML})
    public Response redirectToWebConsole() {
        return Response
            .temporaryRedirect(URI.create(HttpConfiguration.PATH_WEB))
            .build();
    }
}

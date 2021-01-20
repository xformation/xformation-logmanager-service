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
package com.synectiks.process.server.shared.rest.resources.system;

import com.synectiks.process.server.rest.models.system.responses.SystemJVMResponse;
import com.synectiks.process.server.rest.models.system.responses.SystemOverviewResponse;
import com.synectiks.process.server.rest.models.system.responses.SystemProcessBufferDumpResponse;
import com.synectiks.process.server.rest.models.system.responses.SystemThreadDumpResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface RemoteSystemResource {
    @GET("system")
    Call<SystemOverviewResponse> system();

    @GET("system/jvm")
    Call<SystemJVMResponse> jvm();

    @GET("system/threaddump")
    Call<SystemThreadDumpResponse> threadDump();

    @GET("system/processbufferdump")
    Call<SystemProcessBufferDumpResponse> processBufferDump();
}

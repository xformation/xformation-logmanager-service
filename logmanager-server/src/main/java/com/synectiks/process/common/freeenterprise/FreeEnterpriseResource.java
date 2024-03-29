/*
 * */
package com.synectiks.process.common.freeenterprise;

import com.codahale.metrics.annotation.Timed;
import com.synectiks.process.server.audit.jersey.NoAuditEvent;
import com.synectiks.process.server.shared.rest.resources.RestResource;
import com.synectiks.process.server.shared.security.RestPermissions;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;

@Api(value = "Enterprise")
@Path("/free-enterprise")
@Produces(MediaType.APPLICATION_JSON)
@RequiresAuthentication
public class FreeEnterpriseResource extends RestResource {
    private static final Logger LOG = LoggerFactory.getLogger(FreeEnterpriseResource.class);

    private final FreeEnterpriseService freeEnterpriseService;

    @Inject
    public FreeEnterpriseResource(FreeEnterpriseService freeEnterpriseService) {
        this.freeEnterpriseService = freeEnterpriseService;
    }

    @GET
    @Timed
    @ApiOperation(value = "Get logmanager Enterprise license info")
    @Path("/license/info")
    @RequiresPermissions(RestPermissions.LICENSEINFOS_READ)
    public Response licenseInfo() {
        return Response.ok(Collections.singletonMap("free_license_info", freeEnterpriseService.licenseInfo())).build();
    }

    @POST
    @Timed
    @ApiOperation(value = "Request free logmanager Enterprise license")
    @Path("/license")
    @RequiresPermissions(RestPermissions.FREELICENSES_CREATE)
    @NoAuditEvent("This will be used to get a license. Without license triggering an audit event doesn't make sense.")
    public Response requestFreeLicense(@NotNull @Valid FreeLicenseRequest request) {
        if (freeEnterpriseService.canRequestFreeLicense()) {
            try {
                freeEnterpriseService.requestFreeLicense(request);
            } catch (Exception e) {
                throw new InternalServerErrorException(e.getMessage(), e);
            }
            return Response.accepted().build();
        }
        throw new BadRequestException("Free Logmanger Enterprise license already requested or license already installed");
    }
}

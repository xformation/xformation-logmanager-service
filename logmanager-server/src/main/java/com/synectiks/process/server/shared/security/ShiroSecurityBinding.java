/*
 * */
package com.synectiks.process.server.shared.security;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.util.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import java.lang.reflect.Method;

// Attention!
// Don't register Providers such as ContainerRequestFilter in DynamicFeature.
// This will get Jersey to create a ServiceLocator for every existing Resource.
// Each ServiceLocator will have its own Guice/HK2 Bridge, which is very very inefficient!
public class ShiroSecurityBinding implements DynamicFeature {
    private static final Logger LOG = LoggerFactory.getLogger(ShiroSecurityBinding.class);

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        final Class<?> resourceClass = resourceInfo.getResourceClass();
        final Method resourceMethod = resourceInfo.getResourceMethod();

        if (resourceMethod.isAnnotationPresent(RequiresAuthentication.class) || resourceClass.isAnnotationPresent(RequiresAuthentication.class)) {
            if (resourceMethod.isAnnotationPresent(RequiresGuest.class)) {
                LOG.debug("Resource method {}#{} is marked as unauthenticated, skipping setting filter.");
            } else {
                LOG.debug("Resource method {}#{} requires an authenticated user.", resourceClass.getCanonicalName(), resourceMethod.getName());
                context.register(new ShiroAuthenticationFilter());
            }
        }

        if (resourceMethod.isAnnotationPresent(RequiresPermissions.class) || resourceClass.isAnnotationPresent(RequiresPermissions.class)) {
            RequiresPermissions requiresPermissions = resourceClass.getAnnotation(RequiresPermissions.class);
            if (requiresPermissions == null) {
                requiresPermissions = resourceMethod.getAnnotation(RequiresPermissions.class);
            }

            LOG.debug("Resource method {}#{} requires an authorization checks.", resourceClass.getCanonicalName(), resourceMethod.getName());
            context.register(new ShiroAuthorizationFilter(requiresPermissions));
        }

        // TODO this is the wrong approach, we should have an Environment and proper request wrapping
        context.register((ContainerResponseFilter) (requestContext, responseContext) -> ThreadContext.unbindSubject());
    }
}

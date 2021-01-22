/*
 * */
package com.synectiks.process.server.audit;

import com.google.auto.value.AutoValue;
import com.synectiks.process.server.plugin.system.NodeId;

import org.graylog.autovalue.WithBeanGetter;

import javax.annotation.Nonnull;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Objects.requireNonNull;

@AutoValue
@WithBeanGetter
public abstract class AuditActor {
    private static final String URN_GRAYLOG_NODE = "urn:graylog:node:";
    private static final String URN_GRAYLOG_USER = "urn:graylog:user:";

    public abstract String urn();

    public static AuditActor user(@Nonnull String username) {
        if (isNullOrEmpty(username)) {
            throw new IllegalArgumentException("username must not be null or empty");
        }
        return new AutoValue_AuditActor(URN_GRAYLOG_USER + username);
    }

    public static AuditActor system(@Nonnull NodeId nodeId) {
        return new AutoValue_AuditActor(URN_GRAYLOG_NODE + requireNonNull(nodeId, "nodeId must not be null").toString());
    }
}

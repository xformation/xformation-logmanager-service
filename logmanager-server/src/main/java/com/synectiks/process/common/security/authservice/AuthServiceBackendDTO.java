/*
 * */
package com.synectiks.process.common.security.authservice;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import com.synectiks.process.server.plugin.rest.ValidationResult;

import org.graylog.autovalue.WithBeanGetter;
import org.mongojack.Id;
import org.mongojack.ObjectId;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isBlank;

@AutoValue
@JsonAutoDetect
@JsonDeserialize(builder = AuthServiceBackendDTO.Builder.class)
@WithBeanGetter
public abstract class AuthServiceBackendDTO {
    private static final String FIELD_ID = "id";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_DESCRIPTION = "description";
    private static final String FIELD_DEFAULT_ROLES = "default_roles";
    private static final String FIELD_CONFIG = "config";

    @Id
    @ObjectId
    @Nullable
    @JsonProperty(FIELD_ID)
    public abstract String id();

    @JsonProperty(FIELD_TITLE)
    public abstract String title();

    @JsonProperty(FIELD_DESCRIPTION)
    public abstract String description();

    @JsonProperty(FIELD_DEFAULT_ROLES)
    public abstract Set<String> defaultRoles();

    @NotNull
    @JsonProperty(FIELD_CONFIG)
    public abstract AuthServiceBackendConfig config();

    @JsonIgnore
    public ValidationResult validate() {
        final ValidationResult result = new ValidationResult();

        if (isBlank(title())) {
            result.addError(FIELD_TITLE, "Title cannot be empty.");
        }

        try {
            config().validate(result);
        } catch (UnsupportedOperationException e) {
            result.addError(FIELD_CONFIG, "Config type cannot be empty.");
        }

        return result;
    }

    public AuthServiceBackendDTO withId(String id) {
        return toBuilder().id(id).build();
    }

    public static Builder builder() {
        return Builder.create();
    }

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public abstract static class Builder {
        @JsonCreator
        public static Builder create() {
            return new AutoValue_AuthServiceBackendDTO.Builder()
                    .description("")
                    .defaultRoles(Collections.emptySet());
        }

        @Id
        @ObjectId
        @JsonProperty(FIELD_ID)
        public abstract Builder id(String id);

        @JsonProperty(FIELD_TITLE)
        public abstract Builder title(String title);

        @JsonProperty(FIELD_DESCRIPTION)
        public abstract Builder description(String description);

        @JsonProperty(FIELD_DEFAULT_ROLES)
        public abstract Builder defaultRoles(Set<String> defaultRoles);

        @JsonProperty(FIELD_CONFIG)
        public abstract Builder config(AuthServiceBackendConfig config);

        public abstract AuthServiceBackendDTO build();
    }
}

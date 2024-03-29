/*
 * */
package com.synectiks.process.common.events.notifications.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.synectiks.process.common.events.contentpack.entities.EmailEventNotificationConfigEntity;
import com.synectiks.process.common.events.contentpack.entities.EventNotificationConfigEntity;
import com.synectiks.process.common.events.event.EventDto;
import com.synectiks.process.common.events.notifications.EventNotificationConfig;
import com.synectiks.process.common.events.notifications.EventNotificationExecutionJob;
import com.synectiks.process.common.scheduler.JobTriggerData;
import com.synectiks.process.server.contentpacks.EntityDescriptorIds;
import com.synectiks.process.server.contentpacks.model.entities.references.ValueReference;
import com.synectiks.process.server.plugin.rest.ValidationResult;

import javax.validation.constraints.NotBlank;
import java.util.Set;

@AutoValue
@JsonTypeName(EmailEventNotificationConfig.TYPE_NAME)
@JsonDeserialize(builder = EmailEventNotificationConfig.Builder.class)
public abstract class EmailEventNotificationConfig implements EventNotificationConfig {
    public static final String TYPE_NAME = "email-notification-v1";

    private static final String DEFAULT_SENDER = "logmanager@example.org";
    static final String DEFAULT_SUBJECT = "logmanager event notification: ${event_definition_title}";
    static final String DEFAULT_BODY_TEMPLATE = "--- [Event Definition] ---------------------------\n" +
            "Title:       ${event_definition_title}\n" +
            "Description: ${event_definition_description}\n" +
            "Type:        ${event_definition_type}\n" +
            "--- [Event] --------------------------------------\n" +
            "Timestamp:            ${event.timestamp}\n" +
            "Message:              ${event.message}\n" +
            "Source:               ${event.source}\n" +
            "Key:                  ${event.key}\n" +
            "Priority:             ${event.priority}\n" +
            "Alert:                ${event.alert}\n" +
            "Timestamp Processing: ${event.timestamp}\n" +
            "Timerange Start:      ${event.timerange_start}\n" +
            "Timerange End:        ${event.timerange_end}\n" +
            "Source Streams:       ${event.source_streams}\n" +
            "Fields:\n" +
            "${foreach event.fields field}  ${field.key}: ${field.value}\n" +
            "${end}\n" +
            "${if backlog}\n" +
            "--- [Backlog] ------------------------------------\n" +
            "Last messages accounting for this alert:\n" +
            "${foreach backlog message}\n" +
            "${message}\n\n" +
            "${end}\n" +
            "${end}\n" +
            "\n";

    private static final String FIELD_SENDER = "sender";
    private static final String FIELD_SUBJECT = "subject";
    private static final String FIELD_BODY_TEMPLATE = "body_template";
    private static final String FIELD_HTML_BODY_TEMPLATE = "html_body_template";
    private static final String FIELD_EMAIL_RECIPIENTS = "email_recipients";
    private static final String FIELD_USER_RECIPIENTS = "user_recipients";

    @JsonProperty(FIELD_SENDER)
    @NotBlank
    public abstract String sender();

    @JsonProperty(FIELD_SUBJECT)
    @NotBlank
    public abstract String subject();

    @JsonProperty(FIELD_BODY_TEMPLATE)
    public abstract String bodyTemplate();

    @JsonProperty(FIELD_HTML_BODY_TEMPLATE)
    public abstract String htmlBodyTemplate();

    @JsonProperty(FIELD_EMAIL_RECIPIENTS)
    public abstract Set<String> emailRecipients();

    @JsonProperty(FIELD_USER_RECIPIENTS)
    public abstract Set<String> userRecipients();

    @JsonIgnore
    public JobTriggerData toJobTriggerData(EventDto dto) {
        return EventNotificationExecutionJob.Data.builder().eventDto(dto).build();
    }

    public static Builder builder() {
        return Builder.create();
    }

    @JsonIgnore
    public ValidationResult validate() {
        final ValidationResult validation = new ValidationResult();

        if (sender().isEmpty()) {
            validation.addError(FIELD_SENDER, "Email Notification sender cannot be empty.");
        }
        if (subject().isEmpty()) {
            validation.addError(FIELD_SUBJECT, "Email Notification subject cannot be empty.");
        }
        if (bodyTemplate().isEmpty() && htmlBodyTemplate().isEmpty()) {
            validation.addError("body", "One of Email Notification body template or Email Notification HTML body must not be empty.");
        }
        if (emailRecipients().isEmpty() && userRecipients().isEmpty()) {
            validation.addError("recipients", "Email Notification must have email recipients or user recipients.");
        }

        return validation;
    }

    @AutoValue.Builder
    public static abstract class Builder implements EventNotificationConfig.Builder<Builder> {
        @JsonCreator
        public static Builder create() {
            return new AutoValue_EmailEventNotificationConfig.Builder()
                    .type(TYPE_NAME)
                    .sender(DEFAULT_SENDER)
                    .subject(DEFAULT_SUBJECT)
                    .emailRecipients(ImmutableSet.of())
                    .userRecipients(ImmutableSet.of())
                    .bodyTemplate(DEFAULT_BODY_TEMPLATE)
                    .htmlBodyTemplate("");
        }

        @JsonProperty(FIELD_SENDER)
        public abstract Builder sender(String sender);

        @JsonProperty(FIELD_SUBJECT)
        public abstract Builder subject(String subject);

        @JsonProperty(FIELD_BODY_TEMPLATE)
        public abstract Builder bodyTemplate(String bodyTemplate);

        @JsonProperty(FIELD_HTML_BODY_TEMPLATE)
        public abstract Builder htmlBodyTemplate(String htmlBodyTemplate);

        @JsonProperty(FIELD_EMAIL_RECIPIENTS)
        public abstract Builder emailRecipients(Set<String> emailRecipients);

        @JsonProperty(FIELD_USER_RECIPIENTS)
        public abstract Builder userRecipients(Set<String> userRecipients);

        public abstract EmailEventNotificationConfig build();
    }

    @Override
    public EventNotificationConfigEntity toContentPackEntity(EntityDescriptorIds entityDescriptorIds) {
        return EmailEventNotificationConfigEntity.builder()
            .sender(ValueReference.of(sender()))
            .subject(ValueReference.of(subject()))
            .bodyTemplate(ValueReference.of(bodyTemplate()))
            .htmlBodyTemplate(ValueReference.of(htmlBodyTemplate()))
            .emailRecipients(emailRecipients())
            .userRecipients(userRecipients())
            .build();
    }
}

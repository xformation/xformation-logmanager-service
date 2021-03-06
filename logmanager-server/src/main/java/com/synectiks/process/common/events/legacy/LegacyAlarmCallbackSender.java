/*
 * */
package com.synectiks.process.common.events.legacy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mongodb.BasicDBObject;
import com.synectiks.process.common.events.event.EventDto;
import com.synectiks.process.common.events.processor.EventDefinition;
import com.synectiks.process.common.events.processor.EventProcessorConfig;
import com.synectiks.process.server.alerts.AbstractAlertCondition;
import com.synectiks.process.server.database.NotFoundException;
import com.synectiks.process.server.plugin.MessageSummary;
import com.synectiks.process.server.plugin.alarms.callbacks.AlarmCallback;
import com.synectiks.process.server.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import com.synectiks.process.server.plugin.alarms.callbacks.AlarmCallbackException;
import com.synectiks.process.server.plugin.configuration.ConfigurationException;
import com.synectiks.process.server.plugin.streams.Output;
import com.synectiks.process.server.plugin.streams.Stream;
import com.synectiks.process.server.plugin.streams.StreamRule;
import com.synectiks.process.server.streams.StreamImpl;
import com.synectiks.process.server.streams.StreamService;

import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LegacyAlarmCallbackSender {
    private static final Logger LOG = LoggerFactory.getLogger(LegacyAlarmCallbackSender.class);

    private final LegacyAlarmCallbackFactory alarmCallbackFactory;
    private final StreamService streamService;
    private final ObjectMapper objectMapper;

    @Inject
    public LegacyAlarmCallbackSender(LegacyAlarmCallbackFactory alarmCallbackFactory,
                                     StreamService streamService,
                                     ObjectMapper objectMapper) {
        this.alarmCallbackFactory = alarmCallbackFactory;
        this.streamService = streamService;
        this.objectMapper = objectMapper;
    }

    public void send(LegacyAlarmCallbackEventNotificationConfig config,
                     EventDefinition eventDefinition,
                     EventDto event,
                     List<MessageSummary> backlog) throws Exception {
        final String callbackType = config.callbackType();
        final Stream stream = findStream(eventDefinition.config());

        final AbstractAlertCondition alertCondition = new LegacyAlertCondition(stream, eventDefinition, event);
        final AbstractAlertCondition.CheckResult checkResult = new AbstractAlertCondition.CheckResult(
                true,
                alertCondition,
                event.message(),
                event.processingTimestamp(),
                backlog
        );

        try {
            final AlarmCallback callback = alarmCallbackFactory.create(callbackType, config.configuration());

            callback.checkConfiguration();
            callback.call(stream, checkResult);
        } catch (ClassNotFoundException e) {
            LOG.error("Couldn't find implementation class for type <{}>", callbackType);
            throw e;
        } catch (AlarmCallbackConfigurationException e) {
            LOG.error("Invalid legacy alarm callback configuration", e);
            throw e;
        } catch (ConfigurationException e) {
            LOG.error("Invalid configuration for legacy alarm callback <{}>", callbackType, e);
            throw e;
        } catch (AlarmCallbackException e) {
            LOG.error("Couldn't execute legacy alarm callback <{}>", callbackType, e);
            throw e;
        }
    }

    /**
     * A legacy {@link AlarmCallback} expects to receive a {@link Stream} instance. The old alert conditions where
     * scoped to a single stream. The new event system supports event definitions that search in multiple streams.
     *
     * Migrated alert condition configurations are only using a single stream so we just take the first stream
     * we can find in the event definition config.
     */
    private Stream findStream(EventProcessorConfig eventProcessorConfig) {
        final JsonNode config = objectMapper.convertValue(eventProcessorConfig, JsonNode.class);

        // Since we don't really know which type of event processor config we have, try to find a "streams" array.
        // This will work for the build-in aggregation event processor but might not work for others.
        final JsonNode streams = config.path("streams");

        if (streams.isMissingNode() || !streams.isArray()) {
            LOG.debug("Couldn't find streams in event processor config: {}", eventProcessorConfig);
            return MissingStream.create();
        }

        if (streams.size() > 1) {
            LOG.warn("Found more than one stream in event definition. Please don't use legacy alarm callbacks anymore!");
        }

        final String streamId = streams.path(0).asText();
        try {
            return streamService.load(streamId);
        } catch (NotFoundException e) {
            LOG.error("Couldn't load stream <{}> from database", streamId, e);
            return MissingStream.create();
        }
    }

    private static class MissingStream extends StreamImpl {
        static Stream create() {
            final ImmutableMap<String, Object> fields = ImmutableMap.<String, Object>builder()
                    .put("_id", new ObjectId("5400deadbeefdeadbeefaffe"))
                    .put(StreamImpl.FIELD_TITLE, "Missing stream")
                    .put(StreamImpl.FIELD_DESCRIPTION, "We could find a stream")
                    .put(StreamImpl.FIELD_RULES, ImmutableList.<StreamRule>of())
                    .put(StreamImpl.FIELD_OUTPUTS, ImmutableSet.<Output>of())
                    .put(StreamImpl.FIELD_CREATED_AT, DateTime.now(DateTimeZone.UTC))
                    .put(StreamImpl.FIELD_CREATOR_USER_ID, "admin")
                    .put(StreamImpl.EMBEDDED_ALERT_CONDITIONS, ImmutableList.<BasicDBObject>of())
                    .build();

            return new MissingStream(fields);
        }

        private MissingStream(Map<String, Object> fields) {
            super(new ObjectId(), fields, Collections.emptyList(), Collections.emptySet(), null);
        }
    }
}

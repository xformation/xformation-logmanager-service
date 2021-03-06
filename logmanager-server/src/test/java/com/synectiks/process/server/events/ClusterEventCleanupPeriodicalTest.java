/*
 * */
package com.synectiks.process.server.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.synectiks.process.common.testing.mongodb.MongoDBInstance;
import com.synectiks.process.server.bindings.providers.MongoJackObjectMapperProvider;
import com.synectiks.process.server.database.MongoConnection;
import com.synectiks.process.server.events.ClusterEventCleanupPeriodical;
import com.synectiks.process.server.events.ClusterEventPeriodical;
import com.synectiks.process.server.shared.bindings.providers.ObjectMapperProvider;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class ClusterEventCleanupPeriodicalTest {
    @Rule
    public final MongoDBInstance mongodb = MongoDBInstance.createForClass();
    private static final DateTime TIME = new DateTime(2015, 4, 1, 0, 0, DateTimeZone.UTC);

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    private final ObjectMapper objectMapper = new ObjectMapperProvider().get();
    private MongoConnection mongoConnection;
    private ClusterEventCleanupPeriodical clusterEventCleanupPeriodical;

    @Before
    public void setUpService() throws Exception {
        DateTimeUtils.setCurrentMillisFixed(TIME.getMillis());

        this.mongoConnection = mongodb.mongoConnection();

        MongoJackObjectMapperProvider provider = new MongoJackObjectMapperProvider(objectMapper);
        this.clusterEventCleanupPeriodical = new ClusterEventCleanupPeriodical(provider, mongodb.mongoConnection());
    }

    @After
    public void tearDown() {
        DateTimeUtils.setCurrentMillisSystem();
        mongoConnection.getMongoDatabase().drop();
    }

    @Test
    public void testDoRun() throws Exception {
        final DBCollection collection = mongoConnection.getDatabase().getCollection(ClusterEventPeriodical.COLLECTION_NAME);
        assertThat(insertEvent(collection, 0L)).isTrue();
        assertThat(insertEvent(collection, TIME.getMillis())).isTrue();
        assertThat(insertEvent(collection, TIME.minus(ClusterEventCleanupPeriodical.DEFAULT_MAX_EVENT_AGE).getMillis())).isTrue();
        assertThat(insertEvent(collection, TIME.minus(2 * ClusterEventCleanupPeriodical.DEFAULT_MAX_EVENT_AGE).getMillis())).isTrue();
        assertThat(collection.count()).isEqualTo(4L);

        clusterEventCleanupPeriodical.run();

        assertThat(collection.count()).isEqualTo(2L);
    }

    private boolean insertEvent(DBCollection collection, long timestamp) {
        DBObject event = new BasicDBObjectBuilder()
                .add("timestamp", timestamp)
                .add("producer", "TEST-PRODUCER")
                .add("consumers", Collections.emptyList())
                .add("event_class", String.class.getCanonicalName())
                .add("payload", "Test" + timestamp)
                .get();
        return collection.save(event).wasAcknowledged();
    }
}

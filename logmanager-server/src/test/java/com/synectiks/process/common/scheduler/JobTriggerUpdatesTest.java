/*
 * */
package com.synectiks.process.common.scheduler;

import com.google.common.collect.ImmutableMap;
import com.synectiks.process.common.events.JobSchedulerTestClock;
import com.synectiks.process.common.events.TestJobTriggerData;
import com.synectiks.process.common.scheduler.JobScheduleStrategies;
import com.synectiks.process.common.scheduler.JobTriggerDto;
import com.synectiks.process.common.scheduler.JobTriggerUpdate;
import com.synectiks.process.common.scheduler.JobTriggerUpdates;
import com.synectiks.process.common.scheduler.schedule.IntervalJobSchedule;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class JobTriggerUpdatesTest {
    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    private JobSchedulerTestClock clock;
    private JobScheduleStrategies strategies;

    @Before
    public void setUp() throws Exception {
        this.clock = new JobSchedulerTestClock(DateTime.now(DateTimeZone.UTC));
        this.strategies = new JobScheduleStrategies(clock);
    }

    @Test
    public void scheduleNextExecution() {
        final JobTriggerDto trigger = JobTriggerDto.builderWithClock(clock)
                .jobDefinitionId("abc-123")
                .schedule(IntervalJobSchedule.builder().interval(31).unit(TimeUnit.SECONDS).build())
                .build();

        final JobTriggerUpdates updates = new JobTriggerUpdates(clock, strategies, trigger);

        assertThat(updates.scheduleNextExecution()).isEqualTo(JobTriggerUpdate.withNextTime(clock.nowUTC().plusSeconds(31)));
    }

    @Test
    public void scheduleNextExecutionWithData() {
        final JobTriggerDto trigger = JobTriggerDto.builderWithClock(clock)
                .jobDefinitionId("abc-123")
                .schedule(IntervalJobSchedule.builder().interval(5).unit(TimeUnit.MINUTES).build())
                .build();

        final JobTriggerUpdates updates = new JobTriggerUpdates(clock, strategies, trigger);
        final TestJobTriggerData data = TestJobTriggerData.create(ImmutableMap.of("hello", "world"));

        assertThat(updates.scheduleNextExecution(data))
                .isEqualTo(JobTriggerUpdate.withNextTimeAndData(clock.nowUTC().plusMinutes(5), data));
    }

    @Test
    public void retryIn() {
        final JobTriggerUpdates updates = new JobTriggerUpdates(clock, strategies, mock(JobTriggerDto.class));

        assertThat(updates.retryIn(123, TimeUnit.SECONDS))
                .isEqualTo(JobTriggerUpdate.withNextTime(clock.nowUTC().plusSeconds(123)));

        assertThat(updates.retryIn(1, TimeUnit.HOURS))
                .isEqualTo(JobTriggerUpdate.withNextTime(clock.nowUTC().plusHours(1)));
    }
}

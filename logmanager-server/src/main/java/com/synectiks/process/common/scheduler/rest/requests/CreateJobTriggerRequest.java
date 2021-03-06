/*
 * */
package com.synectiks.process.common.scheduler.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import com.synectiks.process.common.scheduler.JobSchedule;
import com.synectiks.process.common.scheduler.JobTriggerData;
import com.synectiks.process.common.scheduler.JobTriggerDto;
import com.synectiks.process.common.scheduler.JobTriggerStatus;
import com.synectiks.process.common.scheduler.clock.JobSchedulerClock;
import com.synectiks.process.common.scheduler.clock.JobSchedulerSystemClock;

import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * This request is used to create new triggers via the HTTP API. It doesn't expose internal trigger fields like "lock"
 * that shouldn't be set by a user.
 */
@AutoValue
@JsonDeserialize(builder = CreateJobTriggerRequest.Builder.class)
public abstract class CreateJobTriggerRequest {
    private static final String FIELD_JOB_DEFINITION_ID = "job_definition_id";
    private static final String FIELD_START_TIME = "start_time";
    private static final String FIELD_END_TIME = "end_time";
    private static final String FIELD_NEXT_TIME = "next_time";
    private static final String FIELD_SCHEDULE = "schedule";
    private static final String FIELD_DATA = "data";

    @JsonProperty(FIELD_JOB_DEFINITION_ID)
    public abstract String jobDefinitionId();

    @JsonProperty(FIELD_START_TIME)
    public abstract DateTime startTime();

    @JsonProperty(FIELD_END_TIME)
    public abstract Optional<DateTime> endTime();

    @JsonProperty(FIELD_NEXT_TIME)
    public abstract DateTime nextTime();

    @JsonProperty(FIELD_SCHEDULE)
    public abstract JobSchedule schedule();

    @JsonProperty(FIELD_DATA)
    public abstract Optional<JobTriggerData> data();

    public static Builder builder() {
        return Builder.create();
    }

    public abstract Builder toBuilder();

    public JobTriggerDto toDto() {
        return toDto(new JobSchedulerSystemClock());
    }

    public JobTriggerDto toDto(JobSchedulerClock clock) {
        return JobTriggerDto.builderWithClock(clock)
                .jobDefinitionId(jobDefinitionId())
                .startTime(startTime())
                .endTime(endTime().orElse(null))
                .nextTime(nextTime())
                .status(JobTriggerStatus.RUNNABLE)
                .schedule(schedule())
                .data(data().orElse(null))
                .build();
    }

    @AutoValue.Builder
    public static abstract class Builder {
        @JsonCreator
        public static Builder create() {
            return create(new JobSchedulerSystemClock());
        }

        public static Builder create(JobSchedulerClock clock) {
            final DateTime now = clock.nowUTC();

            return new AutoValue_CreateJobTriggerRequest.Builder()
                    .startTime(now)
                    .nextTime(now);
        }

        @JsonProperty(FIELD_JOB_DEFINITION_ID)
        public abstract Builder jobDefinitionId(String jobDefinitionId);

        @JsonProperty(FIELD_START_TIME)
        public abstract Builder startTime(DateTime startTime);

        @JsonProperty(FIELD_END_TIME)
        public abstract Builder endTime(@Nullable DateTime endTime);

        @JsonProperty(FIELD_NEXT_TIME)
        public abstract Builder nextTime(DateTime nextTime);

        @JsonProperty(FIELD_SCHEDULE)
        public abstract Builder schedule(JobSchedule schedule);

        @JsonProperty(FIELD_DATA)
        public abstract Builder data(@Nullable JobTriggerData data);

        public abstract CreateJobTriggerRequest build();
    }
}

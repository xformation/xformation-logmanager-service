/*
 * */
package com.synectiks.process.common.scheduler;

import com.github.joschi.jadconfig.Parameter;
import com.github.joschi.jadconfig.util.Duration;
import com.github.joschi.jadconfig.validators.PositiveDurationValidator;
import com.synectiks.process.server.plugin.PluginConfigBean;

/**
 * Job scheduler specific configuration fields for the server configuration file.
 */
@SuppressWarnings({"FieldCanBeLocal", "unused", "WeakerAccess"})
public class JobSchedulerConfiguration implements PluginConfigBean {
    public static final String LOOP_SLEEP_DURATION = "job_scheduler_loop_sleep_duration";

    @Parameter(value = LOOP_SLEEP_DURATION, validators = PositiveDurationValidator.class)
    private Duration loopSleepDuration = Duration.seconds(1);

    public Duration getLoopSleepDuration() {
        return loopSleepDuration;
    }
}

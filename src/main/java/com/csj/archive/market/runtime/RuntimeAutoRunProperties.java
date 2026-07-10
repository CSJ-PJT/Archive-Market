package com.csj.archive.market.runtime;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "archive.runtime")
public class RuntimeAutoRunProperties {

    private final AutoRun autorun = new AutoRun();
    private Duration tickInterval = Duration.ofSeconds(30);
    private int maxEventsPerTick = 10;
    private int maxBacklogPerTick = 50;

    public AutoRun getAutorun() {
        return autorun;
    }

    public Duration getTickInterval() {
        return tickInterval;
    }

    public void setTickInterval(Duration tickInterval) {
        this.tickInterval = tickInterval;
    }

    public int getMaxEventsPerTick() {
        return maxEventsPerTick;
    }

    public void setMaxEventsPerTick(int maxEventsPerTick) {
        this.maxEventsPerTick = maxEventsPerTick;
    }

    public int getMaxBacklogPerTick() {
        return maxBacklogPerTick;
    }

    public void setMaxBacklogPerTick(int maxBacklogPerTick) {
        this.maxBacklogPerTick = maxBacklogPerTick;
    }

    public static class AutoRun {
        private boolean enabled = true;
        private boolean schedulerEnabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isSchedulerEnabled() {
            return schedulerEnabled;
        }

        public void setSchedulerEnabled(boolean schedulerEnabled) {
            this.schedulerEnabled = schedulerEnabled;
        }
    }
}

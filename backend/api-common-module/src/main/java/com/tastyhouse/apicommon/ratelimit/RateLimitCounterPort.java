package com.tastyhouse.apicommon.ratelimit;

import java.time.Duration;

public interface RateLimitCounterPort {

    boolean isLimitExceeded(String key, int limit, Duration duration);
}

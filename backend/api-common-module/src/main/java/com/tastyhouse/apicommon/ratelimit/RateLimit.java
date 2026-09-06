package com.tastyhouse.apicommon.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    int limit();

    long windowSeconds();

    RateLimitKeyType keyType();

    String keyField() default "";

    String keyPrefix() default "rate_limit";
}

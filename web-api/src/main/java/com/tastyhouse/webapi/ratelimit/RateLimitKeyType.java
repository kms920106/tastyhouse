package com.tastyhouse.webapi.ratelimit;

public enum RateLimitKeyType {

    /** 클라이언트 IP 기반 */
    IP,

    /** 요청 본문의 phoneNumber 필드 기반 */
    PHONE,

    /** 요청 본문의 email 필드 기반 */
    EMAIL
}

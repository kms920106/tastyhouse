package com.tastyhouse.application.shared.error;

public final class ErrorContracts {

    private static final ErrorDescriptor RATE_LIMIT = new ErrorDescriptor(
        429, "RATE_LIMIT_EXCEEDED", "요청 횟수가 초과되었습니다. 잠시 후 다시 시도해주세요.");

    private static final ErrorDescriptor ACCESS_DENIED = new ErrorDescriptor(
        403, "ACCESS_DENIED", "접근 권한이 없습니다.");

    private static final ErrorDescriptor AUTH_REQUIRED = new ErrorDescriptor(
        401, "AUTH_REQUIRED", "인증이 필요합니다.");

    private ErrorContracts() {
    }

    public static ErrorDescriptor rateLimit() {
        return RATE_LIMIT;
    }

    public static ErrorDescriptor accessDenied() {
        return ACCESS_DENIED;
    }

    public static ErrorDescriptor authRequired() {
        return AUTH_REQUIRED;
    }
}

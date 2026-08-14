package com.tastyhouse.apicommon.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * 요청의 클라이언트 IP를 판별하는 static 유틸.
 *
 * <p>프록시·로드밸런서 뒤에서는 {@code getRemoteAddr()}가 프록시의 IP를 돌려주므로,
 * {@code X-Forwarded-For}의 첫 값(원 클라이언트)을 우선한다.
 *
 * <p><b>logging-module의 {@code ApiLoggingFilter#resolveClientIp}와 로직이 같지만 통합하지 않는다</b> —
 * logging-module은 api-common-module을 의존하지 않고, 의존을 추가하면 방향이 뒤집힌다(로깅은 api 계층
 * 아래에 있는 횡단 관심사다). 중복을 감수하는 대신 양쪽에 이 사유를 주석으로 남긴다.
 */
public final class ClientIpResolver {

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    private ClientIpResolver() {
    }

    /**
     * 클라이언트 IP를 판별한다. 판별할 수 없으면 {@code getRemoteAddr()}의 값을 그대로 돌려준다.
     */
    public static String resolve(HttpServletRequest request) {
        String xForwardedFor = request.getHeader(X_FORWARDED_FOR);
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

package com.tastyhouse.domain.ceo.service;

import com.tastyhouse.domain.ceo.model.CeoLoginFailureReason;
import com.tastyhouse.domain.ceo.model.CeoLoginHistory;
import com.tastyhouse.domain.ceo.model.CeoLoginResult;
import com.tastyhouse.domain.ceo.repository.CeoLoginHistoryRepository;
import com.tastyhouse.domain.ceo.vo.CeoId;

/**
 * 점주 로그인 이력 기록을 소유하는 도메인 서비스.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며, 빈 등록은 infrastructure-module의
 * {@code CeoDomainConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는 ceo-api의
 * {@code CeoLoginHistoryCommandService}가 선언한다.
 *
 * <p>{@code userAgent} 절단을 여기서 수행하는 이유: 컬럼 길이({@value #USER_AGENT_MAX_LENGTH}자)는
 * 저장 계약이므로 호출부마다 자르게 두면 한 곳만 빠져도 저장 시점에 실패한다. 기록의 유일한 입구인
 * 여기서 정규화하면 그 실수가 구조적으로 불가능해진다.
 */
public class CeoLoginHistoryRecorder {

    /** {@code CEO_LOGIN_HISTORY.user_agent} 컬럼 길이. 초과분은 절단해 저장한다. */
    private static final int USER_AGENT_MAX_LENGTH = 500;

    private final CeoLoginHistoryRepository ceoLoginHistoryRepository;

    public CeoLoginHistoryRecorder(CeoLoginHistoryRepository ceoLoginHistoryRepository) {
        this.ceoLoginHistoryRepository = ceoLoginHistoryRepository;
    }

    /**
     * 로그인 성공 이력 1행을 기록한다. 성공에는 실패 사유가 없으므로 {@code failureReason}은 null이다.
     */
    public void recordSuccess(CeoId ceoId, String ipAddress, String userAgent) {
        record(ceoId, CeoLoginResult.SUCCESS, null, ipAddress, userAgent);
    }

    /**
     * 로그인 실패 이력 1행을 기록한다.
     *
     * <p>존재하지 않는 아이디로의 시도는 호출부에서 걸러 여기까지 오지 않는다 — 귀속할 {@code ceoId}가
     * 없기 때문이다.
     */
    public void recordFailure(
        CeoId ceoId,
        CeoLoginFailureReason failureReason,
        String ipAddress,
        String userAgent
    ) {
        record(ceoId, CeoLoginResult.FAILURE, failureReason, ipAddress, userAgent);
    }

    private void record(
        CeoId ceoId,
        CeoLoginResult result,
        CeoLoginFailureReason failureReason,
        String ipAddress,
        String userAgent
    ) {
        CeoLoginHistory history = CeoLoginHistory.of(
            ceoId,
            result,
            failureReason,
            ipAddress,
            truncateUserAgent(userAgent)
        );
        ceoLoginHistoryRepository.save(history);
    }

    /**
     * User-Agent를 컬럼 길이에 맞춰 자른다. 브라우저 UA 문자열은 500자를 넘는 경우가 드물지만, 봇·임의
     * 클라이언트는 얼마든지 길게 보낼 수 있으므로 저장 전에 반드시 정규화한다.
     */
    private String truncateUserAgent(String userAgent) {
        if (userAgent == null || userAgent.length() <= USER_AGENT_MAX_LENGTH) {
            return userAgent;
        }
        return userAgent.substring(0, USER_AGENT_MAX_LENGTH);
    }
}

package com.tastyhouse.batch.scheduler;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.service.GradeSettlementService;

/**
 * 회원 등급 산정 배치 서비스.
 *
 * <p>등급 판정 규칙 자체는 도메인 서비스({@link GradeSettlementService})가 갖고 있고, 이 서비스는 배치
 * 트리거의 트랜잭션 경계만 선언한다(공통 지침 패턴 1 — 도메인 서비스는 {@code @Transactional}을 갖지
 * 않는다). {@code RankSchedulerService}와 같은 형태다.
 */
@Service
public class GradeSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(GradeSchedulerService.class);

    private final GradeSettlementService gradeSettlementService;

    public GradeSchedulerService(GradeSettlementService gradeSettlementService) {
        this.gradeSettlementService = gradeSettlementService;
    }

    /**
     * 모든 회원의 등급을 리뷰 개수 기준으로 업데이트한다.
     */
    @Transactional
    public void updateAllMemberGrades() {
        long updated = gradeSettlementService.settleAll(LocalDateTime.now());

        log.info("회원 등급 업데이트 완료: 총 {} 명", updated);
    }
}

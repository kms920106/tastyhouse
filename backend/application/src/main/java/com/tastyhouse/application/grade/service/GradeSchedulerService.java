package com.tastyhouse.application.grade.service;

import com.tastyhouse.application.shared.marker.BatchApp;
import com.tastyhouse.application.grade.port.in.SettleMemberGradesUseCase;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.service.GradeSettlementService;

@Service
@BatchApp
public class GradeSchedulerService implements SettleMemberGradesUseCase {

    private static final Logger log = LoggerFactory.getLogger(GradeSchedulerService.class);

    private final GradeSettlementService gradeSettlementService;

    public GradeSchedulerService(GradeSettlementService gradeSettlementService) {
        this.gradeSettlementService = gradeSettlementService;
    }

    @Transactional
    @Override
    public void updateAllMemberGrades() {
        long updated = gradeSettlementService.settleAll(LocalDateTime.now());

        log.info("회원 등급 업데이트 완료: 총 {} 명", updated);
    }
}

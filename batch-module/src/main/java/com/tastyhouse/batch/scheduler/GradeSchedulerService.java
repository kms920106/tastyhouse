package com.tastyhouse.batch.scheduler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.domain.model.MemberGrade;
import com.tastyhouse.domain.member.domain.repository.MemberRepository;
import com.tastyhouse.infrastructure.review.query.MemberReviewCountQueryDao;
import com.tastyhouse.infrastructure.review.query.MemberReviewCountResult;

@Service
public class GradeSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(GradeSchedulerService.class);

    private final MemberReviewCountQueryDao memberReviewCountQueryDao;
    private final MemberRepository memberRepository;

    public GradeSchedulerService(
        MemberReviewCountQueryDao memberReviewCountQueryDao,
        MemberRepository memberRepository
    ) {
        this.memberReviewCountQueryDao = memberReviewCountQueryDao;
        this.memberRepository = memberRepository;
    }

    /**
     * 모든 회원의 등급을 리뷰 개수 기준으로 업데이트
     */
    @Transactional
    public void updateAllMemberGrades() {
        log.info("=== 회원 등급 업데이트 시작 ===");

        // 전체 기간 리뷰 개수 조회
        LocalDateTime startDate = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.now();

        List<MemberReviewCountResult> reviewCounts = memberReviewCountQueryDao.countReviewsByMemberWithPeriod(startDate, endDate);

        log.info("리뷰 작성 회원 수: {}", reviewCounts.size());

        // 등급별로 회원 ID 그룹핑
        Map<MemberGrade, List<Long>> gradeGroups = groupMembersByGrade(reviewCounts);

        // 등급별로 벌크 업데이트
        long totalUpdated = 0;
        for (Map.Entry<MemberGrade, List<Long>> entry : gradeGroups.entrySet()) {
            MemberGrade grade = entry.getKey();
            List<Long> memberIds = entry.getValue();

            if (!memberIds.isEmpty()) {
                long updated = updateMemberGrades(memberIds, grade);
                totalUpdated += updated;
                log.info("등급 업데이트: {} - {} 명", grade.getDisplayName(), updated);
            }
        }

        log.info("=== 회원 등급 업데이트 완료: 총 {} 명 ===", totalUpdated);
    }

    /**
     * 리뷰 개수에 따라 회원을 등급별로 그룹핑
     */
    private Map<MemberGrade, List<Long>> groupMembersByGrade(List<MemberReviewCountResult> reviewCounts) {
        Map<MemberGrade, List<Long>> gradeGroups = new HashMap<>();

        for (MemberGrade grade : MemberGrade.values()) {
            gradeGroups.put(grade, reviewCounts.stream()
                .filter(dto -> MemberGrade.fromReviewCount(dto.reviewCount().intValue()) == grade)
                .map(dto -> dto.memberId().value())
                .collect(Collectors.toList()));
        }

        return gradeGroups;
    }

    /**
     * 특정 회원들의 등급을 벌크 업데이트
     */
    private long updateMemberGrades(List<Long> memberIds, MemberGrade grade) {
        if (memberIds.isEmpty()) {
            return 0;
        }

        return memberRepository.bulkUpdateGrade(memberIds, grade);
    }
}

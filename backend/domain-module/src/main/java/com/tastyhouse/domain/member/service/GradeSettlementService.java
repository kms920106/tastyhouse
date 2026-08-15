package com.tastyhouse.domain.member.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.tastyhouse.domain.member.model.MemberGrade;
import com.tastyhouse.domain.member.port.MemberReviewCount;
import com.tastyhouse.domain.member.port.MemberReviewCountPort;
import com.tastyhouse.domain.member.repository.MemberRepository;

/**
 * 회원 등급 확정(도메인 서비스).
 *
 * <p>등급 산정은 "전체 기간 회원별 리뷰 수 조회 → 리뷰 수로 등급 판정 → 등급별 일괄 갱신"이 한 규칙으로
 * 묶인 정책이다. 판정 기준(리뷰 수 → 등급) 자체는 {@link MemberGrade#fromReviewCount(int)}가 소유하고,
 * 이 서비스는 그 정책을 전 회원에 적용하는 오케스트레이션을 담당한다.
 *
 * <p>과거에는 이 로직이 batch-module의 스케줄러 서비스 본문에 인라인으로 있었고 데이터 접근도
 * infrastructure의 query DAO를 직접 주입했다. 등급 정책이 도메인 밖에 있으면 순수 단위 테스트가
 * 불가능하고, 같은 batch의 다른 잡들(랭킹·인기검색어)이 도메인 서비스를 경유하는 것과도 어긋난다.
 * 데이터 접근은 출력 포트({@link MemberReviewCountPort})를 경유하며, 집계 조회 구현은 랭킹 집계와 같은
 * infrastructure DAO 하나를 공유한다 — 같은 "회원별 리뷰 수 집계" SQL을 두 벌로 두지 않기 위함이다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code MemberDomainConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는
 * batch의 스케줄러 서비스가 선언한다.
 */
public class GradeSettlementService {

    /**
     * 전체 기간 집계의 시작 시각 — 서비스 개시 이전으로 충분히 이른 고정값.
     */
    private static final LocalDateTime ALL_TIME_START = LocalDateTime.of(2000, 1, 1, 0, 0, 0);

    private final MemberReviewCountPort memberReviewCountPort;
    private final MemberRepository memberRepository;

    public GradeSettlementService(
        MemberReviewCountPort memberReviewCountPort,
        MemberRepository memberRepository
    ) {
        this.memberReviewCountPort = memberReviewCountPort;
        this.memberRepository = memberRepository;
    }

    /**
     * 리뷰를 작성한 전 회원의 등급을 전체 기간 리뷰 수 기준으로 재산정한다.
     *
     * @param now 집계 종료 시각(현재 시각) — 시계를 도메인에 두지 않도록 호출부가 넘긴다
     * @return 등급이 갱신된 회원 수
     */
    public long settleAll(LocalDateTime now) {
        List<MemberReviewCount> reviewCounts =
            memberReviewCountPort.countReviewsByMemberWithPeriod(ALL_TIME_START, now);

        long totalUpdated = 0;
        for (Map.Entry<MemberGrade, List<Long>> entry : groupMembersByGrade(reviewCounts).entrySet()) {
            List<Long> memberIds = entry.getValue();
            if (!memberIds.isEmpty()) {
                totalUpdated += memberRepository.bulkUpdateGrade(memberIds, entry.getKey());
            }
        }

        return totalUpdated;
    }

    /**
     * 리뷰 수로 판정한 등급별로 회원 ID를 묶는다.
     */
    private Map<MemberGrade, List<Long>> groupMembersByGrade(List<MemberReviewCount> reviewCounts) {
        Map<MemberGrade, List<Long>> gradeGroups = new EnumMap<>(MemberGrade.class);
        for (MemberGrade grade : MemberGrade.values()) {
            gradeGroups.put(grade, new ArrayList<>());
        }

        for (MemberReviewCount reviewCount : reviewCounts) {
            MemberGrade grade = MemberGrade.fromReviewCount(reviewCount.reviewCount().intValue());
            gradeGroups.get(grade).add(reviewCount.memberId().value());
        }

        return gradeGroups;
    }
}

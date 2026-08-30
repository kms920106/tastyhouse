package com.tastyhouse.infrastructure.member.adapter;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.member.port.MemberReviewCount;
import com.tastyhouse.domain.member.port.MemberReviewCountPort;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.infrastructure.review.query.MemberReviewCountQueryDao;
import com.tastyhouse.application.review.port.out.MemberReviewCountResult;

/**
 * 등급 산정용 리뷰 수 조회 포트({@link MemberReviewCountPort}) 어댑터.
 *
 * <p>리뷰 집계 조회 자체는 리뷰 도메인 소유이므로 {@link MemberReviewCountQueryDao}에 두고, 이 어댑터는
 * 그 결과를 member 도메인이 이해하는 값 타입({@link MemberReviewCount})으로 옮겨 담는 변환만 담당한다.
 * rank 컨텍스트의 {@code MemberReviewCountAdapter}와 같은 DAO를 공유하며, 컨텍스트 순환을 피하려고
 * 포트·값 타입만 컨텍스트별로 나뉜다(member 포트 Javadoc 참고).
 *
 * <p><strong>클래스명에 {@code Grade}가 붙은 이유</strong>: rank 쪽 어댑터와 단순 클래스명이 같으면
 * 스프링이 유도하는 기본 빈 이름({@code memberReviewCountAdapter})이 충돌해 컴포넌트 스캔이
 * {@code ConflictingBeanDefinitionException}으로 거부하고 앱이 부팅하지 못한다. 두 어댑터는 주입이
 * 타입 기반(서로 다른 컨텍스트의 {@code MemberReviewCountPort})이라 이름을 나눠도 참조가 깨지지 않으며,
 * 용도({@code 등급 산정} vs {@code 랭킹 집계})가 이름에 드러나는 편이 탐색에도 낫다.
 *
 * <p><strong>패키지가 {@code ..persistence..}가 아니라 {@code ..adapter..}인 이유</strong>: 이 클래스는 write
 * 어댑터가 아니라 <em>도메인 출력 포트 구현</em>이라 read model({@code review/query/})을 재사용하는 것이
 * 정상이다. {@code ..persistence..}에 두면 read→write 단방향 규칙({@code LayerRulesTest
 * #persistenceShouldNotDependOnQuery})에 걸리는데, 그 규칙의 봉인 목록은 "줄어들기만 해야" 하므로 새
 * 항목을 추가하지 않고 그 Javadoc이 제시한 해소 방향(포트 어댑터를 {@code ..persistence..} 밖으로)을
 * 따른다.
 */
@Component
public class MemberGradeReviewCountAdapter implements MemberReviewCountPort {

    private final MemberReviewCountQueryDao memberReviewCountQueryDao;

    public MemberGradeReviewCountAdapter(MemberReviewCountQueryDao memberReviewCountQueryDao) {
        this.memberReviewCountQueryDao = memberReviewCountQueryDao;
    }

    @Override
    public List<MemberReviewCount> countReviewsByMemberWithPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return memberReviewCountQueryDao.countReviewsByMemberWithPeriod(startDate, endDate).stream()
            .map(this::toMemberReviewCount)
            .toList();
    }

    private MemberReviewCount toMemberReviewCount(MemberReviewCountResult result) {
        return MemberReviewCount.of(
            MemberId.of(result.memberId()),
            result.reviewCount()
        );
    }
}

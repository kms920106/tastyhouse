package com.tastyhouse.infrastructure.rank.persistence;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.tastyhouse.domain.rank.domain.port.MemberReviewCount;
import com.tastyhouse.domain.rank.domain.port.MemberReviewCountPort;
import com.tastyhouse.infrastructure.review.query.MemberReviewCountQueryDao;
import com.tastyhouse.infrastructure.review.query.MemberReviewCountResult;

/**
 * 랭킹 집계용 리뷰 수 조회 포트({@link MemberReviewCountPort}) 어댑터.
 *
 * <p>리뷰 집계 조회 자체는 리뷰 도메인 소유이므로 {@link MemberReviewCountQueryDao}에 두고, 이 어댑터는
 * 그 결과를 랭킹 도메인이 이해하는 값 타입({@link MemberReviewCount})으로 옮겨 담는 변환만 담당한다.
 * 덕분에 랭킹 도메인 서비스는 리뷰 도메인의 read model이나 QueryDSL을 알지 않는다.
 */
@Component
@RequiredArgsConstructor
public class MemberReviewCountAdapter implements MemberReviewCountPort {

    private final MemberReviewCountQueryDao memberReviewCountQueryDao;

    @Override
    public List<MemberReviewCount> countReviewsByMemberWithPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return memberReviewCountQueryDao.countReviewsByMemberWithPeriod(startDate, endDate).stream()
            .map(this::toMemberReviewCount)
            .toList();
    }

    private MemberReviewCount toMemberReviewCount(MemberReviewCountResult result) {
        return MemberReviewCount.of(
            result.memberId(),
            result.reviewCount(),
            result.lastReviewAt()
        );
    }
}

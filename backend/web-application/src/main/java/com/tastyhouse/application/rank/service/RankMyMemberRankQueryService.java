package com.tastyhouse.application.rank.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.rank.model.RankType;
import com.tastyhouse.application.member.port.out.MemberQueryPort;
import com.tastyhouse.application.member.port.out.MemberWithProfileImageResult;
import com.tastyhouse.application.rank.port.in.RankMyMemberRankQueryUseCase;
import com.tastyhouse.application.rank.port.out.MemberRankResult;
import com.tastyhouse.application.rank.port.out.RankQueryPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
class RankMyMemberRankQueryService implements RankMyMemberRankQueryUseCase {

    private final RankQueryPort rankQueryPort;
    private final MemberQueryPort memberQueryPort;

    public RankMyMemberRankQueryService(RankQueryPort rankQueryPort, MemberQueryPort memberQueryPort) {
        this.rankQueryPort = rankQueryPort;
        this.memberQueryPort = memberQueryPort;
    }

    @Override
    public MemberRankResult getMyMemberRank(Long memberId, String rankType) {
        RankType type = parseRankType(rankType);
        LocalDate baseDate = LocalDate.now();
        MemberId id = MemberId.of(memberId);

        return rankQueryPort.findMemberRank(memberId, type.name(), baseDate)
            .orElseGet(() -> unrankedMemberResult(id));
    }

    private MemberRankResult unrankedMemberResult(MemberId memberId) {
        MemberWithProfileImageResult member = memberQueryPort.findMemberWithProfileImageById(memberId.value())
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.MEMBER_NOT_FOUND));

        return new MemberRankResult(
            memberId.value(),
            member.nickname(),
            member.profileImageUrl(),
            0,
            null,
            member.memberGrade()
        );
    }

    private RankType parseRankType(String rankType) {
        try {
            return RankType.valueOf(rankType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return RankType.ALL;
        }
    }
}

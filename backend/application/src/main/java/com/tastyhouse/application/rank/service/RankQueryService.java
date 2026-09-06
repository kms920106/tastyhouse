package com.tastyhouse.application.rank.service;

import com.tastyhouse.application.shared.marker.WebApp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.rank.model.RankType;
import com.tastyhouse.application.member.port.out.MemberQueryPort;
import com.tastyhouse.application.member.port.out.MemberWithProfileImageResult;
import com.tastyhouse.application.rank.port.out.MemberRankResult;
import com.tastyhouse.application.rank.port.out.RankDurationResult;
import com.tastyhouse.application.rank.port.out.RankPrizeResult;
import com.tastyhouse.application.rank.port.out.RankQueryPort;
import com.tastyhouse.application.rank.port.in.RankQueryUseCase;

@Service
@WebApp
@Transactional(readOnly = true)
public class RankQueryService implements RankQueryUseCase {

    private final RankQueryPort rankQueryPort;
    private final MemberQueryPort memberQueryPort;

    public RankQueryService(RankQueryPort rankQueryPort, MemberQueryPort memberQueryPort) {
        this.rankQueryPort = rankQueryPort;
        this.memberQueryPort = memberQueryPort;
    }

    @Override
    public Optional<RankDurationResult> getDuration() {
        return rankQueryPort.findActiveDuration();
    }

    @Override
    public List<RankPrizeResult> getPrizes() {
        return rankQueryPort.findActivePrizes();
    }

    @Override
    public List<MemberRankResult> getMemberRankList(String rankType, int limit) {
        RankType type = parseRankType(rankType);
        LocalDate baseDate = LocalDate.now();

        return rankQueryPort.findMemberRanks(type, baseDate, limit);
    }

    @Override
    public MemberRankResult getMyMemberRank(Long memberId, String rankType) {
        RankType type = parseRankType(rankType);
        LocalDate baseDate = LocalDate.now();
        MemberId id = MemberId.of(memberId);

        return rankQueryPort.findMemberRank(memberId, type, baseDate)
            .orElseGet(() -> unrankedMemberResult(id));
    }

    private MemberRankResult unrankedMemberResult(MemberId memberId) {
        MemberWithProfileImageResult member = memberQueryPort.findMemberWithProfileImageById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

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

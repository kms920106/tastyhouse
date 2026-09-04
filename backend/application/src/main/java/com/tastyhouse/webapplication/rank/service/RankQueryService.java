package com.tastyhouse.webapplication.rank.service;

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
import com.tastyhouse.webapplication.rank.port.in.RankQueryUseCase;

/**
 * 랭킹 조회 서비스(web).
 *
 * <p>랭킹은 web에서 조회 전용이므로(집계·기간·경품 관리는 admin/batch 몫) CommandService 없이
 * QueryService만 둔다. 읽기 포트({@link RankQueryPort})만 주입해 조회 결과를 그대로 반환한다(패턴 2/3).
 * 표현 계약(Response) 조립은 web-api 컨트롤러의 책임이다. 랭킹 경품·회원 랭킹의 이미지 URL은 DAO가
 * 완성해 주므로 여기서는 값을 그대로 전달한다.
 *
 * <p>내 랭킹 조회는 랭킹에 들지 못한 회원도 자기 정보를 볼 수 있어야 하므로, 랭킹 행이 없으면
 * {@link MemberQueryPort}로 회원 프로필만 읽어 리뷰 0건·순위 없음인 {@link MemberRankResult}를 만들어
 * 돌려준다. 프로필 이미지는 그 DAO가 표시용 URL까지 변환해 담으므로 여기서는 값을 그대로 전달한다.
 */
@Service
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

    /**
     * 아직 랭킹에 들지 못한 회원의 결과 — 프로필만 채우고 리뷰 수 0·순위 없음으로 내려준다.
     */
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

    /**
     * 알 수 없는 타입 문자열은 전체 랭킹으로 관대하게 처리한다(기존 동작 보존 — 랭킹 화면 진입이
     * 400으로 실패하지 않게 한다). 그래서 {@code RankType.from}의 엄격 변환을 쓰지 않는다.
     */
    private RankType parseRankType(String rankType) {
        try {
            return RankType.valueOf(rankType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return RankType.ALL;
        }
    }
}

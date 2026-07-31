package com.tastyhouse.webapi.rank;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.rank.domain.model.RankType;
import com.tastyhouse.domain.exception.EntityNotFoundException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.infrastructure.member.query.MemberQueryDao;
import com.tastyhouse.infrastructure.member.query.MemberWithProfileImageResult;
import com.tastyhouse.infrastructure.rank.query.MemberRankResult;
import com.tastyhouse.infrastructure.rank.query.RankPrizeResult;
import com.tastyhouse.infrastructure.rank.query.RankQueryDao;
import com.tastyhouse.webapi.file.FileService;
import com.tastyhouse.webapi.rank.response.RankDurationResponse;
import com.tastyhouse.webapi.rank.response.RankMemberListItemResponse;
import com.tastyhouse.webapi.rank.response.RankPrizeListItemResponse;

/**
 * 랭킹 조회 서비스(web).
 *
 * <p>랭킹은 web에서 조회 전용이므로(집계·기간·경품 관리는 admin/batch 몫) CommandService 없이
 * QueryService만 둔다. infra read 어댑터({@link RankQueryDao})만 주입해 조회하고 Response를 조립한다
 * (패턴 2/3).
 *
 * <p>내 랭킹 조회는 랭킹에 들지 못한 회원도 자기 정보를 볼 수 있어야 하므로, 랭킹 행이 없으면
 * {@link MemberQueryDao}로 회원 프로필만 읽어 리뷰 0건·순위 없음으로 응답한다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RankQueryService {

    private final RankQueryDao rankQueryDao;
    private final MemberQueryDao memberQueryDao;
    private final FileService fileService;

    public Optional<RankDurationResponse> getDuration() {
        return rankQueryDao.findActiveDuration()
            .map(dto -> RankDurationResponse.from(dto.startAt(), dto.endAt()));
    }

    public List<RankPrizeListItemResponse> getPrizes() {
        return rankQueryDao.findActivePrizes().stream()
            .map(this::toPrizeListItemResponse)
            .toList();
    }

    public List<RankMemberListItemResponse> getMemberRankList(String rankType, int limit) {
        RankType type = parseRankType(rankType);
        LocalDate baseDate = LocalDate.now();

        return rankQueryDao.findMemberRanks(type, baseDate, limit).stream()
            .map(this::toMemberListItemResponse)
            .toList();
    }

    public RankMemberListItemResponse getMyMemberRank(Long memberId, String rankType) {
        RankType type = parseRankType(rankType);
        LocalDate baseDate = LocalDate.now();
        MemberId id = MemberId.of(memberId);

        return rankQueryDao.findMemberRank(id, type, baseDate)
            .map(this::toMemberListItemResponse)
            .orElseGet(() -> toUnrankedMemberResponse(id));
    }

    /**
     * 아직 랭킹에 들지 못한 회원의 응답 — 프로필만 채우고 리뷰 수 0·순위 없음으로 내려준다.
     */
    private RankMemberListItemResponse toUnrankedMemberResponse(MemberId memberId) {
        MemberWithProfileImageResult member = memberQueryDao.findMemberWithProfileImageById(memberId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        return RankMemberListItemResponse.of(
            memberId.value(),
            member.nickname(),
            fileService.getUrlByPath(member.profileImageFilePath()),
            0,
            null,
            member.memberGrade().name()
        );
    }

    private RankMemberListItemResponse toMemberListItemResponse(MemberRankResult dto) {
        return RankMemberListItemResponse.of(
            dto.memberId().value(),
            dto.nickname(),
            fileService.getUrlByPath(dto.profileImageFilePath()),
            dto.reviewCount(),
            dto.rankNo(),
            dto.grade().name()
        );
    }

    private RankPrizeListItemResponse toPrizeListItemResponse(RankPrizeResult dto) {
        return RankPrizeListItemResponse.from(
            dto.id(),
            dto.prizeRank(),
            dto.name(),
            dto.brand(),
            fileService.getUrlByPath(dto.imageFilePath())
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

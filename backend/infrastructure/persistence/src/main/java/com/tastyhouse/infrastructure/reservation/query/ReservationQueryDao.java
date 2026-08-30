package com.tastyhouse.infrastructure.reservation.query;

import com.tastyhouse.application.reservation.port.out.ReservationQueryPort;
import com.tastyhouse.application.reservation.port.out.ReservationDetailResult;
import com.tastyhouse.application.reservation.port.out.ReservationResult;
import com.tastyhouse.application.reservation.port.out.SlotOccupancyResult;
import com.querydsl.core.types.Projections;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.reservation.model.ReservationStatus;
import com.tastyhouse.domain.reservation.vo.ReservationId;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.member.persistence.QMemberJpaEntity.memberJpaEntity;
import static com.tastyhouse.infrastructure.reservation.persistence.QReservationJpaEntity.reservationJpaEntity;
import static com.tastyhouse.infrastructure.reservation.persistence.QReservationSlotJpaEntity.reservationSlotJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;

/**
 * 예약 read 어댑터(CQRS query 측).
 *
 * <p>표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영한다. 도메인 모델을 거치지 않으므로 write
 * 포트({@code ReservationRepository}/{@code ReservationSlotRepository})와 역할이 겹치지 않는다. 소비
 * 모듈(web-api)의 {@code ReservationQueryService}가 이 DAO를 주입해 사용하며, 그 덕분에 api 모듈은
 * QueryDSL을 알지 않는다.
 *
 * <p>과거 core {@code ReservationQueryService}는 예약을 도메인 모델로 읽은 뒤 가게를 건당 다시 조회해
 * 상호명·이미지 경로를 채웠다(목록 크기만큼 반복 조회). 이 DAO는 가게·파일을 join으로 함께 투영해 그 반복
 * 조회를 없앤다.
 *
 * <p>도메인당 DAO 1개 원칙에 따라 소비자별 메서드를 이 한 클래스에 둔다. 내 예약 목록
 * ({@code findReservationsByMemberId})과 가게별 예약 목록({@code findReservationsByShopId})은
 * 시그니처로 구분하며 점주/admin 마커는 붙이지 않는다.
 *
 * <p>가게 대표 이미지는 조인으로 얻은 저장 경로를 {@link FileUrlResolver}로 표시용 URL까지 변환해
 * Result에 담는다 — {@code Projections.constructor}는 record 생성자로 직접 투영하므로 변환을 투영식에 끼울 수
 * 없어, fetch 직후 재조립한다.
 */
@Repository
public class ReservationQueryDao implements ReservationQueryPort {

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ReservationQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    /**
     * 내 예약 목록 — 최근 예약 일시 순.
     */
    @Override
    public List<ReservationResult> findReservationsByMemberId(Long memberId) {
        return reservationQuery()
            .where(reservationJpaEntity.memberId.eq(memberId))
            .orderBy(reservationJpaEntity.reservationDate.desc(), reservationJpaEntity.reservationTime.desc())
            .fetch()
            .stream()
            .map(this::withResolvedShopImageUrl)
            .toList();
    }

    /**
     * 특정 가게의 예약 목록(점주 화면) — 최근 예약 일시 순.
     */
    @Override
    public List<ReservationResult> findReservationsByShopId(Long shopId) {
        return reservationQuery()
            .where(reservationJpaEntity.shopId.eq(shopId))
            .orderBy(reservationJpaEntity.reservationDate.desc(), reservationJpaEntity.reservationTime.desc())
            .fetch()
            .stream()
            .map(this::withResolvedShopImageUrl)
            .toList();
    }

    /**
     * 예약 단건(가게 정보만) — 예약 완료 화면이 소비한다. 없으면 비어 있다.
     */
    @Override
    public Optional<ReservationResult> findReservationById(ReservationId id) {
        return Optional.ofNullable(
                reservationQuery()
                    .where(reservationJpaEntity.id.eq(id.value()))
                    .fetchOne()
            )
            .map(this::withResolvedShopImageUrl);
    }

    /**
     * 예약 단건 상세(가게 정보 + 예약자 회원 정보) — 예약 상세 화면이 소비한다. 없으면 비어 있다.
     */
    @Override
    public Optional<ReservationDetailResult> findReservationDetailById(ReservationId id) {
        ReservationDetailResult result = queryFactory
            .select(Projections.constructor(ReservationDetailResult.class,
                reservationJpaEntity.id,
                reservationJpaEntity.shopId,
                shopJpaEntity.name,
                uploadedFileJpaEntity.filePath,
                shopJpaEntity.roadAddress,
                shopJpaEntity.lotAddress,
                reservationJpaEntity.memberId,
                memberJpaEntity.fullName,
                memberJpaEntity.phoneNumber.value,
                memberJpaEntity.username,
                reservationJpaEntity.reservationDate,
                reservationJpaEntity.reservationTime,
                reservationJpaEntity.partySize,
                reservationJpaEntity.status,
                reservationJpaEntity.request,
                reservationJpaEntity.createdAt
            ))
            .from(reservationJpaEntity)
            .innerJoin(shopJpaEntity).on(shopJpaEntity.id.eq(reservationJpaEntity.shopId))
            .innerJoin(memberJpaEntity).on(memberJpaEntity.id.eq(reservationJpaEntity.memberId))
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(shopThumbnailImageFileId()))
            .where(reservationJpaEntity.id.eq(id.value()))
            .fetchOne();

        return Optional.ofNullable(result).map(this::withResolvedShopImageUrl);
    }

    /**
     * 특정 가게·날짜에 <b>행이 존재하는</b> 슬롯의 잔여 수. 행이 없는 시간대는 예약 0건이므로 결과에 없다
     * (소비 측이 전체 슬롯 목록과 병합해 기본 정원으로 채운다).
     */
    @Override
    public List<SlotOccupancyResult> findSlotOccupancies(Long shopId, LocalDate date) {
        return queryFactory
            .select(Projections.constructor(SlotOccupancyResult.class,
                reservationSlotJpaEntity.slotTime,
                reservationSlotJpaEntity.capacity.subtract(reservationSlotJpaEntity.reservedCount)
            ))
            .from(reservationSlotJpaEntity)
            .where(
                reservationSlotJpaEntity.shopId.eq(shopId),
                reservationSlotJpaEntity.slotDate.eq(date)
            )
            .fetch();
    }

    /**
     * 회원이 그 가게·날짜에 재예약을 막는 예약을 이미 갖고 있는지. 가용성 화면에서 그 날짜 전체 슬롯을
     * 비활성화할지 판정하는 데 쓴다. 차단 대상 상태는 도메인이 소유하므로
     * {@link ReservationStatus#blockingStatuses()}를 그대로 참조한다(실제 차단 로직과 단일 원천 공유).
     */
    @Override
    public boolean existsBlockingReservation(Long memberId, Long shopId, LocalDate date) {
        return queryFactory.selectOne()
            .from(reservationJpaEntity)
            .where(
                reservationJpaEntity.memberId.eq(memberId),
                reservationJpaEntity.shopId.eq(shopId),
                reservationJpaEntity.reservationDate.eq(date),
                reservationJpaEntity.status.in(ReservationStatus.blockingStatuses())
            )
            .fetchFirst() != null;
    }

    /**
     * 예약 목록·단건이 공유하는 select + join 절. 가게는 필수(innerJoin), 썸네일 파일은 없을 수 있어 leftJoin.
     */
    private JPQLQuery<ReservationResult> reservationQuery() {
        return queryFactory
            .select(reservationProjection())
            .from(reservationJpaEntity)
            .innerJoin(shopJpaEntity).on(shopJpaEntity.id.eq(reservationJpaEntity.shopId))
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(shopThumbnailImageFileId()));
    }

    private ConstructorExpression<ReservationResult> reservationProjection() {
        return Projections.constructor(ReservationResult.class,
                reservationJpaEntity.id,
            reservationJpaEntity.shopId,
            shopJpaEntity.name,
            uploadedFileJpaEntity.filePath,
            shopJpaEntity.roadAddress,
            shopJpaEntity.lotAddress,
            reservationJpaEntity.memberId,
            reservationJpaEntity.reservationDate,
            reservationJpaEntity.reservationTime,
            reservationJpaEntity.partySize,
            reservationJpaEntity.status,
            reservationJpaEntity.request,
            reservationJpaEntity.createdAt
        );
    }

    /**
     * 투영된 저장 경로를 표시용 URL로 바꿔 재조립한다. 아래 두 메서드는 {@code Projections.constructor}가
     * 생성자 직접 투영이라 변환을 투영식에 넣을 수 없어 fetch 직후 호출한다.
     */
    private ReservationResult withResolvedShopImageUrl(ReservationResult row) {
        return new ReservationResult(
            row.id(),
            row.shopId(),
            row.shopName(),
            fileUrlResolver.resolve(row.shopImageUrl()),
            row.shopRoadAddress(),
            row.shopLotAddress(),
            row.memberId(),
            row.reservationDate(),
            row.reservationTime(),
            row.partySize(),
            row.status(),
            row.request(),
            row.createdAt()
        );
    }

    private ReservationDetailResult withResolvedShopImageUrl(ReservationDetailResult row) {
        return new ReservationDetailResult(
            row.id(),
            row.shopId(),
            row.shopName(),
            fileUrlResolver.resolve(row.shopImageUrl()),
            row.shopRoadAddress(),
            row.shopLotAddress(),
            row.memberId(),
            row.reserverName(),
            row.reserverPhoneNumber(),
            row.reserverEmail(),
            row.reservationDate(),
            row.reservationTime(),
            row.partySize(),
            row.status(),
            row.request(),
            row.createdAt()
        );
    }

    /**
     * {@code @Convert} VO 컬럼인 {@code SHOP.thumbnail_image_file_id}를 raw {@code Long}으로 비교하기
     * 위한 path(shop 도메인의 크로스 참조).
     */
    private NumberPath<Long> shopThumbnailImageFileId() {
        return Expressions.numberPath(Long.class, shopJpaEntity, "thumbnailImageFileId");
    }
}

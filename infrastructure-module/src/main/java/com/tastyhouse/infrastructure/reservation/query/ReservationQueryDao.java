package com.tastyhouse.infrastructure.reservation.query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.reservation.domain.model.ReservationStatus;
import com.tastyhouse.core.domain.reservation.domain.vo.ReservationId;

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
 */
@Repository
@RequiredArgsConstructor
public class ReservationQueryDao {

    /**
     * 재예약을 차단하는 예약 상태 — 취소·거절분은 차단 대상이 아니다.
     */
    private static final List<ReservationStatus> BLOCKING_STATUSES = List.of(
        ReservationStatus.PENDING,
        ReservationStatus.CONFIRMED,
        ReservationStatus.COMPLETED
    );

    private final JPAQueryFactory queryFactory;

    /**
     * 내 예약 목록 — 최근 예약 일시 순.
     */
    public List<ReservationResult> findReservationsByMemberId(MemberId memberId) {
        return reservationQuery()
            .where(reservationJpaEntity.memberId.eq(memberId))
            .orderBy(reservationJpaEntity.reservationDate.desc(), reservationJpaEntity.reservationTime.desc())
            .fetch();
    }

    /**
     * 특정 가게의 예약 목록(점주 화면) — 최근 예약 일시 순.
     */
    public List<ReservationResult> findReservationsByShopId(Long shopId) {
        return reservationQuery()
            .where(reservationJpaEntity.shopId.eq(shopId))
            .orderBy(reservationJpaEntity.reservationDate.desc(), reservationJpaEntity.reservationTime.desc())
            .fetch();
    }

    /**
     * 예약 단건(가게 정보만) — 예약 완료 화면이 소비한다. 없으면 비어 있다.
     */
    public Optional<ReservationResult> findReservationById(ReservationId id) {
        return Optional.ofNullable(
            reservationQuery()
                .where(reservationJpaEntity.id.eq(id.value()))
                .fetchOne()
        );
    }

    /**
     * 예약 단건 상세(가게 정보 + 예약자 회원 정보) — 예약 상세 화면이 소비한다. 없으면 비어 있다.
     */
    public Optional<ReservationDetailResult> findReservationDetailById(ReservationId id) {
        ReservationDetailResult result = queryFactory
            .select(new QReservationDetailResult(
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
            .innerJoin(memberJpaEntity).on(memberJpaEntity.id.eq(reservationMemberIdPath()))
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(shopJpaEntity.thumbnailImageFileId))
            .where(reservationJpaEntity.id.eq(id.value()))
            .fetchOne();

        return Optional.ofNullable(result);
    }

    /**
     * 특정 가게·날짜에 <b>행이 존재하는</b> 슬롯의 잔여 수. 행이 없는 시간대는 예약 0건이므로 결과에 없다
     * (소비 측이 전체 슬롯 목록과 병합해 기본 정원으로 채운다).
     */
    public List<SlotOccupancyResult> findSlotOccupancies(Long shopId, LocalDate date) {
        return queryFactory
            .select(new QSlotOccupancyResult(
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
     * 회원이 그 가게·날짜에 재예약을 막는 예약(PENDING/CONFIRMED/COMPLETED)을 이미 갖고 있는지.
     * 가용성 화면에서 그 날짜 전체 슬롯을 비활성화할지 판정하는 데 쓴다.
     */
    public boolean existsBlockingReservation(MemberId memberId, Long shopId, LocalDate date) {
        return queryFactory.selectOne()
            .from(reservationJpaEntity)
            .where(
                reservationJpaEntity.memberId.eq(memberId),
                reservationJpaEntity.shopId.eq(shopId),
                reservationJpaEntity.reservationDate.eq(date),
                reservationJpaEntity.status.in(BLOCKING_STATUSES)
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
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(shopJpaEntity.thumbnailImageFileId));
    }

    private ConstructorExpression<ReservationResult> reservationProjection() {
        return new QReservationResult(
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
     * 예약의 {@code memberId}는 {@code @Convert}로 {@code MemberId} VO에 매핑돼 있어 회원 테이블의 raw
     * {@code Long} PK와 직접 join할 수 없다. 같은 컬럼을 {@code Long} 경로로 다시 노출해 join에 쓴다.
     */
    private NumberPath<Long> reservationMemberIdPath() {
        return Expressions.numberPath(Long.class, reservationJpaEntity, "memberId");
    }
}

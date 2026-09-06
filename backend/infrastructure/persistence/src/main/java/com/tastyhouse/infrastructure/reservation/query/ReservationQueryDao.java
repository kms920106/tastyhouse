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

@Repository
public class ReservationQueryDao implements ReservationQueryPort {
    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ReservationQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

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

    @Override
    public Optional<ReservationResult> findReservationById(ReservationId id) {
        return Optional.ofNullable(
                reservationQuery()
                    .where(reservationJpaEntity.id.eq(id.value()))
                    .fetchOne()
            )
            .map(this::withResolvedShopImageUrl);
    }

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

    private NumberPath<Long> shopThumbnailImageFileId() {
        return Expressions.numberPath(Long.class, shopJpaEntity, "thumbnailImageFileId");
    }
}

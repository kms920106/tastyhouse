package com.tastyhouse.infrastructure.persistence.menureview.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.menureview.model.MenuReview;
import com.tastyhouse.domain.menureview.vo.MenuReviewId;
import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.application.menureview.port.out.write.MenuReviewLoadPort;
import com.tastyhouse.application.menureview.port.out.write.MenuReviewSavePort;

import static com.tastyhouse.infrastructure.persistence.menureview.persistence.QMenuReviewJpaEntity.menuReviewJpaEntity;

@Repository
class MenuReviewPersistenceAdapter implements MenuReviewLoadPort, MenuReviewSavePort {

    private final JPAQueryFactory queryFactory;
    private final MenuReviewJpaRepository menuReviewJpaRepository;

    public MenuReviewPersistenceAdapter(JPAQueryFactory queryFactory, MenuReviewJpaRepository menuReviewJpaRepository) {
        this.queryFactory = queryFactory;
        this.menuReviewJpaRepository = menuReviewJpaRepository;
    }

    @Override
    public Optional<MenuReview> findById(MenuReviewId menuReviewId) {
        return menuReviewJpaRepository.findById(menuReviewId.value())
            .map(MenuReviewMapper::toDomain);
    }

    @Override
    public Optional<MenuReview> findByIdAndMemberId(MenuReviewId menuReviewId, MemberId memberId) {
        MenuReviewJpaEntity entity = queryFactory
            .selectFrom(menuReviewJpaEntity)
            .where(
                menuReviewJpaEntity.id.eq(menuReviewId.value()),
                menuReviewJpaEntity.memberId.eq(memberId.value())
            )
            .fetchOne();

        return Optional.ofNullable(entity).map(MenuReviewMapper::toDomain);
    }

    @Override
    public boolean existsByOrderProductId(OrderProductId orderProductId) {
        return queryFactory
            .selectOne()
            .from(menuReviewJpaEntity)
            .where(menuReviewJpaEntity.orderProductId.eq(orderProductId.value()))
            .fetchFirst() != null;
    }

    @Override
    public MenuReview save(MenuReview menuReview) {
        if (menuReview.getId() == null) {
            MenuReviewJpaEntity saved = menuReviewJpaRepository.save(MenuReviewMapper.toEntity(menuReview));
            return MenuReviewMapper.toDomain(saved);
        }

        MenuReviewJpaEntity entity = menuReviewJpaRepository.findById(menuReview.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 메뉴 평가입니다: " + menuReview.getId()));
        MenuReviewMapper.applyChanges(entity, menuReview);
        return MenuReviewMapper.toDomain(entity);
    }

    @Override
    public void deleteById(MenuReviewId menuReviewId) {
        menuReviewJpaRepository.deleteById(menuReviewId.value());
    }
}

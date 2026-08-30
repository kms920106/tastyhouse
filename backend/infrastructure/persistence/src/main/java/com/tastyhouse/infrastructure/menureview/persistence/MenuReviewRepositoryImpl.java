package com.tastyhouse.infrastructure.menureview.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.menureview.model.MenuReview;
import com.tastyhouse.domain.menureview.repository.MenuReviewRepository;
import com.tastyhouse.domain.menureview.vo.MenuReviewId;
import com.tastyhouse.domain.order.vo.OrderProductId;

import static com.tastyhouse.infrastructure.menureview.persistence.QMenuReviewJpaEntity.menuReviewJpaEntity;

@Repository
public class MenuReviewRepositoryImpl implements MenuReviewRepository {

    private final JPAQueryFactory queryFactory;
    private final MenuReviewJpaRepository menuReviewJpaRepository;

    public MenuReviewRepositoryImpl(JPAQueryFactory queryFactory, MenuReviewJpaRepository menuReviewJpaRepository) {
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
        Integer result = queryFactory
            .selectOne()
            .from(menuReviewJpaEntity)
            .where(menuReviewJpaEntity.orderProductId.eq(orderProductId.value()))
            .fetchFirst();
        return result != null;
    }

    @Override
    public MenuReview save(MenuReview menuReview) {
        if (menuReview.getId() == null) {
            MenuReviewJpaEntity saved = menuReviewJpaRepository.save(MenuReviewMapper.toEntity(menuReview));
            return MenuReviewMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
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

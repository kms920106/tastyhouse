package com.tastyhouse.infrastructure.menureview.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.menureview.port.out.write.MenuReviewState;
import com.tastyhouse.application.menureview.port.out.write.MenuReviewStatePort;

import static com.tastyhouse.infrastructure.menureview.persistence.QMenuReviewJpaEntity.menuReviewJpaEntity;

@Repository
public class MenuReviewStatePortImpl implements MenuReviewStatePort {
    private final JPAQueryFactory queryFactory;
    private final MenuReviewJpaRepository menuReviewJpaRepository;

    public MenuReviewStatePortImpl(JPAQueryFactory queryFactory, MenuReviewJpaRepository menuReviewJpaRepository) {
        this.queryFactory = queryFactory;
        this.menuReviewJpaRepository = menuReviewJpaRepository;
    }

    @Override
    public Optional<MenuReviewState> findById(Long id) {
        return menuReviewJpaRepository.findById(id)
            .map(MenuReviewMapper::toState);
    }

    @Override
    public Optional<MenuReviewState> findByIdAndMemberId(Long id, Long memberId) {
        MenuReviewJpaEntity entity = queryFactory
            .selectFrom(menuReviewJpaEntity)
            .where(
                menuReviewJpaEntity.id.eq(id),
                menuReviewJpaEntity.memberId.eq(memberId)
            )
            .fetchOne();

        return Optional.ofNullable(entity).map(MenuReviewMapper::toState);
    }

    @Override
    public boolean existsByOrderProductId(Long orderProductId) {
        Integer result = queryFactory
            .selectOne()
            .from(menuReviewJpaEntity)
            .where(menuReviewJpaEntity.orderProductId.eq(orderProductId))
            .fetchFirst();
        return result != null;
    }

    @Override
    public MenuReviewState save(MenuReviewState state) {
        if (state.id() == null) {
            MenuReviewJpaEntity saved = menuReviewJpaRepository.save(MenuReviewMapper.toEntity(state));
            return MenuReviewMapper.toState(saved);
        }

        MenuReviewJpaEntity entity = menuReviewJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 메뉴 평가입니다: " + state.id()));
        MenuReviewMapper.applyChanges(entity, state);
        return MenuReviewMapper.toState(entity);
    }

    @Override
    public void deleteById(Long id) {
        menuReviewJpaRepository.deleteById(id);
    }
}

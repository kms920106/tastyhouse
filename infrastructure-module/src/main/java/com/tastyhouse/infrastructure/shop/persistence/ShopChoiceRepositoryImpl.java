package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.domain.model.ShopChoice;
import com.tastyhouse.domain.shop.domain.repository.ShopChoiceRepository;

/**
 * 에디터 추천 write 어댑터.
 *
 * <p>목록 페이징 조회({@code findEditorChoice})는 같은 모듈의
 * {@link com.tastyhouse.infrastructure.shop.query.ShopChoiceQueryDao}로 이관했다(공통 지침 패턴 4).
 */
@Repository
public class ShopChoiceRepositoryImpl implements ShopChoiceRepository {

    private final ShopChoiceJpaRepository shopChoiceJpaRepository;

    public ShopChoiceRepositoryImpl(ShopChoiceJpaRepository shopChoiceJpaRepository) {
        this.shopChoiceJpaRepository = shopChoiceJpaRepository;
    }

    @Override
    public Optional<ShopChoice> findById(Long id) {
        return shopChoiceJpaRepository.findById(id).map(ShopChoiceMapper::toDomain);
    }

    @Override
    public ShopChoice save(ShopChoice shopChoice) {
        if (shopChoice.getId() == null) {
            ShopChoiceJpaEntity saved = shopChoiceJpaRepository.save(ShopChoiceMapper.toEntity(shopChoice));
            return ShopChoiceMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회한 뒤 변경 필드만 복사해 dirty checking으로 flush.
        ShopChoiceJpaEntity entity = shopChoiceJpaRepository.findById(shopChoice.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 에디터 초이스입니다: " + shopChoice.getId()));
        ShopChoiceMapper.applyChanges(entity, shopChoice);
        return ShopChoiceMapper.toDomain(entity);
    }

    @Override
    public void deleteById(Long id) {
        shopChoiceJpaRepository.deleteById(id);
    }
}

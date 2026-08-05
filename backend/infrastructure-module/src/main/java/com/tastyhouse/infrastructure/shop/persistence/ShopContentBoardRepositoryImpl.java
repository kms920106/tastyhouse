package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopContentBoard;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.repository.ShopContentBoardRepository;

@Repository
public class ShopContentBoardRepositoryImpl implements ShopContentBoardRepository {

    private final ShopContentBoardJpaRepository shopContentBoardJpaRepository;

    public ShopContentBoardRepositoryImpl(ShopContentBoardJpaRepository shopContentBoardJpaRepository) {
        this.shopContentBoardJpaRepository = shopContentBoardJpaRepository;
    }

    @Override
    public ShopContentBoard save(ShopContentBoard shopContentBoard) {
        if (shopContentBoard.getId() == null) {
            ShopContentBoardJpaEntity saved = shopContentBoardJpaRepository.save(ShopContentBoardMapper.toEntity(shopContentBoard));
            return ShopContentBoardMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        ShopContentBoardJpaEntity entity = shopContentBoardJpaRepository.findById(shopContentBoard.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 콘텐츠보드입니다: " + shopContentBoard.getId()));
        ShopContentBoardMapper.applyChanges(entity, shopContentBoard);
        return ShopContentBoardMapper.toDomain(entity);
    }

    @Override
    public Optional<ShopContentBoard> findById(Long id) {
        return shopContentBoardJpaRepository.findById(id).map(ShopContentBoardMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        shopContentBoardJpaRepository.deleteById(id);
    }

    @Override
    public long countByShopId(Long shopId) {
        return shopContentBoardJpaRepository.countByShopId(ShopId.of(shopId));
    }

}

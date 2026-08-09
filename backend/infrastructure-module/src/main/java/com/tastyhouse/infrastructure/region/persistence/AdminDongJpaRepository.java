package com.tastyhouse.infrastructure.region.persistence;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminDongJpaRepository extends JpaRepository<AdminDongJpaEntity, Long> {

    Optional<AdminDongJpaEntity> findBySidoNameAndSigunguNameAndDongNameAndActiveIsTrue(
        String sidoName,
        String sigunguName,
        String dongName
    );

    boolean existsByIdAndActiveIsTrue(Long id);

    List<AdminDongJpaEntity> findByIdInAndActiveIsTrue(Collection<Long> ids);

    /**
     * 대표점이 바운딩 박스 안에 드는 사용 중 행정동. 배달지역 환산의 후보 프리필터이며
     * {@code idx_admin_dong_center}를 탄다.
     *
     * <p>대표점이 없는 행은 좌표 비교가 {@code NULL}이 되어 자동으로 빠진다 — 판정 근거가 없는 동을
     * 후보에 넣어도 어차피 "판정 불가"로 분류될 뿐이다.
     */
    @Query("""
        SELECT e FROM AdminDongJpaEntity e
        WHERE e.active = true
          AND e.centerLatitude BETWEEN :minLatitude AND :maxLatitude
          AND e.centerLongitude BETWEEN :minLongitude AND :maxLongitude
        """)
    List<AdminDongJpaEntity> findAllWithinBoundingBox(
        @Param("minLatitude") BigDecimal minLatitude,
        @Param("maxLatitude") BigDecimal maxLatitude,
        @Param("minLongitude") BigDecimal minLongitude,
        @Param("maxLongitude") BigDecimal maxLongitude
    );

    /**
     * 넘긴 식별자 중 실재하는(사용 중) 것만 골라 반환한다. 일괄 등록의 존재 검증이 건별 조회를 돌지
     * 않도록 식별자만 투영한다.
     */
    @Query("SELECT e.id FROM AdminDongJpaEntity e WHERE e.active = true AND e.id IN :ids")
    List<Long> findExistingIds(@Param("ids") Collection<Long> ids);
}

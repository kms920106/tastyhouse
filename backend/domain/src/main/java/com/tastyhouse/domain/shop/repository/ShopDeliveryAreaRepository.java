package com.tastyhouse.domain.shop.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.DeliveryAreaSource;
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 가게 배달가능지역 write 포트.
 *
 * <p>여기 남는 조회는 전부 불변식 검증(중복 등록 방지·삭제 대상 로드)이나 주문 접수 시의 배달가능 여부
 * 판정에 쓰이므로 write 포트 잔류 기준을 만족한다. 점주 설정 화면의 표현용 목록(행정동 이름 조인)은
 * infrastructure-module의 {@code shop/query/ShopDeliveryAreaQueryDao}가 별도로 담당한다.
 */
public interface ShopDeliveryAreaRepository {

    List<ShopDeliveryArea> findByShopId(ShopId shopId);

    Optional<ShopDeliveryArea> findById(Long deliveryAreaId);

    boolean existsByShopIdAndAdminDongId(ShopId shopId, AdminDongId adminDongId);

    /**
     * 가게의 배달가능지역 등록 건수. 0건이면 "배달가능지역 미설정"이라 주문 접수 시 지역 검사를 생략한다.
     */
    long countByShopId(ShopId shopId);

    ShopDeliveryArea save(ShopDeliveryArea shopDeliveryArea);

    /**
     * 여러 배달가능지역을 한 번에 저장한다. 반경 일괄 적용·도형 환산이 수십~수백 건을 동시에 넣으므로
     * 단건 {@code save} 루프 대신 배치 저장 경로를 둔다.
     */
    List<ShopDeliveryArea> saveAll(List<ShopDeliveryArea> shopDeliveryAreas);

    /** 가게의 배달가능지역 중 특정 출처의 행만 읽는다. 폴리곤 재저장이 교체 대상을 파악할 때 쓴다. */
    List<ShopDeliveryArea> findByShopIdAndSource(ShopId shopId, DeliveryAreaSource source);

    /**
     * 가게의 특정 출처 행을 전량 삭제한다. 폴리곤 저장은 {@code POLYGON} 행만 지우고 다시 넣으므로
     * 점주가 직접 등록한 {@code MANUAL} 행은 보존된다.
     */
    void deleteByShopIdAndSource(ShopId shopId, DeliveryAreaSource source);

    /**
     * 가게에 등록된 행정동 식별자 집합. 중복 판정(이미 등록된 동은 건너뛴다)에 쓴다 — 대상이 수백 건이라
     * 건별 {@code existsByShopIdAndAdminDongId}를 루프로 도는 대신 집합을 한 번 읽어 메모리에서 비교한다.
     */
    Set<AdminDongId> findAdminDongIdsByShopId(ShopId shopId);

    void deleteById(Long deliveryAreaId);
}

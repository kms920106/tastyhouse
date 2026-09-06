package com.tastyhouse.domain.shop.repository;

import java.util.Set;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 지역별 배달팁 참조 여부만 확인하는 전용 조회 포트.
 *
 * <p><b>왜 별도 인터페이스인가</b>: {@code ShopDeliveryAreaService}는 배달가능지역을 삭제하기 전에
 * "그 행정동을 참조하는 지역별 배달팁 행이 있는지"를 확인해야 한다(있으면
 * {@code SHOP_DELIVERY_AREA_IN_USE}로 차단 — 지역별 팁이 배달불가 지역을 가리키는 상태 방지).
 * 그런데 배달팁 5종 write 포트인 {@code ShopDeliveryTipRepository}를 통째로 주입받으면, 배달가능지역
 * 관리라는 좁은 관심사가 배달팁 설정 전체 계약에 의존하게 된다. 필요한 것은 boolean 하나이므로
 * 그 한 메서드만 노출하는 최소 포트를 둔다(ISP).
 *
 * <p><b>구현 소유</b>: 별도 어댑터를 만들지 않고 infrastructure-module의
 * {@code ShopDeliveryTipRepositoryImpl}이 {@code ShopDeliveryTipRepository}와 <b>함께</b> 이 인터페이스도
 * 구현한다 — 두 포트가 같은 테이블({@code SHOP_DELIVERY_TIP_REGION})을 읽으므로 어댑터를 쪼개면
 * 같은 쿼리가 두 곳에 생긴다.
 */
public interface ShopDeliveryTipRegionLookup {

    /** 해당 가게에 그 행정동을 대상으로 하는 지역별 배달팁 행이 있는지. */
    boolean existsRegionTipByShopIdAndAdminDongId(ShopId shopId, AdminDongId adminDongId);

    /**
     * 해당 가게의 지역별 배달팁이 참조하는 행정동 집합을 <b>한 번에</b> 읽는다.
     *
     * <p>일괄 삭제·폴리곤 재저장은 수십~수백 개의 행정동을 동시에 닫으므로, 위 단건 {@code exists}를
     * 루프로 돌면 쿼리가 대상 수만큼 나간다. 더 중요한 것은 <b>판정 시점</b>이다 — 이 경로는 "하나라도
     * 참조돼 있으면 한 건도 지우지 않는다"는 원자적 차단을 해야 하는데, 그러려면 지우기 전에 참조 집합
     * 전체를 알아야 한다. 건별로 확인하며 지우면 중간까지 지운 뒤 막히는 부분 삭제가 된다.
     */
    Set<AdminDongId> findRegionTipAdminDongIds(ShopId shopId);
}

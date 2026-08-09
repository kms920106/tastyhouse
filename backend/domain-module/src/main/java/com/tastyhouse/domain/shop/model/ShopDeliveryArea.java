package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 가게 배달가능지역(행정동 단위) 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopDeliveryAreaJpaEntity} + {@code ShopDeliveryAreaMapper}가 담당한다.
 *
 * <p>상태전이가 없어 등록(insert)과 삭제(delete)만 존재하므로 전 필드를 {@code final}로 두고 변경
 * 메서드를 두지 않는다({@code MemberFollow} 선례). 가게·행정동 중복 방지는 집합 차원의 불변식이라
 * {@code ShopDeliveryAreaService}가 담당한다.
 */
public class ShopDeliveryArea {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ShopId shopId;
    private final AdminDongId adminDongId;
    private final DeliveryAreaSource source;

    private ShopDeliveryArea(Long id, ShopId shopId, AdminDongId adminDongId, DeliveryAreaSource source) {
        this.id = id;
        this.shopId = shopId;
        this.adminDongId = adminDongId;
        this.source = source;
    }

    /**
     * 신규 배달가능지역을 생성한다. 아직 영속되지 않았으므로 식별자는 없다.
     *
     * <p>출처를 받지 않는 이 오버로드는 {@link DeliveryAreaSource#MANUAL}로 등록한다 — 행정동 직접 선택이
     * 원래의 유일한 등록 경로였고, 도형 파생 경로는 출처를 <b>명시</b>해야 하기 때문이다. 기본값을 MANUAL로
     * 두면 기존 호출부가 그대로 동작하고, 새 경로가 출처 지정을 빠뜨리면 파생 행이 MANUAL로 섞여 폴리곤
     * 재저장 때 지워지지 않는 형태로 드러난다(조용히 반대로 동작하지 않는다).
     */
    public static ShopDeliveryArea of(ShopId shopId, AdminDongId adminDongId) {
        return of(shopId, adminDongId, DeliveryAreaSource.MANUAL);
    }

    /**
     * 출처를 지정해 신규 배달가능지역을 생성한다. 도형 환산 파생 행({@link DeliveryAreaSource#POLYGON})이
     * 쓴다.
     */
    public static ShopDeliveryArea of(ShopId shopId, AdminDongId adminDongId, DeliveryAreaSource source) {
        if (source == null) {
            throw new IllegalArgumentException("배달가능지역의 등록 출처는 필수입니다.");
        }
        return new ShopDeliveryArea(null, shopId, adminDongId, source);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopDeliveryArea reconstitute(Long id, ShopId shopId, AdminDongId adminDongId, DeliveryAreaSource source) {
        return new ShopDeliveryArea(id, shopId, adminDongId, source);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public AdminDongId getAdminDongId() {
        return this.adminDongId;
    }

    /** 등록 출처. 폴리곤 재저장 시 {@code POLYGON} 행만 교체 대상이 된다. */
    public DeliveryAreaSource getSource() {
        return this.source;
    }
}

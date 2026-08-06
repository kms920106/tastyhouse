package com.tastyhouse.domain.order.vo;

import java.math.BigDecimal;

import com.tastyhouse.domain.member.model.MemberDeliveryAddress;

/**
 * 주문 시점의 배달 목적지 스냅샷 값 객체.
 *
 * <p><b>반드시 {@code record}여야 한다.</b> 이 VO는 {@code OrderJpaEntity}의 {@code @Embedded} 대상인데,
 * Hibernate 6의 {@code EmbeddableInstantiatorPojoStandard}는 no-arg 생성자 / {@code @Instantiator} 지정
 * 생성자 / record canonical 생성자 중 하나를 요구한다. 검증 로직이 든 생성자만 가진 일반 class로 두면
 * 셋 다 해당이 없어 런타임에 {@code Unable to locate constructor for embeddable}로 터진다
 * ({@code PhoneNumber} 장애 선례). {@code @Instantiator}는 {@code org.hibernate.annotations} import가
 * 필요해 domain-module 프레임워크-프리 원칙과 상충하므로 record가 유일한 선택지다.
 *
 * <p>같은 이유로 {@code toString()}을 오버라이드하지 않는다 — record 기본 {@code toString()}을 쓴다.
 *
 * <p><b>주소를 FK가 아니라 복사하는 이유</b>: {@code docs/domain/order.md}가 명시한 이 도메인의 원칙
 * (주문 당시 값 고정)과 일치시키기 위해서다. 회원이 주소록을 수정·삭제해도 과거 주문의 배달팁 산출
 * 근거가 사라지면 안 된다.
 *
 * <p>배달이 아닌 주문(포장·매장 등)은 이 값이 전부 {@code null}인 상태로 남는다 —
 * {@code @AttributeOverrides}로 매핑된 7개 컬럼이 모두 nullable인 이유다.
 *
 * <p><b>{@code adminDongId}가 VO가 아니라 raw {@code Long}인 이유</b>: 이 record는 그대로
 * {@code @Embedded} 컬럼에 매핑되는데, VO 타입 필드를 매핑하려면 {@code AttributeConverter}가 필요하고
 * 이 프로젝트는 FK를 VO로 변환하는 {@code @Convert}를 정책적으로 폐지했다(정책 B — QueryDSL이 VO path를
 * 만들어 query DAO의 raw {@code Long} 조인·투영이 깨지는 문제). 도메인 경계에서 {@code AdminDongId}가
 * 필요한 지점은 이 스냅샷이 아니라 원본 {@code MemberDeliveryAddress}에서 승격해서 쓴다
 * (배달팁 산출이 그러하다 — {@code OrderPlacementService}가 주소의 VO를 그대로 계산기에 넘긴다).
 *
 * <p><b>컴포넌트 선언 순서는 반드시 이름 알파벳 오름차순이어야 한다.</b> Hibernate 6의
 * {@code Component#sortProperties}는 embeddable 프로퍼티를 이름순으로 정렬하는데,
 * {@code isSimpleRecord()}(= 정렬 결과가 record 컴포넌트 순서와 일치)일 때만 정렬을 건너뛴다.
 * 선언 순서가 알파벳순이 아니면 {@code ComponentType#deepCopy}가 <b>정렬된 순서</b>로 읽은 값을
 * record canonical 생성자에 <b>선언 순서</b>대로 위치 기반 전달해, 값이 엉뚱한 파라미터로 들어간다.
 * 타입이 다르면 {@code Could not instantiate entity ... argument type mismatch}로 터지고
 * (선례: 주문 생성 시 {@code lotAddress}(String)가 {@code distanceMeters}(Integer) 자리에 들어가
 * {@code Cannot cast java.lang.String to java.lang.Integer} 발생), 타입이 같으면 컴파일·실행 모두
 * 통과하면서 <b>값만 조용히 뒤바뀐다</b>(예: 도로명↔지번 주소). 컴포넌트를 추가할 때도 알파벳 위치에
 * 삽입한다 — {@code @AttributeOverride}의 {@code name}은 컴포넌트명으로 매칭되므로 순서와 무관하다.
 *
 * @param adminDongId    주문 시점 행정동 ID(지역별 배달팁 산출 근거). 매칭 실패 시 null
 * @param detailAddress  주문 시점 상세 주소
 * @param distanceMeters 주문 시점 가게~배달지 직선거리(m). 거리별 배달팁 산출 근거
 * @param latitude       주문 시점 배달지 위도
 * @param longitude      주문 시점 배달지 경도
 * @param lotAddress     주문 시점 배달 지번 주소
 * @param roadAddress    주문 시점 배달 도로명 주소
 */
public record OrderDeliveryDestination(
    Long adminDongId,
    String detailAddress,
    Integer distanceMeters,
    BigDecimal latitude,
    BigDecimal longitude,
    String lotAddress,
    String roadAddress
) {

    /**
     * 회원 배달 주소와 산출된 거리로 스냅샷을 만든다.
     *
     * <p>거리를 주소가 아니라 인자로 받는 이유는 좌표→거리 변환({@code GeoDistance})이 가게 좌표를
     * 함께 필요로 하는 크로스 애그리거트 연산이라, 이 VO가 알 수 없기 때문이다.
     */
    public static OrderDeliveryDestination of(MemberDeliveryAddress address, int distanceMeters) {
        return new OrderDeliveryDestination(
            address.getAdminDongId() == null ? null : address.getAdminDongId().value(),
            address.getDetailAddress(),
            distanceMeters,
            address.getLatitude(),
            address.getLongitude(),
            address.getLotAddress(),
            address.getRoadAddress()
        );
    }

    /**
     * 배달이 아닌 주문의 빈 목적지. 7개 컬럼이 전부 null로 저장된다.
     *
     * <p>{@code null} 자체를 {@code Order}에 넣지 않고 빈 VO를 쓰면 매퍼·엔티티에서 null 분기가 사라진다.
     */
    public static OrderDeliveryDestination none() {
        return new OrderDeliveryDestination(null, null, null, null, null, null, null);
    }

    /** 배달 목적지가 실제로 확정된 스냅샷인지(도로명 주소 존재로 판정). */
    public boolean isPresent() {
        return this.roadAddress != null;
    }
}

package com.tastyhouse.domain.order.vo;

import java.time.LocalDateTime;

/**
 * 주문 시점에 확정된 수령 예약시간 스냅샷 값 객체.
 *
 * <p><b>반드시 {@code record}여야 한다.</b> {@code OrderJpaEntity}의 {@code @Embedded} 대상인데,
 * Hibernate 6의 {@code EmbeddableInstantiatorPojoStandard}는 no-arg 생성자 / {@code @Instantiator} 지정
 * 생성자 / record canonical 생성자 중 하나를 요구한다({@link OrderDeliveryDestination} Javadoc의 상세 근거
 * 참고). 같은 이유로 {@code toString()}을 오버라이드하지 않는다.
 *
 * <p><b>컴포넌트 선언 순서는 이름 알파벳 오름차순이어야 한다</b> — {@code scheduledAt} &lt;
 * {@code scheduledSlotEndAt}. 어긋나면 Hibernate {@code Component#sortProperties}가 이름순으로 정렬한 값을
 * 선언 순서대로 위치 기반 전달해 값이 뒤바뀌는데, <b>두 컴포넌트가 같은 {@code LocalDateTime} 타입이라
 * 예외 없이 조용히 뒤바뀐다</b>. 가드 테스트 {@code EmbeddedRecordComponentOrderTest}가 클래스패스 스캔으로
 * 이 순서를 자동 검증한다.
 *
 * <p><b>확정된 값을 스냅샷으로 복사하는 이유</b>: PDF 규격 "금액·시간은 결제 시점 기준 확정"과 일치시키기
 * 위해서다. 이후 점주가 예약주문을 끄거나 영업시간을 바꿔도 이미 접수된 주문의 수령 시각은 변하지 않는다.
 *
 * <p>즉시 주문은 두 값이 모두 {@code null}인 상태로 남는다 — {@code @AttributeOverrides}로 매핑된 2개
 * 컬럼이 모두 nullable인 이유다.
 *
 * @param scheduledAt        수령 예약 시각(슬롯 시작). {@code null}이면 즉시 주문
 * @param scheduledSlotEndAt 수령 예약 슬롯 종료 시각. 포장은 {@code scheduledAt}과 동일
 */
public record OrderSchedule(
    LocalDateTime scheduledAt,
    LocalDateTime scheduledSlotEndAt
) {

    /**
     * 서버가 재계산해 확정한 슬롯의 시작·종료 시각으로 스냅샷을 만든다.
     *
     * <p><b>shop의 {@code ScheduledOrderSlot}을 그대로 받지 않고 두 시각을 낱개로 받는다</b> —
     * 슬롯 타입을 받으면 order의 VO가 shop 모델을 알게 되어 컨텍스트 경계를 위반한다. 슬롯에서 두
     * 시각을 꺼내 넘기는 것은 이 VO를 만드는 {@code OrderPlacementService}의 몫이다.
     *
     * <p>두 파라미터가 같은 {@code LocalDateTime} 타입이라 <b>순서를 바꿔도 컴파일되고 값만 조용히
     * 뒤바뀐다</b> — 호출부에서 시작·종료 자리를 반드시 대조한다.
     */
    public static OrderSchedule of(LocalDateTime scheduledAt, LocalDateTime scheduledSlotEndAt) {
        return new OrderSchedule(scheduledAt, scheduledSlotEndAt);
    }

    /**
     * 즉시 주문의 빈 예약시간. 2개 컬럼이 전부 null로 저장된다.
     *
     * <p>{@code null} 자체를 {@code Order}에 넣지 않고 빈 VO를 쓰면 매퍼·엔티티에서 null 분기가 사라진다
     * ({@link OrderDeliveryDestination#none()}과 동일한 이유).
     */
    public static OrderSchedule none() {
        return new OrderSchedule(null, null);
    }

    /** 수령 시각이 예약된 주문인지. */
    public boolean isPresent() {
        return this.scheduledAt != null;
    }
}

package com.tastyhouse.infrastructure.shared.persistence;

import java.util.function.Function;

/**
 * JPA 엔티티 FK 필드(raw {@code Long})와 도메인 ID VO 사이의 null-안전 변환 헬퍼.
 *
 * <p>엔티티 FK는 raw {@code Long}으로 저장하고, 도메인 모델의 {@code XxxId} VO는 이 헬퍼를 거쳐 매퍼에서만 승격·언패킹한다.
 *
 * <p><b>왜 필요한가</b>: 모든 {@code XxxId} VO는 compact constructor에서 null을 거부한다({@code
 * IllegalArgumentException}). 반면 일부 FK 컬럼은 nullable이다(예: {@code Shop.ceo_id} — 점주 미배정,
 * {@code BugReport.assignee_admin_id} — 미배정). 매퍼에서 {@code CeoId.of(entity.getCeoId())}처럼 직접
 * 호출하면 컴파일은 통과하고, 해당 FK가 실제로 null인 행을 읽을 때만 예외가 난다 — 빈 테이블이나 FK가 항상
 * 채워진 샘플 데이터로는 잡히지 않는 결함 유형이다. 이 헬퍼는 그 판단을 호출부에서 제거해, nullable 여부를
 * 몰라도 항상 안전한 형태를 기본값으로 만든다 — NOT NULL 컬럼에서는 null 분기가 죽은 코드가 될 뿐이지만,
 * nullable 컬럼에서는 유일하게 안전한 경로다.
 *
 * <p>{@code raw(...)}는 쓰기 방향에서도 대칭적으로 필요하다 — NOT NULL 컬럼이라도 도메인 모델이 아직 배정되지
 * 않은 상태(예: 점주 미배정 {@code Shop})를 null VO로 들고 있을 수 있으므로, {@code domain.getCeoId().value()}는
 * VO가 null이면 NPE로 실패한다.
 *
 * <p>모든 매퍼가 nullable 여부와 무관하게 이 헬퍼를 통일해서 쓴다 — "NOT NULL이면 직접 호출, nullable이면
 * 헬퍼"처럼 컬럼별로 형태를 나누면 작성자·리뷰어가 매번 엔티티의 nullable 여부를 확인해야 하고, 위험한 직접
 * 호출 형태가 흔해 보여 눈에 띄지 않게 된다.
 *
 * <p>{@code Xxx.of(...)} 위임 규칙(DTO 조립 규칙)의 예외가 아니다 — {@code vo(raw, CeoId::of)}의 메서드
 * 레퍼런스가 {@code of()} 팩토리를 경유하므로 {@code new}는 여전히 팩토리 내부에만 남는다.
 */
public final class IdMapping {

    private IdMapping() {
    }

    /**
     * raw FK 값을 ID VO로 승격한다. {@code raw}가 null이면 {@code factory}를 호출하지 않고 null을 반환한다
     * (VO의 compact constructor가 null을 거부하므로, 호출했다면 예외가 났을 것이다).
     */
    public static <T> T vo(Long raw, Function<Long, T> factory) {
        return raw == null ? null : factory.apply(raw);
    }

    /**
     * ID VO를 raw FK 값으로 언패킹한다. {@code vo}가 null이면 {@code extractor}를 호출하지 않고 null을
     * 반환한다(VO가 없는 상태 — 예: 미배정 — 를 그대로 null 컬럼으로 저장한다).
     */
    public static <T> Long raw(T vo, Function<T, Long> extractor) {
        return vo == null ? null : extractor.apply(vo);
    }
}

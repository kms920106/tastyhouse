package com.tastyhouse.application.product.port.out;

/**
 * 옵션그룹 합치기 추천의 후보 그룹 read model(그룹 1건 = 1행).
 *
 * <p>{@code signaturePayload}는 <b>해싱 전의 원시 문자열</b>이다 — SHA-256 계산은 반드시 Java의
 * {@code ProductOptionGroupSignature}가 수행한다. SQL {@code SHA2}와 Java 해시를 두 벌로 유지하면
 * 미세한 인코딩 차이만으로 제외 기능이 조용히 깨지는데, 그것이 이 기능의 가장 큰 correctness
 * 리스크이기 때문이다.
 *
 * <p>같은 {@code signaturePayload}를 가진 행들이 곧 하나의 "중복 묶음"이며, 화면의 묶음 카드
 * 1장에 대응한다.
 */
public record ProductOptionGroupMergeCandidateResult(
    Long optionGroupId,
    String name,
    Integer minSelect,
    Integer maxSelect,
    String signaturePayload
) {
}

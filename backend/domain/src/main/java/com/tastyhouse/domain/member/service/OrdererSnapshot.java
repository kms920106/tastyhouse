package com.tastyhouse.domain.member.service;

/**
 * 주문자 정보 스냅샷 — 주문 헤더에 <b>주문 당시 값으로 박제</b>되는 이름·연락처·계정명이다.
 *
 * <p>세 필드가 모두 {@code String}이라 순서를 바꿔도 컴파일되고 값만 조용히 뒤바뀐다 —
 * 조립·소비 시 자리를 반드시 대조한다.
 */
public record OrdererSnapshot(
    String fullName,
    String phoneNumber,
    String username
) {
}

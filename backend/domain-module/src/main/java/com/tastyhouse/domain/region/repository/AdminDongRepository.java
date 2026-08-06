package com.tastyhouse.domain.region.repository;

import java.util.Optional;

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.vo.AdminDongId;

/**
 * 행정동 마스터 조회 포트.
 *
 * <p>read-only 마스터라 저장·삭제가 없다(시드 SQL이 소유). 여기 남는 조회는 전부 도메인 서비스가
 * 불변식 검증에 쓰는 것이므로 write 포트 잔류 기준을 만족한다 — 표현용 목록 조회는 infrastructure-module의
 * query DAO가 별도로 담당한다.
 */
public interface AdminDongRepository {

    Optional<AdminDong> findById(AdminDongId adminDongId);

    /** 배달가능지역 등록 시 행정동 존재 검증에 쓴다. */
    boolean existsById(AdminDongId adminDongId);

    /**
     * 주소 문자열(시/도 · 시/군/구 · 행정동명)로 행정동을 매칭한다. 회원 배달 주소의 행정동 채우기처럼
     * 좌표가 아닌 주소 문자열에서 행정동을 역추적하는 경로가 쓴다. 매칭 실패는 빈 Optional이다.
     */
    Optional<AdminDong> findByDongNameMatch(String sidoName, String sigunguName, String dongName);
}

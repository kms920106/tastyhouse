package com.tastyhouse.domain.region.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shared.geo.GeoBoundingBox;

/**
 * 행정동 마스터 write 포트.
 *
 * <p>여기 남는 조회는 전부 도메인 서비스가 불변식 검증에 쓰는 것이므로 write 포트 잔류 기준을 만족한다 —
 * 표현용 목록 조회는 infrastructure-module의 query DAO가 별도로 담당한다.
 *
 * <p>쓰기는 {@link #synchronize} 하나뿐이다. 마스터는 건별로 갱신되지 않고 원천 스냅샷 단위로 통째
 * 교체되므로, 개별 save를 열면 "일부만 새 원천, 일부는 옛 원천"인 혼합 상태가 만들어질 수 있다.
 */
public interface AdminDongRepository {

    /**
     * 행정동 마스터를 넘긴 목록으로 동기화한다. 동기화 배치 전용이다.
     *
     * <p><b>기존 행의 {@code id}를 보존한다.</b> 코드가 같은 행은 지우지 않고 제자리에서 값만 갱신하고,
     * 원천에 없어진 코드는 삭제 대신 {@code is_active = 0}으로 내린다. 다른 테이블이
     * {@code ADMIN_DONG.id}를 참조하고 있어서다(배달가능지역·지역별 배달팁·주문 시점 스냅샷) — 지우고
     * 새로 넣으면 그 참조가 <b>말없이 다른 동을 가리키거나 끊어진다.</b> 폐지 동을 {@code is_active = 0}
     * 으로 남기는 것은 이 테이블이 원래 갖고 있던 규약이기도 하다.
     *
     * @return 동기화 결과 요약
     */
    AdminDongSyncResult synchronize(List<AdminDong> adminDongs);

    Optional<AdminDong> findById(AdminDongId adminDongId);

    /** 배달가능지역 등록 시 행정동 존재 검증에 쓴다. */
    boolean existsById(AdminDongId adminDongId);

    /**
     * 주소 문자열(시/도 · 시/군/구 · 행정동명)로 행정동을 매칭한다. 회원 배달 주소의 행정동 채우기처럼
     * 좌표가 아닌 주소 문자열에서 행정동을 역추적하는 경로가 쓴다. 매칭 실패는 빈 Optional이다.
     */
    Optional<AdminDong> findByDongNameMatch(String sidoName, String sigunguName, String dongName);

    /**
     * 대표점이 바운딩 박스 안에 드는 사용 중 행정동을 모두 읽는다. 배달지역 도형·반경 환산의
     * <b>후보 프리필터</b>다 — 전국 3,600여 개를 전부 정밀 판정하지 않고 인덱스
     * ({@code idx_admin_dong_center})로 수십~수백 건까지 좁힌 뒤 넘긴다.
     *
     * <p>대표점이 없는 행은 이 조회로 걸리지 않는다. 좌표가 없으면 박스 판정 자체가 불가능하며, 그런 동은
     * 어차피 환산에서 "판정 불가"로 분류된다.
     */
    List<AdminDong> findAllWithinBoundingBox(GeoBoundingBox boundingBox);

    /** 식별자 목록으로 행정동을 일괄 조회한다. 없는 식별자는 결과에서 빠진다. */
    List<AdminDong> findAllByIds(Collection<AdminDongId> adminDongIds);

    /**
     * 넘긴 식별자 중 <b>실재하는</b> 것만 골라 반환한다. 일괄 등록에서 "없는 행정동이 섞였는지"를
     * 한 번의 조회로 판정하기 위한 것이다 — 건별 {@code existsById}를 루프로 돌면 500건 요청에 쿼리가
     * 500번 나간다.
     */
    Set<AdminDongId> filterExistingIds(Collection<AdminDongId> adminDongIds);
}

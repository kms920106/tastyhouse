package com.tastyhouse.domain.shop.service;

import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoPolygon;
import com.tastyhouse.domain.shared.geo.GeoRing;

/**
 * 배달지역 도형을 행정동 집합으로 환산한다(무상태).
 *
 * <p><b>왜 환산이 필요한가</b>: 주문 배달가능 판정은 배송지의 행정동이 가게의 배달가능지역 집합에 있는지만
 * 본다. 도형은 편집·표현의 원본일 뿐이므로, 저장 시점에 행정동 집합으로 바꿔 두지 않으면 판정에 쓰이지
 * 못한다.
 *
 * <p><b>판정 규칙(순서대로)</b>
 * <ol>
 *   <li>대표점이 도형 안에 들면 포함한다. 대표점은 경계 내부가 보장되는 점이라 이 판정이 가장 신뢰도가
 *       높다.
 *   <li>대표점으로 걸러지지 않았고 경계를 보유했다면, 경계 정점을 최대 200개 균등 샘플링해 도형 안에 든
 *       비율이 30% 이상이면 포함한다. 큰 동의 <b>일부만</b> 도형에 걸친 경우를 잡는 규칙이다.
 *   <li>대표점도 경계도 없으면 <b>판정 불가</b>로 분류한다. 조용히 포함시키지도, 제외하지도 않고 개수를
 *       노출해 점주가 데이터 공백을 인지하게 한다.
 * </ol>
 *
 * <p><b>면적 교차비율을 쓰지 않는 이유</b>: 정확한 교차 면적을 구하려면 폴리곤 클리핑이 필요하고, 그것은
 * JTS급 라이브러리를 뜻한다. domain-module은 production 의존이 0개로 강제되므로 여기에 그런 의존을 들일
 * 수 없다. 정점 샘플 비율은 결정적이고 순수 자바로 구현되며, 판정 단위가 <b>행정동 하나 전체</b>라 30%
 * 임계로 실무상 충분하다 — 어차피 동의 일부만 걸려도 그 동은 통째로 열린다.
 *
 * <p>후보 목록을 좁히는 일(바운딩 박스 프리필터)은 이 클래스의 책임이 아니다. 호출자가 리포지토리로
 * 후보를 읽어 넘긴다 — 그래야 이 클래스가 순수 계산으로 남아 DB 없이 테스트된다.
 */
public final class DeliveryAreaProjection {

    private DeliveryAreaProjection() {
    }

    /**
     * 후보 행정동 중 도형에 포함되는 것을 골라낸다.
     *
     * @param polygon    배달지역 도형
     * @param candidates 바운딩 박스로 좁힌 후보 행정동
     * @return 포함된 행정동 식별자와 판정 불가 개수
     */
    public static Result project(GeoPolygon polygon, List<AdminDong> candidates) {
        if (polygon == null) {
            throw new IllegalArgumentException("환산할 도형은 필수입니다.");
        }
        if (candidates == null) {
            throw new IllegalArgumentException("후보 행정동 목록은 필수입니다(비어 있을 수는 있습니다).");
        }

        List<AdminDongId> included = new ArrayList<>();
        int unresolvedCount = 0;

        for (AdminDong candidate : candidates) {
            if (candidate.hasCenter() && polygon.contains(candidate.getCenter())) {
                included.add(AdminDongId.of(candidate.getId()));
                continue;
            }
            if (candidate.hasBoundary()) {
                if (isCoveredEnough(polygon, candidate.getBoundary())) {
                    included.add(AdminDongId.of(candidate.getId()));
                }
                continue;
            }
            if (!candidate.hasCenter()) {
                // 대표점도 경계도 없어 어떤 규칙으로도 판정할 수 없다.
                unresolvedCount++;
            }
        }

        return new Result(List.copyOf(included), unresolvedCount);
    }

    /**
     * 경계 정점 샘플 중 도형 안에 든 비율이 임계 이상인지.
     *
     * <p>여러 링(도서 지역 등)을 가진 동은 링별로 따로 보지 않고 <b>전체 샘플을 합산</b>한다 — 작은 부속
     * 섬 하나가 통째로 도형에 들었다고 동 전체를 여는 것은 과대 포함이기 때문이다.
     */
    private static boolean isCoveredEnough(GeoPolygon polygon, List<GeoRing> boundary) {
        int sampled = 0;
        int contained = 0;

        for (GeoPoint point : sampleBoundary(boundary)) {
            sampled++;
            if (polygon.contains(point)) {
                contained++;
            }
        }

        return sampled > 0 && (double) contained / sampled >= ShopDeliveryAreaPolicy.COVERAGE_THRESHOLD;
    }

    /**
     * 경계 전체에서 최대 {@code BOUNDARY_SAMPLE_LIMIT}개의 정점을 뽑는다. 링이 여럿이면 링의 정점 수에
     * 비례해 배분하되, 각 링이 최소 1개는 갖도록 한다 — 작은 링이 샘플 0개가 되면 그 링은 판정에서
     * 통째로 사라진다.
     */
    private static List<GeoPoint> sampleBoundary(List<GeoRing> boundary) {
        int totalVertices = 0;
        for (GeoRing ring : boundary) {
            totalVertices += ring.vertexCount();
        }
        if (totalVertices <= ShopDeliveryAreaPolicy.BOUNDARY_SAMPLE_LIMIT) {
            List<GeoPoint> all = new ArrayList<>(totalVertices);
            for (GeoRing ring : boundary) {
                all.addAll(ring.points());
            }
            return all;
        }

        List<GeoPoint> sampled = new ArrayList<>(ShopDeliveryAreaPolicy.BOUNDARY_SAMPLE_LIMIT);
        for (GeoRing ring : boundary) {
            int quota = (int) ((long) ShopDeliveryAreaPolicy.BOUNDARY_SAMPLE_LIMIT * ring.vertexCount() / totalVertices);
            sampled.addAll(ring.sample(Math.max(1, quota)));
        }
        return sampled;
    }

    /**
     * 환산 결과.
     *
     * @param adminDongIds    도형에 포함된 행정동 식별자
     * @param unresolvedCount 좌표·경계 미보유로 판정하지 못한 후보 수
     */
    public record Result(
        List<AdminDongId> adminDongIds,
        int unresolvedCount
    ) {

        public Result {
            adminDongIds = List.copyOf(adminDongIds);
        }

        public boolean isEmpty() {
            return this.adminDongIds.isEmpty();
        }

        public int count() {
            return this.adminDongIds.size();
        }
    }
}

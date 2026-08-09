package com.tastyhouse.domain.shared.geo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 폴리곤을 이루는 닫힌 링(고리) 하나.
 *
 * <p>점 목록을 <b>불변으로 복사</b>해 보관한다 — 호출자가 넘긴 리스트를 나중에 수정해도 이미 검증을
 * 마친 도형이 바뀌지 않게 하기 위해서다.
 *
 * <p><b>첫 점과 끝 점의 중복(명시적 폐합)을 보관하지 않는다.</b> 링은 정의상 닫혀 있으므로 마지막 점에서
 * 첫 점으로 돌아가는 변은 암묵적이다. 저장 형식에 폐합점이 있든 없든 같은 도형으로 취급하려면 표현을
 * 한쪽으로 정규화해야 하고, 그렇지 않으면 {@code vertexCount}가 입력 형식에 따라 달라진다.
 */
public record GeoRing(
    List<GeoPoint> points
) {

    /** 면(面)을 이루는 데 필요한 최소 정점 수. */
    public static final int MIN_POINTS = 3;

    public GeoRing {
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("링에는 좌표가 필요합니다.");
        }
        points = List.copyOf(normalize(points));
        if (points.size() < MIN_POINTS) {
            throw new IllegalArgumentException("링은 서로 다른 좌표가 " + MIN_POINTS + "개 이상이어야 합니다.");
        }
    }

    public static GeoRing of(List<GeoPoint> points) {
        return new GeoRing(points);
    }

    /**
     * 연속 중복점과 폐합점을 제거한다.
     *
     * <p>지도에서 도형을 그릴 때 같은 지점을 두 번 클릭하면 길이 0인 변이 생기는데, ray-casting에서
     * 길이 0 변은 판정에 기여하지 못하면서 경계 판정 분기만 늘린다. 저장 전에 없앤다.
     */
    private static List<GeoPoint> normalize(List<GeoPoint> rawPoints) {
        List<GeoPoint> normalized = new ArrayList<>(rawPoints.size());
        for (GeoPoint point : rawPoints) {
            if (point == null) {
                throw new IllegalArgumentException("링에 빈 좌표가 포함될 수 없습니다.");
            }
            if (normalized.isEmpty() || !normalized.getLast().isSameLocation(point)) {
                normalized.add(point);
            }
        }

        // 명시적 폐합점(마지막 == 첫 점)은 암묵 폐합으로 통일한다.
        while (normalized.size() > 1 && normalized.getFirst().isSameLocation(normalized.getLast())) {
            normalized.removeLast();
        }

        return normalized;
    }

    /** 정점 수(암묵 폐합 기준 — 폐합점을 세지 않는다). */
    public int vertexCount() {
        return this.points.size();
    }

    /** 이 링을 감싸는 최소 바운딩 박스. */
    public GeoBoundingBox boundingBox() {
        return GeoBoundingBox.enclosing(this.points);
    }

    /** 기준점으로부터 가장 먼 정점까지의 거리(m). */
    public double maxDistanceMetersFrom(GeoPoint center) {
        double max = 0;
        for (GeoPoint point : this.points) {
            max = Math.max(max, center.distanceMetersTo(point));
        }
        return max;
    }

    /**
     * 링을 최대 {@code limit}개까지 균등 간격으로 샘플링한다. 정점이 {@code limit} 이하면 전부 반환한다.
     *
     * <p>행정동 경계는 단순화 후에도 수백 정점에 이를 수 있어, 폴리곤 포함 비율을 계산할 때 전 정점을
     * 쓰면 후보 동 수만큼 곱해져 비용이 커진다. 균등 간격 샘플은 특정 구간에 치우치지 않는다.
     */
    public List<GeoPoint> sample(int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("샘플 개수는 1 이상이어야 합니다.");
        }
        if (this.points.size() <= limit) {
            return this.points;
        }

        List<GeoPoint> sampled = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            sampled.add(this.points.get((int) ((long) i * this.points.size() / limit)));
        }
        return Collections.unmodifiableList(sampled);
    }
}

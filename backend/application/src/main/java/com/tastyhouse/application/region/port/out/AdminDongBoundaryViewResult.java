package com.tastyhouse.application.region.port.out;

import java.math.BigDecimal;
import java.util.List;

/**
 * 경계 문자열을 좌표 배열로 푼 뒤의 행정동 경계 한 건 — 표현 계층에 넘기는 조회 결과.
 *
 * <p><b>챕터 09</b>에서 신설. {@link AdminDongBoundaryResult}는 DAO가 읽어 온 <b>인코딩된</b>
 * {@code boundary} 문자열을 그대로 들고 있어 그 자체로는 응답을 만들 수 없다. 디코딩은
 * {@code GeoRingsPort}(아웃바운드 포트, 구현은 infrastructure)가 수행하므로 <b>application에
 * 남아야 하고</b>, 따라서 표현 계약이 {@code from(Result)} 한 번으로 끝낼 수 있도록 디코딩을 마친
 * 이 결과 타입을 따로 둔다.
 *
 * <p>좌표를 {@code domain.shared.geo}의 {@code GeoRing}·{@code GeoPoint}가 아니라 낱개
 * {@code BigDecimal} 쌍({@link Point})으로 내리는 이유는, api 모듈이 도메인 모델을 알지 않는다는
 * 경계 때문이다 — {@code controllersShouldBeDomainFree}의 carve-out은
 * {@code domain.shared.page..}와 도메인 enum뿐이고 {@code domain.shared.geo..}는 포함되지 않는다.
 * 리포 전체에서 api 모듈이 geo 타입을 참조하는 곳은 한 곳도 없으며, 그 경계를 이 챕터가 깨지 않는다.
 */
public record AdminDongBoundaryViewResult(
    long adminDongId,
    String regionName,
    BigDecimal centerLatitude,
    BigDecimal centerLongitude,
    List<List<Point>> rings
) {

    /**
     * 경계를 이루는 좌표 한 점.
     *
     * <p><b>컴포넌트 선언 순서는 알파벳순({@code latitude} → {@code longitude})이다</b> — 둘 다
     * {@code BigDecimal}이라 순서가 어긋나면 컴파일은 통과하고 값만 조용히 뒤바뀐다({@code GeoPoint}가
     * 같은 이유로 세운 규칙을 따른다).
     */
    public record Point(
        BigDecimal latitude,
        BigDecimal longitude
    ) {
    }
}

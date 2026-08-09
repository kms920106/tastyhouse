package com.tastyhouse.external.region;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * 행정동 경계 원천 설정.
 *
 * <p>{@code sourceUrl}에 버전 디렉터리(예: {@code ver20260701})가 박혀 있다 — 원천이 새 버전을 낼 때마다
 * 디렉터리가 하나 늘어나는 방식이라 "최신"을 가리키는 고정 URL이 없기 때문이다. 행정구역 개편은 연 몇 회
 * 수준이고 그때마다 사람이 확인하고 올려야 하는 값이므로, 자동 추종하지 않고 설정으로 고정한다.
 *
 * @param sourceUrl      행정동 경계 GeoJSON URL
 * @param timeoutSeconds 다운로드 타임아웃(초). 30MB대 파일이라 기본값이 넉넉하다
 * @param maxBytes       허용 최대 응답 크기(바이트). 원천이 예상 밖으로 커졌을 때 힙을 지키는 상한이다
 */
@ConfigurationProperties(prefix = "region.admin-dong.boundary")
public record AdminDongBoundaryProperties(
    @DefaultValue("https://raw.githubusercontent.com/vuski/admdongkor/master/ver20260701/HangJeongDong_ver20260701.geojson")
    String sourceUrl,

    @DefaultValue("180") int timeoutSeconds,

    @DefaultValue("134217728") int maxBytes
) {
}

package com.tastyhouse.application.crawling.bbq.port.out;

/**
 * 원격 URL의 이미지를 내려받아 파일 저장소에 올리고 파일 식별자를 돌려주는 아웃바운드 포트.
 *
 * <p>구현은 infrastructure:crawling의 {@code RemoteImageDownloader}다. 크롤링 경로에서만 쓰이므로
 * 소유 규칙에 따라 batch-application이 소유한다.
 *
 * <p>표면은 application이 실제로 호출하는 한 메서드뿐이다 — 구현체의 나머지(HTTP 클라이언트 구성·
 * content-type 파싱·파일명 추출)는 어댑터 내부 사정이다.
 */
public interface RemoteImagePort {

    Long uploadFromUrl(String imageUrl);
}

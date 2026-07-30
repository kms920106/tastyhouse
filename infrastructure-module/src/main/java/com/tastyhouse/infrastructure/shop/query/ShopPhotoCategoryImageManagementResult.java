package com.tastyhouse.infrastructure.shop.query;

/**
 * 관리 화면용 사진 카테고리 이미지 결과.
 *
 * <p>회원 노출용 {@link ShopPhotoCategoryImageResult}와 달리 <b>노출 여부({@code visible})를 포함</b>한다 —
 * 관리 화면은 미노출 이미지도 함께 보여주고 그 상태를 표시해야 하기 때문이다. 필드 셋이 다르므로 두
 * Result를 통합하지 않는다(회원 응답에 관리용 필드를 과잉 노출하지 않기 위함).
 *
 * <p>비-admin 형제({@code ShopPhotoCategoryImageResult})와 같은 패키지에 공존해 이름이 충돌하므로
 * CLAUDE.md의 admin 네이밍 규칙에 따라 {@code Management} 한정어를 붙였다.
 */
public record ShopPhotoCategoryImageManagementResult(
    Long id,
    Long shopPhotoCategoryId,
    String filePath,
    Integer sort,
    boolean visible
) {
}

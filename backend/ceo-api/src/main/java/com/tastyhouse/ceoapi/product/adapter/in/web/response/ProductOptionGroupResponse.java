package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.product.port.out.ProductOptionGroupViewResult;

/**
 * 옵션그룹(점주 관리 화면).
 *
 * <p>메뉴그룹 목록과 마찬가지로 <b>감춘 그룹·옵션도 포함</b>해 내려온다 — 이 화면이 감추기(소프트
 * 삭제)를 수행하므로 필터를 걸면 감춘 항목을 되살릴 방법이 없어진다.
 *
 * <p>일반 옵션그룹만 담는다 — 공통 옵션그룹은 점주 CRUD 대상이 아니며, 두 테이블의 id 공간이
 * 독립적이라 한 목록에 섞으면 후속 요청의 id가 어느 갈래인지 알 수 없어진다.
 */
@Schema(description = "옵션그룹")
public record ProductOptionGroupResponse(
    @Schema(description = "옵션그룹 ID", example = "3")
    Long id,

    @Schema(description = "옵션그룹명", example = "맵기 선택")
    String name,

    @Schema(description = "옵션그룹 설명. 미설정이면 null", example = "원하시는 맵기를 골라주세요.")
    String description,

    @Schema(description = "필수 선택 여부. true면 주문 시 반드시 하나 이상 골라야 한다.", example = "true")
    Boolean required,

    @Schema(description = "다중 선택 여부. false면 하나만 고를 수 있다.", example = "false")
    Boolean multipleSelect,

    @Schema(description = "최소 선택 개수. 미지정이면 null", example = "1")
    Integer minSelect,

    @Schema(description = "최대 선택 개수. 미지정(무제한)이면 null", example = "3")
    Integer maxSelect,

    @Schema(description = "노출 순서(0부터). 순서는 그룹이 아니라 연결(링크)이 가지므로 가게 단위 "
        + "목록에서는 연결된 메뉴 중 가장 앞선 순서값이다.", example = "0")
    Integer sort,

    @Schema(description = "노출 여부. 삭제(감추기)한 그룹은 false다 — 이 목록은 감춘 그룹도 포함하므로 "
        + "화면이 이 값으로 걸러내거나 '삭제됨' 배지를 붙여야 한다.", example = "true")
    Boolean visible,

    @Schema(description = "옵션그룹 유형. CUP_DEPOSIT은 일회용컵 보증금 그룹으로, 필수 선택 불가이고 "
        + "선택 개수가 minSelect=0·maxSelect=1로 고정된다.",
        example = "NORMAL", allowableValues = {"NORMAL", "CUP_DEPOSIT"})
    String groupType,

    @Schema(description = "이 그룹이 연결된 메뉴 수. 1이면 마지막 연결이라 해제가 거부된다.", example = "2")
    Long linkedProductCount,

    @Schema(description = "이 그룹에 속한 옵션 목록(순서 오름차순)")
    List<ProductOptionResponse> options
) {

    public static ProductOptionGroupResponse from(ProductOptionGroupViewResult result) {
        return new ProductOptionGroupResponse(
            result.id(),
            result.name(),
            result.description(),
            result.required(),
            result.multipleSelect(),
            result.minSelect(),
            result.maxSelect(),
            result.sort(),
            result.visible(),
            result.groupType(),
            result.linkedProductCount(),
            result.options().stream()
                .map(ProductOptionResponse::from)
                .toList()
        );
    }
}

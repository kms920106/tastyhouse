package com.tastyhouse.application.shop.port.out;

import java.util.List;

import com.tastyhouse.domain.shop.model.ShopImageType;

/**
 * 가게 관리 조회 포트(CQRS query 측 아웃바운드 포트) — 점주 관리 화면용.
 *
 * <p>점주가 자기 가게를 운영하며 보는 조회를 담당한다 — 콘텐츠보드, 자기가 올린 이미지 변경 요청의
 * 현황, 영업중지·임시휴업 이력, 메뉴판 이미지다. 회원 화면 조회는 {@link ShopQueryPort}, 관리자 검수
 * 화면 조회는 {@link ShopManagementQueryPort}, 여러 앱이 함께 보는 기본 정보는
 * {@link ShopBasicInfoQueryPort}가 소유한다.
 *
 * <p>관리자 계약이 {@code Management} 한정어를 이미 쓰고 있으므로(반환 타입도
 * {@code ShopManagementDetailResult}), 점주 관리 계약은 소유 주체를 담은 {@code Owner}로 구별한다.
 *
 * <p>관리자 쪽과 이름이 겹쳐 보이는 {@code findImageChangeRequests}·{@code findContentBoards}는
 * 시그니처가 다르다 — 점주는 <b>자기 가게 한 곳</b>({@code Long shopId})을 보고, 관리자는 승인 상태별로
 * <b>전체를 페이징</b>한다.
 */
public interface ShopOwnerQueryPort {

    List<ShopContentBoardResult> findContentBoards(Long shopId);

    List<ShopImageChangeRequestResult> findImageChangeRequests(Long shopId, ShopImageType imageType);

    List<ShopSuspensionResult> findSuspensions(Long shopId);

    List<ShopTemporaryClosureResult> findTemporaryClosures(Long shopId);

    List<String> findFoodTypeCategoryNames(Long shopId);

    List<ShopMenuCollectionImageResult> findMenuCollectionImages(Long shopId);
}

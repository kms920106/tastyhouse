package com.tastyhouse.ceoapi.shop;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.infrastructure.shop.query.ShopContentBoardResult;
import com.tastyhouse.infrastructure.shop.query.ShopQueryDao;
import com.tastyhouse.ceoapi.file.FileService;
import com.tastyhouse.ceoapi.shop.response.ShopContentBoardResponse;

/**
 * 점주용 가게 콘텐츠보드 조회 서비스(CQRS query 측).
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopContentBoardQueryService {

    private final ShopQueryDao shopQueryDao;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final FileService fileService;

    public List<ShopContentBoardResponse> getContentBoards(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        List<ShopContentBoardResult> contentBoards = shopQueryDao.findContentBoards(shopId);

        // 목록 항목마다 이미지 URL을 단건 조회하면 항목 수만큼 쿼리가 나가므로(N+1), 파일 식별자를 모아
        // 한 번에 변환한 뒤 매핑한다.
        Map<Long, String> imageUrls = fileService.getUrlsByFileIds(
            contentBoards.stream()
                .map(ShopContentBoardResult::imageFileId)
                .filter(Objects::nonNull)
                .toList()
        );

        return contentBoards.stream()
            .map(dto -> toShopContentBoardResponse(dto, imageUrls))
            .toList();
    }

    private ShopContentBoardResponse toShopContentBoardResponse(ShopContentBoardResult dto, Map<Long, String> imageUrls) {
        return ShopContentBoardResponse.of(
            dto.id(),
            dto.shopId(),
            dto.contentType().name(),
            dto.topic().name(),
            dto.imageFileId() == null ? null : imageUrls.get(dto.imageFileId()),
            dto.youtubeUrl(),
            dto.description(),
            dto.hidden()
        );
    }
}

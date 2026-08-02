package com.tastyhouse.adminapi.shop;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopContentBoard;
import com.tastyhouse.domain.shop.repository.ShopContentBoardRepository;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * admin용 가게 콘텐츠보드 검수 변경 서비스(CQRS command 측).
 *
 * <p>콘텐츠는 점주가 등록하면 즉시 노출되고, 관리자가 사후에 숨김/삭제한다. 단일 애그리거트 연산이라
 * 도메인 서비스로 하강하지 않고 write 포트로 직접 다룬다. 도메인 모델은 순수 POJO라 더티 체킹이
 * 없으므로 상태 변경 후 명시적으로 {@code save}를 호출한다.
 */
@Service
@Transactional
public class ShopContentBoardCommandService {

    private final ShopContentBoardRepository shopContentBoardRepository;

    public ShopContentBoardCommandService(ShopContentBoardRepository shopContentBoardRepository) {
        this.shopContentBoardRepository = shopContentBoardRepository;
    }

    public void changeHidden(Long contentBoardId, boolean hidden) {
        ShopContentBoard shopContentBoard = loadContentBoard(contentBoardId);
        if (hidden) {
            shopContentBoard.hide();
        } else {
            shopContentBoard.unhide();
        }
        shopContentBoardRepository.save(shopContentBoard);
    }

    public void deleteContentBoard(Long contentBoardId) {
        loadContentBoard(contentBoardId);
        shopContentBoardRepository.deleteById(contentBoardId);
    }

    private ShopContentBoard loadContentBoard(Long contentBoardId) {
        return shopContentBoardRepository.findById(contentBoardId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_CONTENT_BOARD_NOT_FOUND));
    }
}

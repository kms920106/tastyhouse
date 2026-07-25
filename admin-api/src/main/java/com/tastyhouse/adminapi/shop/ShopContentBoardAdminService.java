package com.tastyhouse.adminapi.shop;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.shop.application.ShopContentBoardCommandService;

/**
 * admin용 가게 콘텐츠보드 검수 중개 서비스. 소유권 검증 없이 숨김 처리/삭제만 수행한다.
 */
@Service
@RequiredArgsConstructor
public class ShopContentBoardAdminService {

    private final ShopContentBoardCommandService shopContentBoardCommandService;

    public void changeHidden(Long contentBoardId, boolean hidden) {
        shopContentBoardCommandService.changeHidden(contentBoardId, hidden);
    }

    public void deleteContentBoard(Long contentBoardId) {
        shopContentBoardCommandService.deleteContentBoard(contentBoardId);
    }
}

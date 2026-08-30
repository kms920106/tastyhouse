package com.tastyhouse.adminapplication.shop.service;

import com.tastyhouse.adminapplication.shop.port.in.ShopNoticeCommandUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopNoticeHideCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopNoticeUnhideCommand;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopNotice;
import com.tastyhouse.domain.shop.repository.ShopNoticeRepository;
import com.tastyhouse.domain.shop.service.ShopChangeHistoryRecorder;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * admin용 점주 공지 검수 변경 서비스(CQRS command 측).
 *
 * <p>공지는 점주가 등록하면 즉시 노출되고, 관리자가 사후에 게시중단한다. 단일 애그리거트 연산이라 도메인
 * 서비스로 하강하지 않고 write 포트로 직접 다룬다. 도메인 모델은 순수 POJO라 더티 체킹이 없으므로 상태
 * 변경 후 명시적으로 {@code save}를 호출한다.
 *
 * <p><b>{@code exposed}는 건드리지 않는다.</b> 게시중단이 해제되면 점주가 설정한 노출 의도가 복원되어야
 * 하기 때문이다({@code hidden = true}인 동안에는 {@code exposed}와 무관하게 web에서 사라진다).
 *
 * <p>게시중단 사유는 별도 컬럼 없이 변경이력({@code NOTICE}/{@code UPDATE}, 관리자 액터)에 남긴다 —
 * 조치 근거는 감사 대상이므로 유실되면 안 되고, 이력이 이미 "누가·언제·무엇을" 기록하는 공용 경로다.
 */
@Service
@Transactional
public class ShopNoticeCommandService implements ShopNoticeCommandUseCase {

    private final ShopNoticeRepository shopNoticeRepository;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopNoticeCommandService(
        ShopNoticeRepository shopNoticeRepository,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopNoticeRepository = shopNoticeRepository;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    @Override
    public void hideNotice(ShopNoticeHideCommand command) {
        Long adminId = command.adminId();
        Long noticeId = command.noticeId();
        String reason = command.reason();
        ShopNotice notice = loadNotice(noticeId);
        if (notice.isHidden()) {
            throw new BusinessException(ErrorCode.SHOP_NOTICE_ALREADY_HIDDEN);
        }

        notice.hide();
        shopNoticeRepository.save(notice);

        shopChangeHistoryRecorder.record(
            notice.getShopId(),
            ShopChangeType.NOTICE,
            ShopChangeActionType.UPDATE,
            ShopChangeActor.admin(adminId),
            "게시중",
            "게시중단: " + reason
        );
    }

    @Override
    public void unhideNotice(ShopNoticeUnhideCommand command) {
        Long adminId = command.adminId();
        Long noticeId = command.noticeId();
        ShopNotice notice = loadNotice(noticeId);
        if (!notice.isHidden()) {
            throw new BusinessException(ErrorCode.SHOP_NOTICE_NOT_HIDDEN);
        }

        notice.unhide();
        shopNoticeRepository.save(notice);

        shopChangeHistoryRecorder.record(
            notice.getShopId(),
            ShopChangeType.NOTICE,
            ShopChangeActionType.UPDATE,
            ShopChangeActor.admin(adminId),
            "게시중단",
            "게시중"
        );
    }

    private ShopNotice loadNotice(Long noticeId) {
        return shopNoticeRepository.findById(noticeId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOTICE_NOT_FOUND));
    }
}

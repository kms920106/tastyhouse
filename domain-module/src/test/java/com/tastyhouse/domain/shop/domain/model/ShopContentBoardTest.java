package com.tastyhouse.domain.shop.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.tastyhouse.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.domain.shop.domain.vo.ShopId;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다.
 */
class ShopContentBoardTest {

    @Test
    @DisplayName("of로 IMAGE 콘텐츠보드를 생성하면 미영속 상태(식별자·감사시각 없음)이고 숨김 처리되지 않은 상태다")
    void of_createsTransientContentBoard_withImage() {
        ShopContentBoard board = ShopContentBoard.of(ShopId.of(1L), ShopContentType.IMAGE, ShopContentTopic.EXTERIOR, UploadedFileId.of(10L), null, "설명");

        assertThat(board.getId()).isNull();
        assertThat(board.getShopId()).isEqualTo(ShopId.of(1L));
        assertThat(board.getContentType()).isEqualTo(ShopContentType.IMAGE);
        assertThat(board.getTopic()).isEqualTo(ShopContentTopic.EXTERIOR);
        assertThat(board.getImageFileId()).isEqualTo(UploadedFileId.of(10L));
        assertThat(board.getYoutubeUrl()).isNull();
        assertThat(board.getDescription()).isEqualTo("설명");
        assertThat(board.isHidden()).isFalse();
        assertThat(board.getCreatedAt()).isNull();
        assertThat(board.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("VIDEO 콘텐츠보드는 유효한 유튜브 링크가 있으면 정상 생성된다")
    void of_createsTransientContentBoard_withValidYoutubeUrl() {
        ShopContentBoard board = ShopContentBoard.of(
            ShopId.of(1L), ShopContentType.VIDEO, ShopContentTopic.NEWS, null, "https://www.youtube.com/watch?v=abcdefg", "동영상 설명"
        );

        assertThat(board.getContentType()).isEqualTo(ShopContentType.VIDEO);
        assertThat(board.getYoutubeUrl()).isEqualTo("https://www.youtube.com/watch?v=abcdefg");
    }

    @Test
    @DisplayName("VIDEO 콘텐츠보드는 유효하지 않은 유튜브 링크면 예외가 발생한다")
    void of_withInvalidYoutubeUrl_throws() {
        assertThatThrownBy(() ->
            ShopContentBoard.of(ShopId.of(1L), ShopContentType.VIDEO, ShopContentTopic.NEWS, null, "https://example.com/video", "설명")
        )
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_CONTENT_YOUTUBE_URL_INVALID);
    }

    @Test
    @DisplayName("description이 50자를 초과하면 예외가 발생한다")
    void of_withTooLongDescription_throws() {
        String tooLong = "가".repeat(51);

        assertThatThrownBy(() ->
            ShopContentBoard.of(ShopId.of(1L), ShopContentType.IMAGE, ShopContentTopic.EXTERIOR, UploadedFileId.of(10L), null, tooLong)
        )
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_CONTENT_DESCRIPTION_TOO_LONG);
    }

    @Test
    @DisplayName("hide는 숨김 플래그를 true로, unhide는 false로 만든다")
    void hide_and_unhide_toggleHiddenFlag() {
        ShopContentBoard board = ShopContentBoard.of(ShopId.of(1L), ShopContentType.IMAGE, ShopContentTopic.EXTERIOR, UploadedFileId.of(10L), null, "설명");

        board.hide();
        assertThat(board.isHidden()).isTrue();

        board.unhide();
        assertThat(board.isHidden()).isFalse();
    }

    @Test
    @DisplayName("enum from은 유효하지 않은 코드에 대해 BusinessException을 던진다")
    void enum_from_withInvalidCode_throws() {
        assertThatThrownBy(() -> ShopContentType.from("UNKNOWN"))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_CONTENT_TYPE_UNKNOWN);

        assertThatThrownBy(() -> ShopContentTopic.from("UNKNOWN"))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_CONTENT_TOPIC_UNKNOWN);
    }
}

package com.tastyhouse.domain.review.repository;

import java.util.List;

import com.tastyhouse.domain.review.model.ReviewBlindRequestAttachment;

/**
 * 게시중단 요청 첨부 서류 write 포트.
 *
 * <p>불변 애그리거트라 적재만 있고 수정 경로가 없다. 조회는 화면 표시 목적이므로 write 포트가 아니라
 * {@code ReviewBlindRequestQueryDao}의 join 투영이 담당한다(write 포트 잔류 판정 기준).
 */
public interface ReviewBlindRequestAttachmentRepository {

    List<ReviewBlindRequestAttachment> saveAll(List<ReviewBlindRequestAttachment> attachments);
}

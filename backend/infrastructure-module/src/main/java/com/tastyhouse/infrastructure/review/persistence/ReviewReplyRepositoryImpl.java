package com.tastyhouse.infrastructure.review.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.review.model.ReviewReply;
import com.tastyhouse.domain.review.repository.ReviewReplyRepository;
import com.tastyhouse.domain.review.vo.ReviewReplyId;

@Repository
public class ReviewReplyRepositoryImpl implements ReviewReplyRepository {

    private final ReviewReplyJpaRepository reviewReplyJpaRepository;

    public ReviewReplyRepositoryImpl(ReviewReplyJpaRepository reviewReplyJpaRepository) {
        this.reviewReplyJpaRepository = reviewReplyJpaRepository;
    }

    @Override
    public Optional<ReviewReply> findById(ReviewReplyId replyId) {
        return reviewReplyJpaRepository.findById(replyId.value()).map(ReviewReplyMapper::toDomain);
    }

    @Override
    public ReviewReply save(ReviewReply reply) {
        if (reply.getId() == null) {
            ReviewReplyJpaEntity saved = reviewReplyJpaRepository.save(ReviewReplyMapper.toEntity(reply));
            return ReviewReplyMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        ReviewReplyJpaEntity entity = reviewReplyJpaRepository.findById(reply.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 리뷰 답글입니다: " + reply.getId()));
        ReviewReplyMapper.applyChanges(entity, reply);
        return ReviewReplyMapper.toDomain(entity);
    }

    @Override
    public void deleteById(ReviewReplyId replyId) {
        reviewReplyJpaRepository.deleteById(replyId.value());
    }
}

package com.tastyhouse.domain.policy.repository;

import java.util.Optional;

import com.tastyhouse.domain.policy.model.PolicyDocument;
import com.tastyhouse.domain.policy.model.PolicyType;
import com.tastyhouse.domain.policy.vo.PolicyDocumentId;

/**
 * 정책 문서 write 포트.
 *
 * <p>도메인 모델·VO만 주고받는 command 경로 전용 포트다. 표현 목적 조회(Result DTO·페이징·목록)는
 * infrastructure-module의 {@code PolicyQueryDao}가 담당하므로 이 인터페이스에 두지 않는다.
 *
 * <p>{@link #findCurrentEntityByType}은 예외적으로 조회 메서드이지만, "같은 유형의 현행 정책은
 * 하나뿐"이라는 불변식을 검증·전이하기 위해 {@code PolicyActivationService}의 트랜잭션 안에서
 * 도메인 모델을 로드하는 용도이므로 write 포트에 잔류한다.
 */
public interface PolicyDocumentRepository {

    Optional<PolicyDocument> findById(PolicyDocumentId id);

    Optional<PolicyDocument> findCurrentEntityByType(PolicyType type);

    PolicyDocument save(PolicyDocument policyDocument);
}

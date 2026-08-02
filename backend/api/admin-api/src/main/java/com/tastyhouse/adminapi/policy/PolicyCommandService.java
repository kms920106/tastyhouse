package com.tastyhouse.adminapi.policy;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.policy.model.PolicyDocument;
import com.tastyhouse.domain.policy.model.PolicyType;
import com.tastyhouse.domain.policy.repository.PolicyDocumentRepository;
import com.tastyhouse.domain.policy.service.PolicyActivationService;
import com.tastyhouse.domain.policy.vo.PolicyDocumentId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 약관·정책 관리 command 서비스.
 *
 * <p>domain write 포트({@link PolicyDocumentRepository})와 도메인 서비스({@link PolicyActivationService})만
 * 주입해 생성·수정·활성화를 수행한다. 조회는 소비 모듈의 QueryService(web-api {@code PolicyQueryService})가
 * 담당하며, 이 서비스는 infra query DAO를 주입하지 않는다.
 *
 * <p>"같은 유형의 현행 정책은 하나뿐"이라는 불변식은 액터와 무관한 도메인 규칙이라 이 서비스가 직접
 * 처리하지 않고 {@link PolicyActivationService}에 위임한다(공통 지침 분류 C).
 *
 * <p>HTTP 경계에서 {@code String}으로 받은 정책 유형은 이 서비스에서 {@code PolicyType.from(String)}으로
 * 승격한다(도메인 enum 경계 규칙). {@code PolicyDocument}는 순수 POJO라 더티 체킹이 없으므로 도메인
 * 변경 후 명시적으로 {@code policyDocumentRepository.save(policyDocument)}를 호출한다.
 */
@Service
@Transactional
public class PolicyCommandService {

    private final PolicyDocumentRepository policyDocumentRepository;
    private final PolicyActivationService policyActivationService;

    public PolicyCommandService(PolicyDocumentRepository policyDocumentRepository, PolicyActivationService policyActivationService) {
        this.policyDocumentRepository = policyDocumentRepository;
        this.policyActivationService = policyActivationService;
    }

    public Long createPolicy(
        String type,
        String version,
        String title,
        String content,
        boolean mandatory,
        LocalDateTime effectiveDate,
        String createdBy
    ) {
        PolicyDocument policyDocument = PolicyDocument.of(
            PolicyType.from(type),
            version,
            title,
            content,
            mandatory,
            effectiveDate,
            createdBy
        );
        PolicyDocument saved = policyDocumentRepository.save(policyDocument);
        return saved.getPolicyDocumentId().value();
    }

    public void updatePolicy(
        Long id,
        String title,
        String content,
        boolean mandatory,
        LocalDateTime effectiveDate,
        String updatedBy
    ) {
        PolicyDocumentId policyDocumentId = PolicyDocumentId.of(id);
        PolicyDocument policyDocument = findPolicyDocumentOrThrow(policyDocumentId);

        policyDocument.update(title, content, mandatory, effectiveDate, updatedBy);
        policyDocumentRepository.save(policyDocument);
    }

    public void activateCurrentPolicy(Long id) {
        PolicyDocumentId policyDocumentId = PolicyDocumentId.of(id);
        PolicyDocument policyDocument = findPolicyDocumentOrThrow(policyDocumentId);

        policyActivationService.activate(policyDocument);
    }

    private PolicyDocument findPolicyDocumentOrThrow(PolicyDocumentId policyDocumentId) {
        return policyDocumentRepository.findById(policyDocumentId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.POLICY_NOT_FOUND));
    }
}

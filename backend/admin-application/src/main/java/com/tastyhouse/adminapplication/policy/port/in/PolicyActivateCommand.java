package com.tastyhouse.adminapplication.policy.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 현행 약관 활성화 command. 요청 본문이 없는 상태 전이 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record PolicyActivateCommand(Long policyDocumentId) {
    public PolicyActivateCommand {
        if (policyDocumentId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static PolicyActivateCommand of(Long policyDocumentId) {
        return new PolicyActivateCommand(policyDocumentId);
    }
}

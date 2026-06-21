package com.tastyhouse.core.domain.admin.domain.model;

/**
 * 관리자 계정 상태
 * - ACTIVE: 정상 (로그인 가능)
 * - INACTIVE: 비활성화 (로그인 불가)
 */
public enum AdminStatus {
    ACTIVE,
    INACTIVE
}

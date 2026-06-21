package com.tastyhouse.core.domain.admin.domain.model;

/**
 * 관리자 권한 등급
 * - SUPER_ADMIN: 최고관리자. 관리자 계정 생성 등 모든 권한 보유
 * - ADMIN: 일반 관리자
 */
public enum AdminRole {
    SUPER_ADMIN,
    ADMIN
}

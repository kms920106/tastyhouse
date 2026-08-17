/**
 * 알림 유형.
 *
 * `REVIEW_BLIND_APPROVED`는 사장님의 게시중단 요청이 관리자 승인을 받아 리뷰가 숨겨졌을 때
 * 작성자에게 발송된다. 이 알림의 대상 리뷰는 상세 조회가 404이므로 삭제 동의 화면으로 보낸다.
 */
export type NotificationType = 'REVIEW_OWNER_REPLY' | 'REVIEW_BLIND_APPROVED'

export type NotificationTargetType = 'REVIEW'

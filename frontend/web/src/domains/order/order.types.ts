export type OrderMethodType = 'TABLE' | 'RESERVATION' | 'DELIVERY' | 'TAKEOUT'

export type OrderStatusCode = 'PENDING' | 'CONFIRMED' | 'PREPARING' | 'COMPLETED' | 'CANCELLED'

export type OrderErrorCode =
  | 'SHOP_MINIMUM_ORDER_AMOUNT_NOT_MET' // 가게 최소주문금액 미달
  | 'ORDER_MINIMUM_AMOUNT_NOT_MET' // 쿠폰 최소주문금액 미달
  | 'ORDER_PRODUCT_SOLD_OUT' // 품절 상품 포함
  | 'ORDER_PRODUCT_NOT_FOUND' // 상품 없음
  | 'SHOP_NOT_FOUND' // 가게 없음
  | 'ORDER_DELIVERY_TIP_AMOUNT_MISMATCH' // 클라이언트가 보낸 배달팁 ≠ 서버 계산값
  | 'ORDER_PRODUCT_AMOUNT_MISMATCH' // 클라이언트가 보낸 상품 금액 ≠ 서버 계산값 (가격 변경)
  | 'ORDER_DELIVERY_ADDRESS_REQUIRED' // 배달 주문인데 주소가 없음
  | 'ORDER_DELIVERY_AREA_NOT_COVERED' // 가게의 배달 가능 지역이 아님
  | 'SHOP_SCHEDULED_ORDER_DISABLED' // 가게가 예약주문 미운영
  | 'ORDER_SCHEDULE_METHOD_NOT_SUPPORTED' // DELIVERY/TAKEOUT이 아닌 주문방식
  | 'ORDER_SCHEDULED_AT_UNAVAILABLE' // 서버 재계산 결과 유효 슬롯이 아님
  | 'SHOP_NOT_ORDERABLE' // 가게가 주문 불가 상태 (임시중지·휴무·영업시간 밖·휴게시간)
  | 'SHOP_ORDER_METHOD_NOT_SUPPORTED' // 요청한 주문유형을 가게가 취급하지 않음
  | 'SHOP_ORDER_METHOD_SUSPENDED' // 그 주문유형이 임시중지 중

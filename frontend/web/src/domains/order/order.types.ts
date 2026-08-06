export type OrderMethodType = 'TABLE' | 'RESERVATION' | 'DELIVERY' | 'TAKEOUT'

export type OrderStatusCode = 'PENDING' | 'CONFIRMED' | 'PREPARING' | 'COMPLETED' | 'CANCELLED'

export type OrderErrorCode =
  | 'SHOP_MINIMUM_ORDER_AMOUNT_NOT_MET' // 가게 최소주문금액 미달
  | 'ORDER_MINIMUM_AMOUNT_NOT_MET' // 쿠폰 최소주문금액 미달
  | 'ORDER_PRODUCT_SOLD_OUT' // 품절 상품 포함
  | 'ORDER_PRODUCT_NOT_FOUND' // 상품 없음
  | 'SHOP_NOT_FOUND' // 가게 없음
  | 'ORDER_DELIVERY_TIP_AMOUNT_MISMATCH' // 클라이언트가 보낸 배달팁 ≠ 서버 계산값
  | 'ORDER_DELIVERY_ADDRESS_REQUIRED' // 배달 주문인데 주소가 없음
  | 'ORDER_DELIVERY_AREA_NOT_COVERED' // 가게의 배달 가능 지역이 아님

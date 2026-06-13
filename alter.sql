-- ORDER_ITEM -> ORDER_PRODUCT 네이밍 변경 마이그레이션

-- 1. 테이블명 변경
RENAME TABLE ORDER_ITEM TO ORDER_PRODUCT;
RENAME TABLE ORDER_ITEM_OPTION TO ORDER_PRODUCT_OPTION;

-- 2. ORDER_PRODUCT 인덱스명 변경
ALTER TABLE ORDER_PRODUCT RENAME INDEX idx_order_item_order_id TO idx_order_product_order_id;
ALTER TABLE ORDER_PRODUCT RENAME INDEX idx_order_item_product_id TO idx_order_product_product_id;

-- 3. ORDER_PRODUCT_OPTION 컬럼명 변경 (order_item_id -> order_product_id)
ALTER TABLE ORDER_PRODUCT_OPTION
    CHANGE COLUMN order_item_id order_product_id BIGINT NOT NULL COMMENT '주문 상품 ID (ORDER_PRODUCT.id 참조)';

-- 4. ORDER_PRODUCT_OPTION 인덱스명 변경
ALTER TABLE ORDER_PRODUCT_OPTION RENAME INDEX idx_order_item_option_order_item_id TO idx_order_product_option_order_product_id;

INSERT INTO SHOP_AMENITY_CATEGORY
(amenity, display_name, image_url_on, image_url_off, sort, is_active, created_at, updated_at)
VALUES ('PARKING', '주차', '/images/shop/icon-parking-on.png', '/images/shop/icon-parking-off.png', 1, 1, NOW(), NOW()),
       ('RESTROOM', '내부화장실', '/images/shop/icon-toilet-on.png', '/images/shop/icon-toilet-off.png', 2, 1, NOW(),
        NOW()),
       ('RESERVATION', '예약', '/images/shop/icon-reservation-on.png', '/images/shop/icon-reservation-off.png', 3, 1,
        NOW(), NOW()),
       ('BABY_CHAIR', '아기의자', '/images/shop/icon-baby-chair-on.png', '/images/shop/icon-baby-chair-off.png', 4, 1,
        NOW(), NOW()),
       ('PET_FRIENDLY', '애견동반', '/images/shop/icon-pet-on.png', '/images/shop/icon-pet-off.png', 5, 1, NOW(), NOW()),
       ('OUTLET', '개별 콘센트', '/images/shop/icon-socket-on.png', '/images/shop/icon-socket-off.png', 6, 1, NOW(),
        NOW()),
       ('TAKEOUT', '포장', '/images/shop/icon-takeout-on.png', '/images/shop/icon-takeout-off.png', 7, 1, NOW(), NOW()),
       ('DELIVERY', '배달', '/images/shop/icon-delivery-on.png', '/images/shop/icon-delivery-off.png', 8, 1, NOW(),
        NOW());

INSERT INTO SHOP_FOOD_TYPE_CATEGORY
(food_type, display_name, image_url, sort, is_active, created_at, updated_at)
VALUES ('KOREAN', '한식', '/images/shop/icon-filter-korean.png', 1, 1, NOW(), NOW()),
       ('JAPANESE', '일식', '/images/shop/icon-filter-japanese.png', 2, 1, NOW(), NOW()),
       ('WESTERN', '양식', '/images/shop/icon-filter-western.png', 3, 1, NOW(), NOW()),
       ('CHINESE', '중식', '/images/shop/icon-filter-chinese.png', 4, 1, NOW(), NOW()),
       ('WORLD', '세계음식', '/images/shop/icon-filter-world.png', 5, 1, NOW(), NOW()),
       ('SNACK', '분식', '/images/shop/icon-filter-bunsik.png', 6, 1, NOW(), NOW()),
       ('BAR', '주점', '/images/shop/icon-filter-pub.png', 7, 1, NOW(), NOW()),
       ('CAFE', '카페', '/images/shop/icon-filter-cafe.png', 8, 1, NOW(), NOW());

INSERT INTO POLICY_DOCUMENT
(type, version, title, content, is_current, mandatory, effective_date, created_by, created_at, updated_at)
VALUES ('TERMS_OF_SERVICE', '1.0', '이용약관',
        '<h2>제1조 (목적)</h2><p>본 약관은 (이하 ''약관'')은 (주)컬쳐히어로(이하 ''회사'')에서 제공하는 온라인 인상의 인터넷 서비스(이하 ''서비스'')의 이용과 관련하여 회사와 회원간의 권리, 의무 및 책임 사항, 기타 필요한 사항을 규정함을 목적으로 합니다.</p><h2>제2조 (이용약관의 효력 및 변경)</h2><p>본 약관은 회사에서 제공하는 서비스의 이용약관으로 이용자에게 공시함으로써 효력이 발생하며, 합리적인 사유가 발생할 경우 관련 법령에 위배되지 않는 범위 안에서 개정될 수 있습니다.</p>',
        1, 1, NOW(), 'SYSTEM', NOW(), NOW()),
       ('PRIVACY_POLICY', '1.0', '개인정보처리방침',
        '<h2>개인정보의 수집목적 및 이용</h2><p>당사는 다음의 목적을 위하여 개인정보를 처리하고 있으며, 다음의 목적 이외의 용도로는 이용하지 않습니다.</p><ul><li>고객 가입 의사 확인, 고객에 대한 서비스 제공에 따른 본인 식별·인증, 회원자격 유지·관리, 물품 또는 서비스 공급에 따른 금액 결제, 물품 또는 서비스의 공급·배송 등</li></ul><h2>개인정보 수집 항목</h2><p>당사는 정보주체로부터 개인정보를 수집할 때 동의를 받은 개인정보 보유·이용기간 또는 법령에 따른 개인정보 보유·이용기간 내에서 개인정보를 처리·보유합니다.</p>',
        1, 1, NOW(), 'SYSTEM', NOW(), NOW());

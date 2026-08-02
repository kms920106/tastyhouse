## 프로젝트 구조

```
tastyhouse-web/
├── src/
│   ├── app/                              # App Router (라우팅 전용)
│   │   ├── (with-footer)/                # Footer 포함 레이아웃 그룹
│   │   │   ├── (with-sidebar)/           # Sidebar 포함 그룹
│   │   │   │   ├── (home)/               # 홈 페이지
│   │   │   │   ├── places/               # 장소 목록
│   │   │   │   ├── rank/                 # 랭킹
│   │   │   │   └── reviews/              # 리뷰 목록
│   │   │   └── (without-sidebar)/
│   │   │       └── mypage/               # 마이페이지
│   │   │
│   │   ├── auth/                         # 인증
│   │   │   ├── login/
│   │   │   ├── signup/
│   │   │   │   ├── complete/
│   │   │   │   └── social/
│   │   │   ├── forgot-password/
│   │   │   └── callback/                 # OAuth 콜백 (apple, kakao, naver)
│   │   │
│   │   ├── account/                      # 계정 관리
│   │   │   ├── info/
│   │   │   ├── profile/
│   │   │   ├── password/
│   │   │   ├── grade/
│   │   │   └── withdraw/
│   │   │
│   │   ├── places/                       # 장소(식당) 관련
│   │   │   ├── [id]/                     # 장소 상세
│   │   │   │   ├── menus/
│   │   │   │   │   └── [menuId]/
│   │   │   │   └── order/                # 주문 프로세스
│   │   │   │       ├── menus/
│   │   │   │       ├── cart/
│   │   │   │       ├── checkout/
│   │   │   │       ├── method/
│   │   │   │       └── reservation/
│   │   │   ├── filter/
│   │   │   └── suggest/
│   │   │
│   │   ├── orders/                       # 주문 관리
│   │   │   ├── [id]/
│   │   │   │   ├── (detail)/
│   │   │   │   └── complete/
│   │   │   └── reviews/                  # 주문 리뷰
│   │   │       ├── create/
│   │   │       └── edit/
│   │   │
│   │   ├── reviews/                      # 리뷰
│   │   │   └── [id]/
│   │   │       └── product/
│   │   │
│   │   ├── members/                      # 회원 프로필
│   │   │   ├── search/
│   │   │   └── [id]/
│   │   │       └── follows/
│   │   │
│   │   ├── payments/                     # 결제
│   │   │   └── fail/
│   │   │
│   │   ├── notices/                      # 공지사항
│   │   ├── faqs/                         # FAQ
│   │   ├── events/                       # 이벤트
│   │   │   └── [id]/
│   │   ├── coupons/                      # 쿠폰
│   │   ├── point/                        # 포인트
│   │   ├── map/                          # 지도
│   │   ├── advertising/                  # 광고/파트너십
│   │   ├── customer-center/              # 고객센터
│   │   ├── bug-reports/                  # 버그 리포트
│   │   ├── privacy/                      # 개인정보처리방침
│   │   ├── terms/                        # 이용약관
│   │   ├── setting/                      # 설정
│   │   │
│   │   ├── api/                          # API 라우트
│   │   │   ├── files/upload/
│   │   │   ├── logs/
│   │   │   └── payments/                 # 결제 PG (tosspayments, inicis, danal)
│   │   │
│   │   ├── layout.tsx                    # Root Layout
│   │   ├── error.tsx
│   │   └── global-error.tsx
│   │
│   ├── components/                       # 재사용 컴포넌트
│   │   ├── ui/                           # 기본 UI 컴포넌트 (shadcn 포함)
│   │   ├── layouts/                      # 레이아웃 컴포넌트 (Header, Footer, Sidebar)
│   │   ├── reviews/                      # 리뷰 관련 컴포넌트
│   │   ├── places/                       # 장소 관련 컴포넌트
│   │   ├── menus/                        # 메뉴 컴포넌트
│   │   ├── products/                     # 상품 컴포넌트
│   │   ├── order/                        # 주문 컴포넌트
│   │   ├── cart/                         # 장바구니 컴포넌트
│   │   ├── member/                       # 회원 프로필 컴포넌트
│   │   ├── home/                         # 홈 공용 컴포넌트
│   │   ├── mypage/                       # 마이페이지 컴포넌트
│   │   ├── account/                      # 계정 컴포넌트
│   │   ├── modals/                       # 모달 컴포넌트
│   │   └── forms/                        # 폼 컴포넌트
│   │
│   ├── domains/                          # 도메인 레이어 (7-Layer 패턴)
│   │   ├── auth/                         # 인증
│   │   ├── member/                       # 회원
│   │   ├── place/                        # 장소
│   │   ├── order/                        # 주문
│   │   ├── payment/                      # 결제
│   │   ├── review/                       # 리뷰
│   │   ├── product/                      # 상품
│   │   ├── banner/                       # 배너
│   │   ├── rank/                         # 랭킹
│   │   ├── grade/                        # 회원 등급
│   │   ├── event/                        # 이벤트
│   │   ├── notice/                       # 공지사항
│   │   ├── faq/                          # FAQ
│   │   ├── follow/                       # 팔로우
│   │   ├── file/                         # 파일
│   │   ├── policies/                     # 정책 (약관/개인정보)
│   │   ├── email-verification/           # 이메일 인증
│   │   ├── phone-verification/           # 전화 인증
│   │   ├── partnership/                  # 파트너십
│   │   ├── bug-report/                   # 버그 리포트
│   │   └── CLAUDE.md                     # 도메인 작성 가이드
│   │
│   ├── actions/                          # Next.js Server Actions
│   │   └── CLAUDE.md
│   │
│   ├── lib/                              # 유틸리티 & 설정
│   │   ├── api.ts                        # API 클라이언트
│   │   ├── auth-config.ts                # NextAuth 설정
│   │   ├── paths.ts                      # 라우트 경로 상수
│   │   ├── date.ts                       # 날짜 유틸
│   │   ├── number.ts                     # 숫자 포맷팅
│   │   ├── form.ts                       # 폼 유틸
│   │   ├── sanitize.ts                   # HTML 새니타이제이션
│   │   ├── logger.ts / logger-browser.ts # 로거
│   │   ├── uploadFile.ts                 # 파일 업로드
│   │   ├── paymentCalculation.ts         # 결제 금액 계산
│   │   └── utils.ts                      # 일반 유틸
│   │
│   ├── hooks/                            # Custom React Hooks
│   │   ├── useCartInfo.ts
│   │   ├── useFollowMutation.ts
│   │   ├── usePlaceBookmark.ts
│   │   ├── useReviewPanel.ts
│   │   ├── useImageLightbox.ts
│   │   ├── useTossPayments.ts
│   │   ├── usePhoneVerification.ts
│   │   ├── useFileUpload.ts
│   │   ├── useMemberProfile.ts
│   │   └── ...
│   │
│   ├── types/                            # TypeScript 타입 정의
│   │   ├── common.ts
│   │   └── facebook.d.ts
│   │
│   ├── constants/                        # 전역 상수
│   │   └── validation.ts
│   │
│   ├── providers/                        # Context Providers
│   │   └── QueryProvider.tsx
│   │
│   ├── styles/                           # 글로벌 스타일
│   │   └── globals.css
│   │
│   └── middleware.ts                     # Next.js 미들웨어
│
├── e2e/                                  # Playwright E2E 테스트
│   ├── fixtures/                         # 커스텀 픽스처 (인증 세션)
│   ├── page-objects/                     # Page Object Model
│   └── tests/                            # 테스트 파일
│
├── public/                               # 정적 파일
│   ├── images/
│   ├── font/
│   └── favicon.ico
│
├── .vscode/                              # VS Code 설정
├── .env                                  # 환경 변수
├── next.config.ts                        # Next.js 설정
├── tsconfig.json                         # TypeScript 설정
├── tailwind.config.ts                    # Tailwind CSS 설정
├── playwright.config.ts                  # Playwright 설정
└── package.json
```

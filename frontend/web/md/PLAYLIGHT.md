# 전체 실행

npm run e2e

# UI 모드 (권장 — 시각적 디버깅)

npm run e2e:ui

# 특정 파일만

npx playwright test e2e/tests/auth/login.spec.ts

# 디버그 모드

npm run e2e:debug

# 코드 자동 생성 (셀렉터 찾기)

npm run e2e:codegen

---

메모

Playwright 적용 완료
생성된 파일 구조

e2e/
├── .auth/
│ └── user.json ← 인증 세션 저장 (gitignore됨)
├── fixtures/
│ ├── auth.setup.ts ← 로그인 1회 → storageState 저장
│ └── test.ts ← 커스텀 test/expect (POM 주입)
├── page-objects/
│ ├── LoginPage.ts
│ ├── PlacesPage.ts
│ ├── PlaceDetailPage.ts
│ └── OrdersPage.ts
└── tests/
├── auth/
│ └── login.spec.ts ← 비인증 프로젝트
├── places/
│ └── places.spec.ts ← 인증 프로젝트
├── orders/
│ └── orders.spec.ts ← 인증 프로젝트
└── accessibility.spec.ts ← axe-core a11y

playwright.config.ts ← 루트
.github/workflows/e2e.yml ← GitHub Actions CI
Best Practice 적용 포인트
패턴 적용 내용
Global Setup Auth auth.setup.ts에서 로그인 1회 → storageState 재사용 (매 테스트마다 로그인 없음)
Page Object Model 셀렉터를 POM으로 캡슐화하여 UI 변경 시 한 곳만 수정
Custom Fixtures base.extend()로 POM을 DI — 테스트 코드가 깔끔
프로젝트 분리 unauthenticated / chromium / mobile-chrome 분리
접근성 테스트 @axe-core/playwright로 WCAG 자동 검사
CI 최적화 storageState, retries: 2, artifact 업로드, --with-deps
실행 명령어

# 전체 실행

npm run e2e

# UI 모드 (권장 — 시각적 디버깅)

npm run e2e:ui

# 특정 파일만

npx playwright test e2e/tests/auth/login.spec.ts

# 디버그 모드

npm run e2e:debug

# 코드 자동 생성 (셀렉터 찾기)

npm run e2e:codegen
실행 전 설정 필요
.env.local에 E2E 전용 계정 추가:

E2E_USERNAME=테스트계정아이디
E2E_PASSWORD=테스트계정비밀번호
E2E_TEST_PLACE_ID=1
GitHub Secrets에도 동일하게 E2E_USERNAME, E2E_PASSWORD, E2E_API_URL, E2E_TEST_PLACE_ID 추가가 필요합니다.

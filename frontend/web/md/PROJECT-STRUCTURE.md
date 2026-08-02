```
tastyhouse-web/
├── src/                              # 소스 코드 (권장 사항)
│   ├── app/                          # App Router (라우팅 전용)
│   │   ├── (home)/                   # Route Group - 홈
│   │   │   ├── _components/          # 홈 전용 컴포넌트 (Private Folder)
│   │   │   │   └── BannerSection.tsx
│   │   │   ├── layout.tsx
│   │   │   └── page.tsx
│   │   │
│   │   ├── map/                      # 지도 페이지
│   │   │   └── page.tsx
│   │   │
│   │   ├── reviews/                  # 리뷰 페이지
│   │   │   └── create/
│   │   │       └── page.tsx
│   │   │
│   │   └── layout.tsx                # Root Layout
│   │
│   ├── components/                   # 재사용 컴포넌트
│   │   ├── ui/                       # 기본 UI 컴포넌트
│   │   ├── forms/                    # 폼 컴포넌트
│   │   ├── layouts/                  # 레이아웃 컴포넌트
│   │   ├── home/                     # 홈 관련 공용 컴포넌트
│   │   │   ├── KakaoMap.tsx
│   │   │   ├── ReviewButton.tsx
│   │   │   ├── SideBarButton.tsx
│   │   │   └── SideBar.tsx
│   │   └── reviews/                  # 리뷰 관련 공용 컴포넌트
│   │       ├── SubmitButton.tsx
│   │       ├── TagInput.tsx
│   │       ├── ReviewInput.tsx
│   │       └── PhotoUploader.tsx
│   │
│   ├── lib/                          # 유틸리티 & 설정
│   │   ├── api.ts                    # API 클라이언트
│   │   └── places.ts                 # 장소 관련 유틸리티
│   │
│   ├── hooks/                        # Custom React Hooks
│   │
│   ├── types/                        # TypeScript 타입 정의
│   │   └── api/                      # API 관련 타입
│   │       ├── banner.ts
│   │       ├── common.ts
│   │       └── place.ts              # 장소 타입
│   │
│   └── styles/                       # 글로벌 스타일
│       └── globals.css
│
├── public/                           # 정적 파일
│   ├── images/
│   ├── font/
│   └── favicon.ico
│
├── .vscode/                          # VS Code 설정
├── .env                              # 환경 변수
├── next.config.ts                    # Next.js 설정
├── tsconfig.json                     # TypeScript 설정
├── tailwind.config.ts                # Tailwind CSS 설정
└── package.json
```

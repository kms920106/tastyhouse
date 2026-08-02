# Sign in with Apple - 핵심 정리 가이드

> AI 참고용 레퍼런스. Apple Developer 공식 문서 + Auth.js 문서 기반 정리.

---

## 1. 개요

Sign in with Apple은 Apple이 제공하는 OAuth 2.0 + OpenID Connect(Hybrid Flow) 기반 인증 서비스다. 사용자는 Apple ID로 앱/웹에 로그인할 수 있으며, **이메일 숨기기(Private Email Relay)** 기능을 제공한다.

### 핵심 특징

- OAuth 2.0 / OpenID Connect 표준 채택
- scope는 `name`과 `email` 두 가지만 지원
- **사용자 정보(name, email)는 최초 동의 시 1회만 반환** — 이후 로그인에서는 `id_token`만 반환
- `sub` 클레임이 사용자 고유 식별자 (앱별 고정값, pairwise)
- Apple은 **UserInfo 엔드포인트를 제공하지 않음** — `id_token`에서 직접 사용자 정보 추출
- localhost / HTTP URL 미지원 — **HTTPS 필수**

---

## 2. Apple Developer 계정 설정

### 2.1 필요한 리소스 목록

| 리소스 | 설명 | 위치 |
|--------|------|------|
| **App ID** | 앱 고유 식별자 (Bundle ID) | Certificates, Identifiers & Profiles > Identifiers |
| **Services ID** | 웹 인증용 Client ID | Certificates, Identifiers & Profiles > Identifiers |
| **Key (.p8)** | client_secret JWT 서명용 개인키 | Certificates, Identifiers & Profiles > Keys |
| **Team ID** | 10자리 개발자 팀 ID | 계정 멤버십 페이지 우측 상단 |

### 2.2 App ID 생성

1. Identifiers > `+` 버튼 > **App IDs** 선택
2. Bundle ID 입력 (reverse-domain 형식: `com.example.myapp`)
3. Capabilities에서 **Sign in with Apple** 체크
4. Continue > Register

### 2.3 Services ID 생성 (웹용 Client ID)

1. Identifiers > `+` 버튼 > **Services IDs** 선택
2. Description 및 Identifier 입력 (예: `com.example.myapp.web`)
   - 이 Identifier가 OAuth의 **client_id**가 됨
3. **Sign in with Apple** 체크 > **Configure** 클릭
4. **Primary App ID**: 위에서 만든 App ID 선택
5. **Domains and Subdomains**: 도메인 입력 (프로토콜 `https://`와 trailing `/` 제외)
6. **Return URLs**: 콜백 URL 입력 (HTTPS 필수)
7. Done > Continue > Save

> ⚠️ 도메인에 프로토콜(`https://`)이나 trailing slash(`/`)를 포함하면 "Invalid domain" 에러 발생

> 📌 개인 계정: 최대 10개 URL 등록 / 조직 계정: 최대 100개 URL 등록

### 2.4 Key 생성 (.p8 파일)

1. Keys > `+` 버튼 > Key Name 입력
2. **Sign in with Apple** 체크 > Configure
3. Primary App ID 선택 > Save
4. Continue > Register
5. **Download** 클릭하여 `.p8` 파일 저장

> ⚠️ **`.p8` 파일은 1회만 다운로드 가능** — 분실 시 키를 revoke하고 새로 생성해야 함  
> ⚠️ Key ID (10자리)를 별도로 기록해 둘 것

---

## 3. 인증 플로우 (웹)

### 3.1 전체 흐름

```
[사용자] → [Sign in with Apple 버튼 클릭]
       → [Apple 로그인 페이지] (appleid.apple.com/auth/authorize)
       → [Apple 인증 완료]
       → [Redirect URI로 POST 요청] (code, id_token, state, user 전달)
       → [서버에서 code를 token으로 교환] (appleid.apple.com/auth/token)
       → [id_token 검증 및 사용자 식별]
```

### 3.2 Authorization Request

```
GET https://appleid.apple.com/auth/authorize
  ?client_id={Services ID}
  &redirect_uri={등록된 Return URL}
  &response_type=code id_token
  &scope=name email
  &response_mode=form_post
  &state={CSRF 방지용 랜덤 문자열}
  &nonce={리플레이 공격 방지용 해시값}
```

| 파라미터 | 설명 |
|----------|------|
| `client_id` | Services ID (예: `com.example.myapp.web`) |
| `redirect_uri` | Apple Developer에 등록한 Return URL |
| `response_type` | `code` 또는 `code id_token` |
| `scope` | `name email` (공백 구분) |
| `response_mode` | `form_post` (POST로 결과 전달) |
| `state` | CSRF 방지용 — 응답에서 동일 값 확인 |
| `nonce` | 리플레이 방지 — id_token 내에 포함되어 반환 |

### 3.3 Authorization Response (POST)

Apple이 redirect_uri로 POST 요청을 보냄:

```
POST /callback
Content-Type: application/x-www-form-urlencoded

code={authorization_code}
&id_token={JWT}
&state={전송했던 state 값}
&user={"name":{"firstName":"길동","lastName":"홍"},"email":"user@example.com"}
```

> ⚠️ `user` 객체는 **최초 동의 시에만** 포함됨. 반드시 이 시점에 서버에 저장해야 함.

### 3.4 Token Exchange

```
POST https://appleid.apple.com/auth/token
Content-Type: application/x-www-form-urlencoded

client_id={Services ID}
&client_secret={JWT 형태의 client_secret}
&code={authorization_code}
&grant_type=authorization_code
&redirect_uri={등록된 Return URL}
```

> 📌 Apple의 token 엔드포인트는 `User-Agent` 헤더가 필요함

응답:
```json
{
  "access_token": "...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "refresh_token": "...",
  "id_token": "eyJ..."
}
```

---

## 4. Client Secret 생성 (JWT)

Apple은 일반적인 shared secret 대신 **JWT 형태의 client_secret**을 요구한다. `.p8` 개인키로 ES256 서명한다.

### JWT 구조

**Header:**
```json
{
  "alg": "ES256",
  "kid": "{Key ID (10자리)}"
}
```

**Payload:**
```json
{
  "iss": "{Team ID (10자리)}",
  "iat": 1234567890,
  "exp": 1234567890,
  "aud": "https://appleid.apple.com",
  "sub": "{Services ID (= client_id)}"
}
```

### 주요 규칙

- 알고리즘: **ES256** (ECDSA with P-256 curve + SHA-256)
- `iss`: Apple Developer 계정의 Team ID
- `sub`: Services ID (웹) 또는 Bundle ID (네이티브 앱)
- `aud`: 항상 `https://appleid.apple.com`
- `exp`: 최대 **6개월 (180일)** — 만료 전 재생성 필요
- 서명 키: Apple Developer에서 다운받은 `.p8` 파일

### Node.js 예시

```javascript
const jwt = require('jsonwebtoken');
const fs = require('fs');

const privateKey = fs.readFileSync('AuthKey_XXXXXXXXXX.p8');

const clientSecret = jwt.sign({
  iss: 'TEAM_ID',        // 10자리 Team ID
  iat: Math.floor(Date.now() / 1000),
  exp: Math.floor(Date.now() / 1000) + (86400 * 180), // 최대 180일
  aud: 'https://appleid.apple.com',
  sub: 'com.example.myapp.web'  // Services ID
}, privateKey, {
  algorithm: 'ES256',
  header: {
    alg: 'ES256',
    kid: 'KEY_ID'  // 10자리 Key ID
  }
});
```

---

## 5. Identity Token (id_token) 검증

### 5.1 id_token 구조 (JWT, RS256 서명)

**Header:**
```json
{
  "kid": "AIDOPK1",
  "alg": "RS256"
}
```

**Payload (claims):**
```json
{
  "iss": "https://appleid.apple.com",
  "aud": "{client_id (Services ID)}",
  "exp": 1579073561,
  "iat": 1579072961,
  "sub": "{Apple 사용자 고유 식별자}",
  "email": "user@example.com",
  "email_verified": "true",
  "is_private_email": "true",
  "auth_time": 1579072961,
  "nonce": "{전송한 nonce}",
  "nonce_supported": true,
  "real_user_status": 2
}
```

### 5.2 주요 클레임 설명

| 클레임 | 설명 |
|--------|------|
| `sub` | **사용자 고유 식별자** — DB의 사용자 키로 사용. 앱별 고유값(pairwise) |
| `email` | 실제 이메일 또는 Private Relay 주소 (`@privaterelay.appleid.com`) |
| `email_verified` | 항상 `true` (Apple은 검증된 이메일만 반환) — String 또는 Boolean |
| `is_private_email` | 이메일이 프라이빗 릴레이 주소인지 여부 |
| `real_user_status` | 실제 사용자 여부: `0`=Unsupported, `1`=Unknown, `2`=LikelyReal (iOS 14+ 전용, 웹 미지원) |
| `transfer_sub` | 앱 이전 시 사용자 마이그레이션용 (60일간만 유효) |
| `nonce_supported` | nonce 지원 여부 — `true`면 nonce 필수 검증, `false`면 선택 |

### 5.3 검증 절차

1. Apple의 공개키 가져오기: `GET https://appleid.apple.com/auth/keys` (JWKS 형식)
2. id_token의 header.kid와 일치하는 공개키 선택
3. RS256으로 서명 검증
4. 다음 클레임 검증:
   - `iss` = `https://appleid.apple.com`
   - `aud` = 내 `client_id`
   - `exp` > 현재 시간 (만료 확인)
   - `nonce` = 전송했던 nonce 값 (사용한 경우)

---

## 6. REST API 엔드포인트

### Apple OpenID Configuration

```
GET https://appleid.apple.com/.well-known/openid-configuration
```

```json
{
  "issuer": "https://appleid.apple.com",
  "authorization_endpoint": "https://appleid.apple.com/auth/authorize",
  "token_endpoint": "https://appleid.apple.com/auth/token",
  "jwks_uri": "https://appleid.apple.com/auth/keys",
  "response_types_supported": ["code"],
  "response_modes_supported": ["query", "fragment", "form_post"],
  "subject_types_supported": ["pairwise"],
  "id_token_signing_alg_values_supported": ["RS256"],
  "scopes_supported": ["openid", "email", "name"],
  "token_endpoint_auth_methods_supported": ["client_secret_post"],
  "claims_supported": ["aud", "email", "email_verified", "exp", "iat", "iss", "sub"]
}
```

### API 목록

| 엔드포인트 | 메서드 | 용도 |
|-----------|--------|------|
| `/auth/authorize` | GET | 인증 요청 (사용자를 Apple 로그인 페이지로 이동) |
| `/auth/token` | POST | authorization_code → 토큰 교환 / refresh_token 갱신 |
| `/auth/revoke` | POST | 토큰 무효화 (계정 삭제 시 필수) |
| `/auth/keys` | GET | Apple 공개키 (JWKS) 조회 |

### Token Revoke (계정 삭제 시 필수)

```
POST https://appleid.apple.com/auth/revoke
Content-Type: application/x-www-form-urlencoded

client_id={Services ID}
&client_secret={JWT client_secret}
&token={access_token 또는 refresh_token}
&token_type_hint=refresh_token
```

> ⚠️ App Store 앱이 계정 삭제를 지원하면 반드시 토큰 revoke 필요 (App Store 심사 요구사항)  
> ⚠️ authorization_code는 **10분 후 만료** — 즉시 토큰 교환 필요  
> ⚠️ refresh_token은 **반드시 서버에 저장**해야 나중에 revoke 가능

### Server-to-Server 알림

Apple은 사용자 이벤트 발생 시 등록된 엔드포인트로 알림을 보낸다:

| 이벤트 타입 | 설명 |
|------------|------|
| `consent-revoked` | 사용자가 Apple ID 설정에서 앱 연결 해제 |
| `account-delete` | 사용자가 Apple ID 계정 삭제 |
| `email-disabled` | 사용자가 이메일 릴레이 비활성화 |
| `email-enabled` | 사용자가 이메일 릴레이 재활성화 |

---

## 7. Sign in with Apple JS SDK (프론트엔드)

### 7.1 SDK 로드

```html
<script
  type="text/javascript"
  src="https://appleid.cdn-apple.com/appleauth/static/jsapi/appleid/1/en_US/appleid.auth.js"
></script>
```

### 7.2 초기화

```javascript
AppleID.auth.init({
  clientId: 'com.example.myapp.web',   // Services ID
  scope: 'name email',                  // 요청할 scope
  redirectURI: 'https://example.com/callback', // 등록된 Return URL
  state: 'random_state_string',         // CSRF 방지
  nonce: 'hashed_nonce_value',          // 리플레이 방지 (SHA256 해시값)
  usePopup: true                        // true: 팝업 / false: 리다이렉트
});
```

### 7.3 로그인 트리거

```javascript
// 방법 1: 프로그래밍 방식
document.getElementById('apple-btn').addEventListener('click', async () => {
  const response = await AppleID.auth.signIn();
});

// 방법 2: Apple 기본 버튼 (자동 렌더링)
<div
  id="appleid-signin"
  data-color="black"
  data-border="true"
  data-type="sign in"
></div>
```

### 7.4 버튼 커스터마이징 속성

| 속성 | 값 | 설명 |
|------|------|------|
| `data-color` | `black`, `white` | 버튼 색상 |
| `data-border` | `true`, `false` | 테두리 표시 |
| `data-type` | `sign in`, `continue` | 버튼 텍스트 |

### 7.5 이벤트 리스너 (Popup 모드)

```javascript
// 성공
document.addEventListener('AppleIDSignInOnSuccess', (event) => {
  const { authorization, user } = event.detail;
  const { code, id_token, state } = authorization;
  // user는 최초 동의 시에만 존재
  // { email: "...", name: { firstName: "...", lastName: "..." } }
});

// 실패
document.addEventListener('AppleIDSignInOnFailure', (event) => {
  const { error } = event.detail;
  console.error(error);
  // 사용자가 팝업 닫으면 에러 발생
});
```

---

## 8. Auth.js (NextAuth) 연동

### 8.1 기본 설정

```typescript
// auth.ts
import Apple from "@auth/core/providers/apple";

export const { handlers, signIn, signOut, auth } = NextAuth({
  providers: [
    Apple({
      clientId: process.env.AUTH_APPLE_ID,        // Services ID
      clientSecret: process.env.AUTH_APPLE_SECRET, // JWT client_secret
    }),
  ],
});
```

### 8.2 Callback URL

```
https://example.com/auth/callback/apple
```

> 이 URL을 Apple Developer > Services ID > Return URLs에 등록해야 함

### 8.3 Client Secret 생성

Auth.js CLI 제공:

```bash
npx auth add apple
```

필요한 정보를 입력하면 `.env`에 `AUTH_APPLE_ID`와 `AUTH_APPLE_SECRET`이 자동 추가된다.

### 8.4 AppleProfile 타입 (Auth.js)

```typescript
interface AppleProfile {
  sub: string;           // 사용자 고유 ID
  email: string;         // 이메일
  email_verified: true | "true";
  is_private_email: boolean | "true" | "false";
  iss: "https://appleid.apple.com";
  aud: string;           // client_id
  iat: number;
  exp: number;
  auth_time: number;
  at_hash: string;
  nonce: string;
  nonce_supported: boolean;
  real_user_status: 0 | 1 | 2;  // 웹 미지원
  transfer_sub: string;          // 앱 이전 시에만
  user?: {                        // 최초 동의 시에만
    email: string;
    name: { firstName: string; lastName: string; };
  };
}
```

### 8.5 주의사항

- Apple은 **localhost/HTTP 미지원** — 개발 시 ngrok 등 HTTPS 터널 필요
- client_secret은 **JWT 형태** — 최대 6개월 유효, 만료 전 재생성 필요
- 사용자 정보(name, email)는 **최초 1회만** 반환
- 동일 클라이언트를 여러 배포 환경(preview deployment 등)에서 공유 불가

---

## 9. 실전 체크리스트

### 개발 환경 세팅

- [ ] Apple Developer Program 등록
- [ ] App ID 생성 + Sign in with Apple 활성화
- [ ] Services ID 생성 + 도메인/Return URL 설정
- [ ] Key(.p8) 생성 + 안전하게 보관
- [ ] Team ID, Key ID, Services ID 기록
- [ ] client_secret JWT 생성 로직 구현
- [ ] HTTPS 환경 준비 (개발: ngrok / 운영: SSL)

### 구현 필수 사항

- [ ] 최초 로그인 시 `user` 객체 (name, email) 서버에 저장
- [ ] `sub` 클레임을 사용자 고유 키로 사용
- [ ] id_token 서명 검증 (Apple JWKS로 RS256 검증)
- [ ] state 파라미터로 CSRF 방지
- [ ] nonce 파라미터로 리플레이 방지
- [ ] authorization_code 10분 내 토큰 교환
- [ ] refresh_token 서버 저장 (revoke 용)
- [ ] Private Email Relay 주소 대응 (`@privaterelay.appleid.com`)

### 계정 삭제 시

- [ ] 저장된 refresh_token으로 `/auth/revoke` 호출
- [ ] 서버의 사용자 데이터 삭제
- [ ] Server-to-Server `consent-revoked` 알림 핸들링

---

## 10. 주요 ID/키 정리표

| 값 | 어디서 확인 | 용도 |
|----|-----------|------|
| **Team ID** | Developer 계정 > 멤버십 (우측 상단) | client_secret JWT의 `iss` |
| **Services ID** | Identifiers > Services IDs | OAuth `client_id` |
| **Key ID** | Keys 목록에서 확인 | client_secret JWT Header의 `kid` |
| **Bundle ID** | Identifiers > App IDs | 네이티브 앱의 `client_id` |
| **.p8 파일** | Key 생성 시 다운로드 | client_secret JWT 서명용 개인키 |

---

## 11. 테스트/디버깅 팁

### 사용자 정보 재수신 (user 객체 다시 받기)

이미 동의한 앱의 user 객체를 다시 받으려면 앱 연결을 해제해야 한다:

- **iPhone**: 설정 > Apple ID > 비밀번호 및 보안 > Apple ID로 로그인한 앱 > 앱 선택 > "Apple ID 사용 중단"
- **웹**: appleid.apple.com > 로그인 > 보안 > Apple ID를 사용하는 앱 관리 > 앱 선택 > "Apple ID 사용 중단"

### 자주 발생하는 에러

| 에러 | 원인 | 해결 |
|------|------|------|
| `invalid_client` | client_secret JWT 오류 (kid, iss, sub, aud 확인) | JWT payload/header 값 및 .p8 키 확인 |
| `Invalid domain` | 도메인에 프로토콜/slash 포함 | `https://` 와 trailing `/` 제거 |
| `redirect_uri_mismatch` | Return URL 불일치 | Apple Developer에 등록된 URL과 정확히 일치하는지 확인 |

---

## 참고 링크

- [Apple Developer - Sign in with Apple 개요](https://developer.apple.com/sign-in-with-apple/get-started/)
- [웹 환경 설정](https://developer.apple.com/documentation/signinwithapple/configuring-your-environment-for-sign-in-with-apple)
- [웹페이지 구성](https://developer.apple.com/documentation/signinwithapple/configuring-your-webpage-for-sign-in-with-apple)
- [Sign in with Apple JS SDK](https://developer.apple.com/documentation/signinwithapplejs)
- [REST API](https://developer.apple.com/documentation/signinwithapplerestapi)
- [Services ID 설정](https://developer.apple.com/help/account/capabilities/configure-sign-in-with-apple-for-the-web/)
- [Auth.js Apple Provider](https://authjs.dev/reference/core/providers/apple)
- [OpenID Configuration](https://appleid.apple.com/.well-known/openid-configuration)
- [Apple Public Keys (JWKS)](https://appleid.apple.com/auth/keys)

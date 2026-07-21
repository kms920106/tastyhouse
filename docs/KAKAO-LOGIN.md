# 카카오 로그인 REST API 문서

> 참고: https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api

---

## 전체 흐름

```
1. 클라이언트 → 카카오 인증 서버: 인가 코드 요청 (GET /oauth/authorize)
2. 사용자: 카카오 로그인 및 동의
3. 카카오 인증 서버 → 클라이언트: 인가 코드 전달 (redirect_uri?code=xxx)
4. 클라이언트 → 서버: 인가 코드 전달
5. 서버 → 카카오 인증 서버: 토큰 요청 (POST /oauth/token)
6. 카카오 인증 서버 → 서버: access_token, refresh_token 발급
7. 서버 → 카카오 API 서버: 사용자 정보 조회 (GET /v2/user/me)
8. 카카오 API 서버 → 서버: 사용자 정보 반환
```

---

## 1. 인가 코드 받기

### 기본 정보

| 항목 | 값 |
|------|-----|
| 메서드 | `GET` |
| URL | `https://kauth.kakao.com/oauth/authorize` |
| 인증 | 없음 |

### 요청 - 쿼리 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|:----:|------|
| `client_id` | String | O | 앱 REST API 키 |
| `redirect_uri` | String | O | 인가 코드를 전달받을 서비스 서버의 URI |
| `response_type` | String | O | `code`로 고정 |
| `scope` | String | X | 추가 동의 요청 시 동의항목 ID 목록 (쉼표 구분) |
| `state` | String | X | CSRF 방지용 임의 문자열 (요청과 응답에서 동일 값 유지) |
| `nonce` | String | X | ID 토큰 재생 공격 방지용 임의 문자열 |
| `prompt` | String | X | `login`: 재인증 요청 / `none`: 인증 정보 있으면 동의 화면 생략 / `create`: 회원가입 / `select_account`: 계정 선택 |
| `login_hint` | String | X | 카카오 로그인 페이지 ID란에 자동 입력할 값 |
| `service_terms` | String | X | 동의 화면에 포함할 서비스 약관 태그 목록 (쉼표 구분) |

### 요청 예시

```
https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=${REST_API_KEY}&redirect_uri=${REDIRECT_URI}
```

### 응답 - 성공 (redirect_uri로 리다이렉트)

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `code` | String | 토큰 받기에 필요한 인가 코드 |
| `state` | String | 요청 시 전달한 state 값 (있는 경우) |

```
HTTP/1.1 302
Location: ${REDIRECT_URI}?code=${AUTHORIZE_CODE}&state=${STATE}
```

### 응답 - 실패 (redirect_uri로 리다이렉트)

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `error` | String | 에러 코드 |
| `error_description` | String | 에러 메시지 |
| `state` | String | 요청 시 전달한 state 값 (있는 경우) |

```
HTTP/1.1 302
Location: ${REDIRECT_URI}?error=access_denied&error_description=User%20denied%20access
```

---

## 2. 토큰 받기 (Access Token 발급)

### 기본 정보

| 항목 | 값 |
|------|-----|
| 메서드 | `POST` |
| URL | `https://kauth.kakao.com/oauth/token` |
| 인증 | 없음 |

### 요청 - 헤더

| 헤더 | 값 | 필수 |
|------|-----|:----:|
| `Content-Type` | `application/x-www-form-urlencoded;charset=utf-8` | O |

### 요청 - 본문 (Form)

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|:----:|------|
| `grant_type` | String | O | `authorization_code`로 고정 |
| `client_id` | String | O | 앱 REST API 키 |
| `redirect_uri` | String | O | 인가 코드가 리다이렉트된 URI (인가 코드 요청 시 사용한 값과 동일해야 함) |
| `code` | String | O | 인가 코드 요청으로 얻은 인가 코드 |
| `client_secret` | String | X | 보안 > Client Secret 활성화한 경우 필수 |

### 요청 예시

```bash
curl -X POST "https://kauth.kakao.com/oauth/token" \
  -H "Content-Type: application/x-www-form-urlencoded;charset=utf-8" \
  -d "grant_type=authorization_code" \
  -d "client_id=${REST_API_KEY}" \
  -d "redirect_uri=${REDIRECT_URI}" \
  -d "code=${AUTHORIZE_CODE}"
```

### 응답 - 본문

| 필드 | 타입 | 필수 | 설명 |
|------|------|:----:|------|
| `token_type` | String | O | `bearer`로 고정 |
| `access_token` | String | O | 사용자 액세스 토큰 |
| `id_token` | String | X | OpenID Connect 활성화 시 발급되는 ID 토큰 |
| `expires_in` | Integer | O | 액세스 토큰 만료까지 남은 시간 (초) |
| `refresh_token` | String | O | 사용자 리프레시 토큰 |
| `refresh_token_expires_in` | Integer | O | 리프레시 토큰 만료까지 남은 시간 (초) |
| `scope` | String | X | 인증된 사용자 정보 조회 권한 범위 (공백 구분) |

### 응답 예시

```json
{
  "token_type": "bearer",
  "access_token": "${ACCESS_TOKEN}",
  "expires_in": 43199,
  "refresh_token": "${REFRESH_TOKEN}",
  "refresh_token_expires_in": 5184000,
  "scope": "account_email profile"
}
```

---

## 3. 토큰 갱신 (Refresh Token으로 Access Token 재발급)

### 기본 정보

| 항목 | 값 |
|------|-----|
| 메서드 | `POST` |
| URL | `https://kauth.kakao.com/oauth/token` |
| 인증 | 없음 |

### 요청 - 헤더

| 헤더 | 값 | 필수 |
|------|-----|:----:|
| `Content-Type` | `application/x-www-form-urlencoded;charset=utf-8` | O |

### 요청 - 본문 (Form)

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|:----:|------|
| `grant_type` | String | O | `refresh_token`으로 고정 |
| `client_id` | String | O | 앱 REST API 키 |
| `refresh_token` | String | O | 토큰 발급 시 받은 리프레시 토큰 |
| `client_secret` | String | X | 보안 > Client Secret 활성화한 경우 필수 |

### 요청 예시

```bash
curl -X POST "https://kauth.kakao.com/oauth/token" \
  -H "Content-Type: application/x-www-form-urlencoded;charset=utf-8" \
  -d "grant_type=refresh_token" \
  -d "client_id=${REST_API_KEY}" \
  -d "refresh_token=${REFRESH_TOKEN}"
```

### 응답 - 본문

| 필드 | 타입 | 필수 | 설명 |
|------|------|:----:|------|
| `token_type` | String | O | `bearer`로 고정 |
| `access_token` | String | O | 갱신된 액세스 토큰 |
| `id_token` | String | X | 갱신된 ID 토큰 (OpenID Connect 사용 시) |
| `expires_in` | Integer | O | 액세스 토큰 만료까지 남은 시간 (초) |
| `refresh_token` | String | X | 갱신된 리프레시 토큰 (**만료까지 1개월 미만인 경우에만 반환**) |
| `refresh_token_expires_in` | Integer | X | 리프레시 토큰 만료까지 남은 시간 (초) |

### 응답 예시

```json
{
  "token_type": "bearer",
  "access_token": "${ACCESS_TOKEN}",
  "expires_in": 43199,
  "refresh_token": "${REFRESH_TOKEN}",
  "refresh_token_expires_in": 5184000
}
```

> **참고**: 리프레시 토큰의 만료 시간이 1개월 이상 남은 경우, 응답에 `refresh_token`이 포함되지 않습니다. 기존 리프레시 토큰을 그대로 사용하면 됩니다.

---

## 4. 사용자 정보 가져오기

### 기본 정보

| 항목 | 값 |
|------|-----|
| 메서드 | `GET` 또는 `POST` |
| URL | `https://kapi.kakao.com/v2/user/me` |
| 인증 | 액세스 토큰 또는 서비스 앱 어드민 키 |

### 4-1. 액세스 토큰 방식

#### 요청 - 헤더

| 헤더 | 값 | 필수 |
|------|-----|:----:|
| `Authorization` | `Bearer ${ACCESS_TOKEN}` | O |
| `Content-Type` | `application/x-www-form-urlencoded;charset=utf-8` | X |

#### 요청 - 쿼리 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|:----:|------|
| `secure_resource` | Boolean | X | 이미지 URL을 HTTPS로 받을지 여부 (기본값: false) |
| `property_keys` | String[] | X | 특정 사용자 정보만 조회하고 싶을 때 지정 |

### 4-2. 어드민 키 방식

#### 요청 - 헤더

| 헤더 | 값 | 필수 |
|------|-----|:----:|
| `Authorization` | `KakaoAK ${SERVICE_APP_ADMIN_KEY}` | O |
| `Content-Type` | `application/x-www-form-urlencoded;charset=utf-8` | O |

#### 요청 - 본문

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|:----:|------|
| `target_id_type` | String | O | `user_id`로 고정 |
| `target_id` | Long | O | 조회할 사용자의 회원번호 |
| `secure_resource` | Boolean | X | 이미지 URL HTTPS 여부 |
| `property_keys` | String[] | X | 특정 사용자 정보만 조회 |

### property_keys 사용 가능 값

| 키 | 설명 |
|----|------|
| `kakao_account.profile` | 프로필 (닉네임, 프로필 사진) |
| `kakao_account.name` | 이름 |
| `kakao_account.email` | 이메일 |
| `kakao_account.age_range` | 연령대 |
| `kakao_account.birthday` | 생일 |
| `kakao_account.gender` | 성별 |
| `kakao_account.phone_number` | 전화번호 |

### 요청 예시

```bash
# 전체 정보 조회
curl -G "https://kapi.kakao.com/v2/user/me" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"

# 특정 정보만 조회
curl -X POST "https://kapi.kakao.com/v2/user/me" \
  -H "Content-Type: application/x-www-form-urlencoded;charset=utf-8" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  --data-urlencode 'property_keys=["kakao_account.email"]'
```

### 응답 - 본문 (최상위)

| 필드 | 타입 | 필수 | 설명 |
|------|------|:----:|------|
| `id` | Long | O | 회원번호 |
| `has_signed_up` | Boolean | X | 자동 연결 비활성 앱에서 수동 연결 여부 |
| `connected_at` | String | X | 서비스에 연결 완료된 시각 (UTC) |
| `synched_at` | String | X | 카카오싱크 간편가입으로 로그인한 시각 (UTC) |
| `properties` | Object | X | 사용자 프로퍼티 (커스텀 키-값) |
| `kakao_account` | Object | X | 카카오계정 정보 |
| `for_partner` | Object | X | UUID 등 파트너 추가 정보 |

### 응답 - kakao_account 객체

| 필드 | 타입 | 설명 |
|------|------|------|
| `profile_needs_agreement` | Boolean | 프로필 정보 동의 필요 여부 |
| `profile_nickname_needs_agreement` | Boolean | 닉네임 동의 필요 여부 |
| `profile_image_needs_agreement` | Boolean | 프로필 사진 동의 필요 여부 |
| `profile` | Object | 프로필 정보 (아래 참조) |
| `name_needs_agreement` | Boolean | 이름 동의 필요 여부 |
| `name` | String | 이름 |
| `email_needs_agreement` | Boolean | 이메일 동의 필요 여부 |
| `is_email_valid` | Boolean | 이메일 유효 여부 |
| `is_email_verified` | Boolean | 이메일 인증 여부 |
| `email` | String | 이메일 |
| `age_range_needs_agreement` | Boolean | 연령대 동의 필요 여부 |
| `age_range` | String | 연령대 (`1~9`, `10~14`, `15~19`, `20~29`, `30~39`, ...) |
| `gender_needs_agreement` | Boolean | 성별 동의 필요 여부 |
| `gender` | String | 성별 (`female` / `male`) |
| `phone_number_needs_agreement` | Boolean | 전화번호 동의 필요 여부 |
| `phone_number` | String | 전화번호 (예: `+82 010-1234-5678`) |

### 응답 - profile 객체

| 필드 | 타입 | 설명 |
|------|------|------|
| `nickname` | String | 닉네임 |
| `thumbnail_image_url` | String | 프로필 미리보기 이미지 URL (110x110) |
| `profile_image_url` | String | 프로필 사진 URL (640x640) |
| `is_default_image` | Boolean | 기본 프로필 사진 사용 여부 |
| `is_default_nickname` | Boolean | 기본 닉네임 사용 여부 |

### 응답 예시

```json
{
  "id": 123456789,
  "connected_at": "2022-04-11T01:45:28Z",
  "kakao_account": {
    "profile_nickname_needs_agreement": false,
    "profile_image_needs_agreement": false,
    "profile": {
      "nickname": "홍길동",
      "thumbnail_image_url": "http://k.kakaocdn.net/.../img_110x110.jpg",
      "profile_image_url": "http://k.kakaocdn.net/.../img_640x640.jpg",
      "is_default_image": false,
      "is_default_nickname": false
    },
    "name_needs_agreement": false,
    "name": "홍길동",
    "email_needs_agreement": false,
    "is_email_valid": true,
    "is_email_verified": true,
    "email": "sample@example.com",
    "gender_needs_agreement": false,
    "gender": "female",
    "phone_number_needs_agreement": false,
    "phone_number": "+82 010-1234-5678"
  },
  "properties": {
    "custom_key": "custom_value"
  }
}
```

---

## 5. 로그아웃

### 기본 정보

| 항목 | 값 |
|------|-----|
| 메서드 | `POST` |
| URL | `https://kapi.kakao.com/v1/user/logout` |
| 인증 | 액세스 토큰 또는 서비스 앱 어드민 키 |

### 5-1. 액세스 토큰 방식

#### 요청 - 헤더

| 헤더 | 값 | 필수 |
|------|-----|:----:|
| `Authorization` | `Bearer ${ACCESS_TOKEN}` | O |

### 5-2. 어드민 키 방식

#### 요청 - 헤더

| 헤더 | 값 | 필수 |
|------|-----|:----:|
| `Authorization` | `KakaoAK ${SERVICE_APP_ADMIN_KEY}` | O |
| `Content-Type` | `application/x-www-form-urlencoded;charset=utf-8` | O |

#### 요청 - 본문

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|:----:|------|
| `target_id_type` | String | O | `user_id`로 고정 |
| `target_id` | Long | O | 로그아웃시킬 사용자의 회원번호 |

### 응답 - 본문

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | Long | 로그아웃된 사용자의 회원번호 |

### 요청 예시

```bash
curl -X POST "https://kapi.kakao.com/v1/user/logout" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
```

### 응답 예시

```json
{
  "id": 123456789
}
```

> **참고**: 로그아웃은 액세스 토큰과 리프레시 토큰을 모두 만료시킵니다. 서비스 자체 세션은 별도로 처리해야 합니다.

---

## 6. 연결 끊기 (Unlink)

### 기본 정보

| 항목 | 값 |
|------|-----|
| 메서드 | `POST` |
| URL | `https://kapi.kakao.com/v1/user/unlink` |
| 인증 | 액세스 토큰 또는 서비스 앱 어드민 키 |

### 6-1. 액세스 토큰 방식

#### 요청 - 헤더

| 헤더 | 값 | 필수 |
|------|-----|:----:|
| `Authorization` | `Bearer ${ACCESS_TOKEN}` | O |

### 6-2. 어드민 키 방식

#### 요청 - 헤더

| 헤더 | 값 | 필수 |
|------|-----|:----:|
| `Authorization` | `KakaoAK ${SERVICE_APP_ADMIN_KEY}` | O |
| `Content-Type` | `application/x-www-form-urlencoded;charset=utf-8` | O |

#### 요청 - 본문

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|:----:|------|
| `target_id_type` | String | O | `user_id`로 고정 |
| `target_id` | Long | O | 연결을 끊을 사용자의 회원번호 |

### 응답 - 본문

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | Long | 연결 끊기에 성공한 사용자의 회원번호 |

### 요청 예시

```bash
curl -X POST "https://kapi.kakao.com/v1/user/unlink" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
```

### 응답 예시

```json
{
  "id": 123456789
}
```

> **참고**: 연결 끊기는 앱과 사용자의 연결을 해제합니다. 로그아웃과 달리 사용자 데이터도 삭제됩니다. 이후 같은 사용자가 다시 로그인하면 새로운 연결이 생성됩니다.

---

## 7. 액세스 토큰 정보 보기

### 기본 정보

| 항목 | 값 |
|------|-----|
| 메서드 | `GET` |
| URL | `https://kapi.kakao.com/v1/user/access_token_info` |
| 인증 | 액세스 토큰 |

### 요청 - 헤더

| 헤더 | 값 | 필수 |
|------|-----|:----:|
| `Authorization` | `Bearer ${ACCESS_TOKEN}` | O |

### 응답 - 본문

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | Long | 회원번호 |
| `expires_in` | Integer | 액세스 토큰 만료까지 남은 시간 (초) |
| `app_id` | Integer | 토큰이 발급된 앱 ID |

### 요청 예시

```bash
curl -G "https://kapi.kakao.com/v1/user/access_token_info" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
```

### 응답 예시

```json
{
  "id": 123456789,
  "expires_in": 7199,
  "app_id": 412345
}
```

---

## API URL 요약

| API | 메서드 | URL |
|-----|--------|-----|
| 인가 코드 받기 | GET | `https://kauth.kakao.com/oauth/authorize` |
| 토큰 받기 | POST | `https://kauth.kakao.com/oauth/token` |
| 토큰 갱신 | POST | `https://kauth.kakao.com/oauth/token` |
| 사용자 정보 가져오기 | GET/POST | `https://kapi.kakao.com/v2/user/me` |
| 로그아웃 | POST | `https://kapi.kakao.com/v1/user/logout` |
| 연결 끊기 | POST | `https://kapi.kakao.com/v1/user/unlink` |
| 토큰 정보 보기 | GET | `https://kapi.kakao.com/v1/user/access_token_info` |

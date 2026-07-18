# MemoryMe Backend

MemoryMe는 웹에서 발견한 링크, 텍스트, 이미지, 영상, 파일을 빠르게 저장하고 나중에 다시 꺼내 보기 쉽게 정리하는 개인 지식 저장 서비스입니다.

이 저장소는 MemoryMe의 백엔드 서버입니다. 사용자의 인증 상태를 관리하고, 임시 메모를 보드와 노트로 구조화하며, 첨부파일을 S3에 저장하고, 링크 메타데이터와 검색 인덱스를 관리합니다.

## 프로젝트 배경

웹에서 자료를 모을 때 저장 위치가 쉽게 흩어집니다. 링크는 브라우저 북마크에 남고, 이미지는 갤러리에 쌓이며, 간단한 생각은 메모 앱이나 채팅방에 남습니다. 시간이 지나면 저장했다는 사실은 기억나지만, 어디에 저장했는지 찾기 어려워집니다.

MemoryMe는 이 문제를 두 단계로 나눠 해결합니다.

1. 지금은 빠르게 저장한다.
2. 나중에 보드와 노트로 정리하고 검색한다.

백엔드는 이 흐름을 위해 단순 CRUD보다 다음 지점에 집중했습니다.

- 사용자가 저장한 메모를 나중에 보드/노트 구조로 변환할 수 있어야 한다.
- 링크만 저장해도 제목, 설명, 썸네일, 요약을 함께 보여줄 수 있어야 한다.
- 파일은 S3에 저장하되 사용자별 접근 범위를 서버에서 검증해야 한다.
- 검색은 DB 조회와 분리해 Elasticsearch 인덱스로 확장할 수 있어야 한다.
- 회원 탈퇴 시 DB 데이터뿐 아니라 외부 저장소 정리 대상도 추적해야 한다.

## 기술 스택

| 분류 | 기술 |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 4.0.2, Spring MVC |
| Persistence | Spring Data JPA, MySQL |
| Security | Spring Security, JWT, BCrypt |
| OAuth | Kakao Login, Apple Login |
| File Storage | AWS S3, AWS SDK for Java v2 |
| Search | Elasticsearch, Spring Data Elasticsearch, Nori analyzer |
| API Docs | SpringDoc OpenAPI, Swagger UI |
| Mail | Spring Mail, Gmail SMTP |
| External API | OpenAI API, YouTube oEmbed |
| Build / Infra | Gradle, Docker Compose |

## 핵심 기능

### 1. 인증

- 이메일 회원가입과 로그인
- 이메일 인증 코드 발송 및 검증
- 비밀번호 재설정 이메일 인증
- JWT access token, refresh token 발급
- refresh token 기반 토큰 재발급
- 로그아웃
- 카카오 로그인
- 애플 로그인

Spring Security 설정에서 인증 없이 접근 가능한 API와 인증이 필요한 API를 분리했습니다. 인증이 필요한 요청은 `JwtFilter`를 통해 현재 사용자를 식별합니다.

### 2. 빠른 메모

빠른 메모는 사용자가 정리하기 전에 일단 저장하는 영역입니다.

- 텍스트 메모 생성
- 이미지 메모 생성
- 영상 메모 생성
- 파일 메모 생성
- 메모 북마크
- 메모 삭제
- 메모를 새 보드로 변환
- 메모를 기존 보드의 노트로 변환

메모를 보드로 변환하면 임시 저장 상태에서 정리된 콘텐츠로 넘어갑니다. 이때 첨부파일도 함께 노트에 연결될 수 있도록 메모와 노트가 같은 첨부파일 도메인을 공유합니다.

### 3. 보드와 노트

보드는 사용자가 자료를 정리하는 단위이고, 노트는 보드 안에 들어가는 개별 콘텐츠입니다.

- 보드 생성, 조회, 수정, 삭제
- 보드 북마크
- 보드 태그 관리
- 노트 생성, 수정, 삭제
- 노트 순서 이동
- 노트를 다른 보드로 이동
- 보드/메모를 섞어서 보여주는 타임라인 조회

노트에는 텍스트, URL 목록, OG 데이터, 첨부파일이 함께 저장됩니다. 보드 안의 노트는 `sortOrder`로 순서를 관리합니다.

### 4. 링크 메타데이터와 AI 요약

링크를 저장할 때 사용자가 URL만 보고 내용을 다시 추측하지 않도록 OG 데이터를 수집합니다.

- `og:title`
- `og:description`
- `og:image`
- `og:site_name`
- YouTube oEmbed 기반 제목, 채널, 썸네일
- OpenAI API 기반 한국어 요약

일반 웹 페이지는 HTML meta tag를 파싱하고, YouTube URL은 oEmbed API를 먼저 사용합니다. 이후 수집한 title, description, siteName, 본문 후보를 OpenAI API에 전달해 2~3문장 요약을 생성합니다.

### 5. 파일 업로드

이미지, 영상, 일반 파일을 분리해서 업로드합니다.

- `POST /v1/upload/image`
- `POST /v1/upload/video`
- `POST /v1/upload/file`
- 업로드 객체 목록 조회
- S3 객체 접근 URL 조회
- S3 객체 접근 URL 리다이렉트

S3 객체 key는 사용자 단위 prefix를 포함합니다.

```text
{base-prefix}/users/{userUid}/{images|videos|files}/{attachmentUid}.{extension}
```

객체를 조회하거나 삭제할 때는 현재 로그인한 사용자의 prefix와 요청 key가 일치하는지 검증합니다. key에 `..`이 포함된 경우도 차단합니다. 클라이언트가 넘긴 S3 key를 그대로 신뢰하지 않고 서버에서 소유권을 다시 확인하기 위한 처리입니다.

### 6. 검색

검색은 MySQL 테이블을 직접 검색하지 않고 Elasticsearch 문서로 분리했습니다.

- 메모 검색
- 보드 검색
- 노트 검색
- 사용자 단위 재색인
- 검색 인덱스 자동 생성
- 검색 비활성화 환경 지원

검색 기능은 `SEARCH_ENABLED`로 켜고 끌 수 있습니다. Elasticsearch가 준비되지 않은 로컬 환경에서도 서버를 실행할 수 있도록 Noop 검색 구현체를 두었습니다.

### 7. 회원 탈퇴

회원 탈퇴는 DB row 삭제만으로 끝나지 않습니다. S3에 저장된 파일과 검색 인덱스도 함께 고려해야 합니다.

현재 탈퇴 흐름은 다음과 같습니다.

1. 사용자 첨부파일의 S3 key를 먼저 수집한다.
2. 수집한 key를 `pending_s3_delete`에 등록한다.
3. pending link, memo, board 데이터를 삭제한다.
4. 검색 인덱스에서 사용자 문서를 삭제한다.
5. 삭제 완료 이벤트를 발행한다.
6. 트랜잭션 커밋 이후 후속 처리를 실행할 수 있도록 분리한다.

외부 저장소 삭제 실패가 DB 트랜잭션 전체를 망가뜨리지 않도록, 삭제 대상 수집과 실제 외부 리소스 정리 책임을 분리했습니다.

## 시스템 구조

```text
Client
  |
  | REST API
  | Authorization: Bearer {accessToken}
  v
Spring Boot API Server
  |
  |-- Auth
  |     |-- Email Login
  |     |-- Kakao Login
  |     |-- Apple Login
  |     `-- JWT
  |
  |-- Content
  |     |-- Memo
  |     |-- Board
  |     |-- Note
  |     `-- PendingLink
  |
  |-- Upload
  |     |-- AWS S3
  |     `-- Presigned URL
  |
  |-- Link Preview
  |     |-- HTML OG Parser
  |     |-- YouTube oEmbed
  |     `-- OpenAI Summary
  |
  `-- Search
        |-- Elasticsearch
        `-- Reindex Event

MySQL
AWS S3
Elasticsearch
External APIs
```

## 패키지 구조

```text
src/main/java/memme/memoryme
├── auth
│   ├── api
│   ├── application
│   ├── domain
│   ├── exception
│   └── infra
├── board
│   ├── api
│   ├── application
│   ├── domain
│   ├── exception
│   └── infra
├── memo
│   ├── api
│   ├── application
│   ├── domain
│   ├── exception
│   └── infra
├── note
│   ├── api
│   ├── application
│   ├── domain
│   ├── exception
│   └── infra
├── upload
├── og
├── search
├── pendinglink
├── timeline
├── tag
├── user
├── withdrawal
└── global
```

기능별 패키지를 기준으로 나누고, 내부에서는 다음 역할을 분리했습니다.

| 계층 | 역할 |
| --- | --- |
| `api` | Controller, Request/Response DTO, Swagger API interface |
| `application` | 유스케이스 서비스, 트랜잭션 처리, 이벤트 발행 |
| `domain` | JPA Entity, 도메인 상태 변경 메서드 |
| `infra` | Repository, 외부 저장소 접근 |
| `exception` | 기능별 ErrorCode |
| `global` | 공통 응답, 공통 예외, 보안, JWT, Swagger 설정 |

## 도메인 관계

```text
User
  | 1:N
  v
Memo
  | 1:N
  v
NoteAttachment

User
  | 1:N
  v
Board
  | 1:N
  v
Note
  | 1:N
  v
NoteAttachment

User
  | 1:N
  v
PendingLink

User
  | 1:N
  v
PendingS3Delete

SearchDocument
  |-- memo
  |-- board
  `-- note
```

`NoteAttachment`는 메모와 노트 양쪽에 연결될 수 있습니다. 메모가 보드의 노트로 변환되는 흐름이 있기 때문에, 첨부파일을 별도 도메인으로 두고 연결 대상을 바꿀 수 있도록 구성했습니다.

## 주요 API

전체 요청/응답 스키마는 Swagger UI에서 확인할 수 있습니다.

```text
http://localhost:8080/v1/docs
http://localhost:8080/swagger-ui/index.html
```

| 기능 | Method | Path |
| --- | --- | --- |
| 회원가입 | POST | `/v1/auth/register` |
| 로그인 | POST | `/v1/auth/login` |
| 토큰 재발급 | POST | `/v1/auth/refresh` |
| 로그아웃 | POST | `/v1/auth/logout` |
| 카카오 로그인 | POST | `/v1/auth/kakao` |
| 애플 로그인 | POST | `/v1/auth/apple` |
| 이메일 인증 요청 | POST | `/v1/email/request` |
| 이메일 인증 확인 | POST | `/v1/email/verify` |
| 메모 생성 | POST | `/v1/memos` |
| 이미지 메모 생성 | POST | `/v1/memos/image` |
| 영상 메모 생성 | POST | `/v1/memos/video` |
| 파일 메모 생성 | POST | `/v1/memos/file` |
| 메모 북마크 변경 | PATCH | `/v1/memos/{memoUid}/bookmark` |
| 메모를 새 보드로 변환 | POST | `/v1/memos/{memoUid}/convert/new-board` |
| 메모를 기존 보드로 변환 | POST | `/v1/memos/{memoUid}/convert/boards/{boardUid}` |
| 보드 생성 | POST | `/v1/boards` |
| 보드 조회 | GET | `/v1/boards/{boardUid}` |
| 보드 수정 | PUT | `/v1/boards/{boardUid}` |
| 보드 삭제 | DELETE | `/v1/boards/{boardUid}` |
| 노트 생성 | POST | `/v1/boards/{boardUid}/notes` |
| 노트 수정 | PUT | `/v1/boards/{boardUid}/notes/{noteUid}` |
| 노트 삭제 | DELETE | `/v1/boards/{boardUid}/notes/{noteUid}` |
| 노트 이동 | PATCH | `/v1/boards/{boardUid}/notes/move` |
| 업로드 | POST | `/v1/upload/image`, `/v1/upload/video`, `/v1/upload/file` |
| OG 조회 | GET | `/v1/og` |
| OG 요약 조회 | GET | `/v1/og/summary` |
| 검색 | GET | `/v1/search` |
| 재색인 | POST | `/v1/search/reindex` |
| 타임라인 | GET | `/v1/timeline` |
| 회원 탈퇴 | DELETE | `/v1/withdrawal` |

## 주요 구현 흐름

### 메모를 보드로 변환

```text
Client
  -> POST /v1/memos/{memoUid}/convert/new-board
  -> MemoService
  -> Memo 조회 및 사용자 소유권 확인
  -> Board 생성
  -> Note 생성
  -> Memo의 첨부파일을 Note에 연결
  -> Memo 삭제
  -> SearchReindexEvent 발행
  -> Transaction Commit
  -> Elasticsearch 재색인
```

### 파일 업로드와 조회

```text
Client
  -> POST /v1/upload/image
  -> JwtFilter에서 현재 사용자 식별
  -> 파일 타입 검증
  -> S3 key 생성
  -> S3 putObject
  -> 객체 URL, key, 원본 파일명 반환

Client
  -> GET /v1/upload/object-url?key={s3Key}
  -> 현재 사용자 prefix 검증
  -> S3 presigned URL 생성
  -> URL 반환
```

### 검색 재색인

```text
Memo / Board / Note 변경
  -> SearchReindexEvent 발행
  -> DB Transaction Commit
  -> SearchIndexEventHandler 실행
  -> 사용자 기준 기존 검색 문서 삭제
  -> Memo / Board / Note를 SearchDocument로 매핑
  -> Elasticsearch bulk index
```

### 회원 탈퇴

```text
Client
  -> DELETE /v1/withdrawal
  -> 현재 사용자 식별
  -> 첨부파일 S3 key 수집
  -> pending_s3_delete 등록
  -> pending link 삭제
  -> memo 삭제
  -> board 삭제
  -> search index 삭제
  -> user 삭제
  -> UserDataDeletedEvent 발행
```

## 설계 포인트

### 사용자 소유권 검증을 서비스 계층에 배치

메모, 보드, 노트, 첨부파일, S3 객체는 모두 사용자 UID를 기준으로 접근 범위를 확인합니다. URL path에 들어온 uid나 key를 그대로 신뢰하지 않고, 현재 인증 사용자와 실제 데이터의 `userUid`를 비교합니다.

### 검색 기능을 선택 가능한 인프라로 분리

검색은 서비스 핵심 기능이지만 로컬 개발 환경에서 항상 Elasticsearch를 띄우는 것은 부담이 있습니다. 그래서 검색 활성화 여부에 따라 실제 Elasticsearch 구현체와 Noop 구현체가 교체되도록 만들었습니다.

```text
SEARCH_ENABLED=false
  -> NoopSearchQueryService
  -> NoopSearchIndexService

SEARCH_ENABLED=true
  -> ElasticsearchSearchQueryService
  -> ElasticsearchSearchIndexService
  -> SearchIndexInitializer
```

### 트랜잭션 이후 이벤트 처리

검색 재색인과 회원 탈퇴 후속 처리는 `@TransactionalEventListener(phase = AFTER_COMMIT)`을 사용합니다. DB 변경이 확정된 뒤 외부 인프라 작업을 수행하기 위해서입니다.

### 공통 응답과 도메인별 예외 코드

모든 API 응답은 `ResponseWrapper`를 사용합니다.

```json
{
  "success": true,
  "status": 200,
  "message": "요청 성공",
  "timestamp": "2026-03-20T16:10:00",
  "data": {}
}
```

비즈니스 예외는 기능별 ErrorCode로 나누고, `GlobalExceptionHandler`에서 공통 실패 응답으로 변환합니다. 컨트롤러마다 try-catch를 반복하지 않기 위한 구조입니다.

## 로컬 실행

### Requirements

- Java 17
- MySQL 8.x
- Docker, Docker Compose
- AWS S3 bucket
- Kakao OAuth client
- Apple OAuth client
- Gmail SMTP app password
- OpenAI API key

검색 기능을 사용하지 않는다면 Elasticsearch는 실행하지 않아도 됩니다.

### 환경 변수 파일 준비

```bash
cp memme.example.yaml memme.yaml
```

`memme.yaml`에 로컬 값을 채웁니다.

```yaml
SERVER_PORT: 8080

DATABASE_USERNAME: root
DATABASE_PASSWORD: your-database-password
DATABASE_URL: jdbc:mysql://localhost:3306/memme-server?createDatabaseIfNotExist=true

JWT_SECRET: your-jwt-secret-at-least-32-bytes
JWT_REFRESH_TOKEN_EXPIRATION: 1209600000

KAKAO_CLIENT_ID: your-kakao-client-id
KAKAO_REDIRECT_URI: http://localhost:8080/login/code/kakao

GMAIL_USERNAME: your-gmail-address
GMAIL_APP_PASSWORD: your-gmail-app-password

OPENAI_API_KEY: your-openai-api-key
OPENAI_MODEL: gpt-4o-mini
OPENAI_TIMEOUT_SECONDS: 12
OPENAI_MAX_TOKENS: 500

SEARCH_ENABLED: false
SEARCH_INDEX_NAME: memme_search
SEARCH_INITIALIZE_INDEX: true
ELASTICSEARCH_URIS: http://localhost:9200

AWS_S3_BUCKET: your-s3-bucket
AWS_REGION: ap-northeast-2
AWS_S3_BASE: dev/memme
AWS_ACCESS_KEY_ID: your-aws-access-key-id
AWS_SECRET_ACCESS_KEY: your-aws-secret-access-key
```

### Elasticsearch 실행

검색 기능을 사용할 때 실행합니다.

```bash
docker compose -f docker-compose.search.yml up -d
```

Kibana까지 실행하려면 다음 명령을 사용합니다.

```bash
docker compose -f docker-compose.search.yml --profile tools up -d
```

검색을 켤 때는 `memme.yaml`에서 다음 값을 설정합니다.

```yaml
SEARCH_ENABLED: true
SEARCH_INDEX_NAME: memme_search
SEARCH_INITIALIZE_INDEX: true
ELASTICSEARCH_URIS: http://localhost:9200
```

### 서버 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=prod'
```

### 테스트

```bash
./gradlew test
```

## 설정 파일

| 파일 | 설명 |
| --- | --- |
| `memme.example.yaml` | 로컬 실행에 필요한 환경 변수 예시 |
| `memme.yaml` | 실제 로컬 환경 변수 파일. Git에 커밋하지 않음 |
| `src/main/resources/application-dev.yaml` | 개발 프로필 설정 |
| `src/main/resources/application-prod.yaml` | 운영/배포 프로필 설정 |
| `docker-compose.search.yml` | Elasticsearch, Kibana 로컬 실행 |
| `src/main/resources/search/memme_search_index.json` | Elasticsearch index mapping |

## 보안과 운영 고려사항

- JWT secret, OAuth secret, AWS key, Gmail app password, OpenAI API key는 저장소에 커밋하지 않습니다.
- S3 객체 접근은 presigned URL을 사용하고, 발급 전에 사용자 prefix를 검증합니다.
- 운영 환경에서는 Swagger 접근 범위를 제한하는 것이 좋습니다.
- 검색 기능은 외부 인프라 의존성이 있으므로 `SEARCH_ENABLED`로 장애 범위를 줄일 수 있게 했습니다.
- 회원 탈퇴 시 외부 저장소 삭제 대상은 별도 테이블에 남겨 추적할 수 있도록 했습니다.


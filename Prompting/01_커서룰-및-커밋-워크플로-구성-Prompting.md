# 코스베이스 및 README 참조 커서룰 작성

_Exported on 5/19/2026 at 12:00:00 GMT+9 from Cursor (unknown)_

**User**

코스베이스 및 @README.md 파일을 참조하여 커서룰 파일을 적절하게 만들어보자

---

**Cursor**

코드베이스와 README를 분석한 뒤 Cursor 규칙 파일을 작성했습니다.

## 생성된 규칙

| 파일 | 적용 범위 | 역할 |
|------|-----------|------|
| `shealth-project.mdc` | **항상** (`alwaysApply: true`) | 프로젝트 목적, BMI 도메인 규칙, 데이터 형식, 실습 4단계 기능, 빌드 정보 |
| `java-refactoring.mdc` | `src/main/java/**/*.java` | 코드 스멜 제거, SRP 분리, 네이밍·상수화 방향 |
| `java-tests.mdc` | `src/test/**/*.java` | JUnit 5 TC 항목(BMI·보정·분류·예외), fixture·실행 방법 |

## 규칙에 반영한 핵심 내용

**도메인 (README)**  
- BMI 공식, 18.5 / 23 / 25 경계  
- `weight == 0` → 동일 연령대(20·30·…·70대) 평균 보정  
- 향후: `height == 0` 보정, 정상 BMI 사용자 목록, 전체 대비 비율  

**코드베이스 현황**  
- 10,000 크기 배열, `type` 100/200/300/400, 연령대별 중복 필드 등 개선 포인트를 리팩토링 규칙에 명시  
- `SHealthBMITest`의 `fail()` 플레이스홀더를 실제 TC로 교체하도록 테스트 규칙에 포함  

**실습 흐름**  
- README Activities 순서(분석 → 리팩토링 → 테스트 → 기능)를 AI가 따르도록 프로젝트 규칙에 기재  

Cursor에서 해당 Java 파일을 열면 `java-refactoring` / `java-tests`가 자동으로 붙고, 대화 전반에는 `shealth-project`가 항상 적용됩니다.

추가로 `shealth.dat` 전용 규칙이나 `pom.xml` 의존성 규칙이 필요하면 말씀해 주세요.

---

**User**

## 커밋 요청 시 (사전 태스크 → Git)

사용자가 **커밋해줘 / commit** 등으로 명시적으로 요청한 경우에만 Git 커밋을 수행합니다.
요청이 없으면 커밋하지 않습니다. **반드시 아래 1 → 2 → 3 순서**로 진행합니다.

폴더 (프로젝트 루트, 대소문자 고정): `Report/`, `Prompting/` — 없으면 생성합니다.

(... 커밋 워크플로 전문 및 응답·코드 제안 시 규칙 ...)

위 내용은 내가 이전에 다른 프로젝트를 하면서 .cursorrules 파일에 넣었던 내용인데 이건 커서의 agent 파일로 만드는 것은 어떨까?

---

**Cursor**

**결론: `.cursorrules` 한 덩어리보다 `.cursor/rules/*.mdc` + (선택) `AGENTS.md`가 더 낫습니다.** 이미 그 방향으로 반영해 두었습니다.

## 왜 Agent 규칙(.mdc)이 나은가

| 방식 | 장점 | 단점 |
|------|------|------|
| **`.cursorrules`** (레거시) | 한 파일에 모두 적기 쉬움 | 커밋·Java·도메인이 섞여 매 턴 토큰 소모, 규칙 on/off 어려움 |
| **`.cursor/rules/*.mdc`** | 관심사별 분리, `globs`/`alwaysApply` 제어, Cursor UI에서 규칙 선택 가능 | 파일이 여러 개 |
| **`AGENTS.md`** | 사람이 읽기 좋은 인덱스·온보딩 | Agent가 자동으로 전부 로드하지는 않음 → **규칙 본문은 `.mdc`에** |

커밋 워크플로는 **평소에는 필요 없고**, “커밋해줘”일 때만 필요하므로 `alwaysApply: false`인 전용 규칙이 적합합니다.

## 이번에 추가·정리한 것

1. **`.cursor/rules/commit-workflow.mdc`** — 이전 `.cursorrules`의 커밋 절차
2. **`.cursor/rules/response-style.mdc`** — “응답·코드 제안 시” 블록 분리
3. **`AGENTS.md`** — 규칙 목록과 “커밋 시 `commit-workflow` 필수” 안내

---

**User**

agent 폴더를 만들어서 넣어야 인식되는 거 아니야?

---

**Cursor**

**아니요. “인식”하려는 대상마다 폴더가 다릅니다.** `agent` 폴더는 **서브에이전트**용이고, 지금 만든 **커밋 규칙·슬래시 명령**과는 별개입니다.

## 무엇을 쓰려는지에 따라

| 하고 싶은 것 | 넣는 위치 | 인식 방식 |
|-------------|-----------|-----------|
| 채팅에서 **`/commit` 같은 슬래시** | `.cursor/commands/commit.md` | `/` 메뉴에 표시 |
| Agent가 **항상/가끔 따라야 할 규칙** | `.cursor/rules/commit-workflow.mdc` | 규칙으로 적용·`@commit-workflow` |
| **전용 서브에이전트** | `.cursor/agents/이름.md` | Task/위임용 |
| 짧은 프로젝트 안내 | `AGENTS.md` | 슬래시 아님 |

**`agent` 폴더만 만들었다고** 슬래시로 뜨지는 않습니다.

---

**User**

그럼 command 로 만들어줘봐

---

**Cursor**

`.cursor/commands/commit.md`를 추가했습니다.

## 사용 방법

1. Agent 채팅 입력창에 **`/`** 입력  
2. 목록에서 **`commit`** 선택  
3. 실행하면 Report → Prompting → Git 순서로 진행합니다.

---

**User**

해당 명령 테스트를 해볼 수 있어?

---

**Cursor**

`/commit` 워크플로를 실제로 실행해 테스트합니다. Report 번호 확인 후 1→2→3 순서로 진행합니다.

(이후 Report·Prompting 작성 및 Git 커밋 수행)

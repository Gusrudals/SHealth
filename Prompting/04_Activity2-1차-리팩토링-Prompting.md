# Activity 2 1차 리팩토링 PCTF 실행 및 커밋

_Exported on 5/20/2026 at 16:00:00 GMT+9 from Cursor (unknown)_

**User**

현재 코드베이스를 클린코드의 원칙에 따라, 리펙토링을 하고 싶은데. 어떻게 프롬프팅을 해야 할지 모르겠어.
현재 코드베이스의 내용을 분석해서 PCTF 의 방식으로 프롬프트를 만들어줘 

---

**Cursor**

코드베이스와 `docs/code-smell-analysis.md`, 기존 `Prompting/03` 패턴을 기준으로 **Activity 2(1차 리팩토링)** 에 바로 붙여 넣을 수 있는 PCTF 프롬프트를 정리했다.

## PCTF란 (이 프로젝트에서의 쓰임)

| 구분 | 역할 |
|------|------|
| **P**ersona | AI가 어떤 전문가처럼 행동할지 |
| **C**ontext | 프로젝트·도메인·제약·현재 코드 상태 |
| **T**ask | 무엇을 할지(범위·금지·순서) |
| **F**ormat | 출력·변경 방식(한국어 설명, diff 범위, 테스트 등) |

Cursor에서는 `@Codebase`, `@docs/code-smell-analysis.md`, `@.cursorrules` 를 함께 붙이면 Context가 안정된다.

## 복사용 — Activity 2 전체 1차 리팩토링 PCTF

(Persona·Context·Task·Format 전문: God Class/Long Method/24필드/`getBmiRatio` 24분기, BMI=25 버그, 5단계 순서, Activity 4 제외 등 — 세션에서 사용자에게 제공한 전체 블록)

## 단계별로 쓰는 짧은 PCTF

전체를 한 번에 넣기 부담스러우면 Task만 1~4단계로 교체해 반복.

## 사용 팁

1. 첫 메시지: `@Codebase` + PCTF + `@docs/code-smell-analysis.md`
2. `src/main/java/**` 열면 `java-refactoring.mdc` 자동 적용
3. 3단계 이후 TC 함께 요청 권장

---

**User**

## Persona (역할)
당신은 Java 21·Maven·JUnit 5 실습용 코드를 다루는 **클린코드·리팩토링 전문가**입니다.
- README Activities **2**(1차 리팩토링) 범위에 집중합니다. Activity 4(키 보정, 정상 BMI 목록, 전체 비율 API)는 **이번 작업에서 구현하지 않습니다**.
- `docs/code-smell-analysis.md`에 정리된 스멜을 제거하되, **도메인 규칙을 README와 일치**시킵니다.
- 변경은 **작은 단계·최소 diff**로 하고, 각 단계 후 `mvn test`로 회귀를 확인합니다.
- 루트 `.cursorrules`가 `java-refactoring.mdc`보다 우선합니다(매직 넘버 상수화, public Javadoc, 메서드 **최대 20줄**, 테스트명 `should_…_when_…`).

## Context (맥락)
(... 도메인 표, 코드 스멜 요약, 권장 구조, 제약 ...)

## Task (할 일)
### 1단계 — 도메인 경계·상수·enum (기반)
### 2단계 — 네이밍·하드코드 정리
### 3단계 — 함수 추출 (Long Method 해소)
### 4단계 — 반복/중복 제거
### 5단계 — (선택) 테스트 골격

**이번 Task에서 하지 말 것**: height==0 보정, 정상 BMI 목록, 전체 비율 API, 대규모 public 클래스 SRP 분리

## Format (출력 형식)
1. 작업 전: 파일·메서드 목록
2. 변경 후: 한국어 요약, 영향, 최소 diff
3. 검증: `mvn test`
4. 다음 단계 제안

분석만 하라는 요청이 아니면 **반드시 실제 코드를 수정**합니다.

---

**Cursor**

## Activity 2 리팩토링 완료 (1~5단계)

**변경 이유:** README Activity 2와 `docs/code-smell-analysis.md` 스멜 제거, BMI=25 분류 버그 수정.

**영향:** BMI 계산·`weight==0` 보정·분류 경계·연령대 비율 API(`getBmiRatio` 호환 + `getRatio`)

### 주요 변경

- **신규** `BmiCategory`, `BmiClassifier` — 분류·BMI 계산 일원화
- **`SHealth`** — 24 필드 제거, `Map<Integer, EnumMap<BmiCategory, Double>>`, 메서드 추출, `IOException` 전파
- **`SHealthBMI`** — `AGE_DECADES` 루프, enum 기반 출력
- **테스트** 6건 (`failedTest` 제거), `impute-weight.csv` fixture

### 검증

`mvn test` → BUILD SUCCESS (6 tests)

### 다음 단계

Activity 3: 예외 TC, `shealth.dat` 통합 검증

---

**User**

/commit 변경 내용 모두 커밋해줘 

---

**Cursor**

커밋 워크플로(Report → Prompting → Git) 진행: `Report/04_Activity2-1차-리팩토링.md`, `Prompting/04_Activity2-1차-리팩토링-Prompting.md` 작성 후 스테이징·커밋.

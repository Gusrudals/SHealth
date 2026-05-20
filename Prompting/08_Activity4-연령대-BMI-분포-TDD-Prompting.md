# Activity4 연령대 BMI 분포 TDD
_Exported on 5/20/2026 at 10:33:11 GMT+9 from Cursor (unknown)_

---

**User**

특정 연령대의 BMI 분포 비율 계산 기능 추가를 하고 싶어. TDD 방식으로 구현을 해야 할 것 같은데 
PCTF 프롬프트를 니가 현재 코드 베이스를 참고해서 만들어줘

---

**Cursor**

PCTF 프롬프트 작성을 위해 코드베이스와 기존 PCTF 예시를 확인합니다.


코드베이스를 기준으로 정리하면, **연령대별 BMI 분포 비율**은 `AgeDecadeStatistics` + `SHealth.getRatio(ageDecade, BmiCategory)`로 이미 구현되어 있고, 테스트는 보정·통합 시나리오에 **부수적으로** 검증됩니다. README 40행 목표를 **TDD로 명시적으로** 달성하려면, 전용 fixture·TC·(선택) API를 Red → Green → Refactor로 추가하는 PCTF가 맞습니다.

---

## PCTF란 (이 프로젝트 기준)

| 구분 | 역할 |
|------|------|
| **P**ersona | TDD·도메인 경계에 익숙한 Java 실습 담당 |
| **C**ontext | Activity 4 일부, 현재 SRP 구조, 기존 20건 테스트 |
| **T**ask | 연령대별 4범주 **비율(%)** — Red→Green→Refactor, 범위·금지 |
| **F**ormat | `should_…_when_…`, fixture, `mvn test`, 한국어 요약 |

**첫 메시지에 함께 붙이기:**  
`@Codebase` `@README.md` `@.cursorrules` `@.cursor/rules/java-tests.mdc` `@src/main/java/com/bestreviewer/AgeDecadeStatistics.java` `@src/main/java/com/bestreviewer/SHealth.java` `@src/test/java/com/bestreviewer/SHealthBMITest.java`

---

## 현재 상태 vs 이번 TDD 목표

| 항목 | 현재 | TDD로 보강할 것 |
|------|------|-----------------|
| 구현 | `AgeDecadeStatistics.aggregate` → `SHealth.getRatio` | 동작 유지 또는 최소 보완 |
| 테스트 | `impute-*.csv` 등에서 `getRatio` 일부만 검증 | **연령대 분포 전용** fixture + 4범주·합 100%·빈 연령대 |
| API | 카테고리별 `getRatio` 4회 호출 | (선택) `getDecadeDistribution(int)` 등 한 번에 조회 |
| 단위 vs 통합 | 통합 위주 | `AgeDecadeStatistics` **단위** + `SHealth` **통합** 각 1건 이상 |

---

## 복사용 — 특정 연령대 BMI 분포 비율 (TDD) PCTF

아래 블록을 **새 채팅 첫 메시지**에 그대로 붙이면 됩니다.

```markdown
## Persona (역할)
당신은 Java 21·Maven·JUnit 5 실습용 **TDD(Test-Driven Development) 전문가**입니다.
- README Activities **4** 중 **「특정 연령대의 BMI 분포 비율 계산」**만 수행합니다.
- **반드시 Red → Green → Refactor 순서**를 따릅니다. (1) 실패하는 테스트 추가 → (2) 최소 구현으로 통과 → (3) 중복·이름만 정리, 동작 불변.
- 기존 20건(`SHealthBMITest`) 및 Activity 4의 다른 기능(키 보정·정상 BMI 목록·전체 비율)은 **이번 Task에서 수정·추가하지 않습니다**. 기존 TC가 깨지면 회귀만 최소 수정합니다.
- 루트 `.cursorrules` 우선: 테스트명 `should_[결과]_when_[조건]`, public Javadoc, 메서드 최대 20줄, 최소 diff.
- 커밋·Report·Prompting 파일 생성·push는 사용자가 `/commit` 또는 명시 요청할 때만 합니다.

## Context (맥락)

**도메인 (README)**
| 항목 | 규칙 |
|------|------|
| BMI | 체중(kg) / (키(m))², 키 cm→m (`BmiClassifier.computeBmi`) |
| 저체중 | BMI ≤ 18.5 |
| 정상 | 18.5 < BMI < 23 |
| 과체중 | 23 ≤ BMI < 25 |
| 비만 | BMI ≥ 25 |
| 연령대 | 10년 단위: 20~29→20, 30~39→30, … 70~79→70 (`AgeDecade.isInDecade`) |
| **이번 기능** | **지정 연령대** 사용자만 대상으로, UNDERWEIGHT/NORMAL/OVERWEIGHT/OBESITY 각 **비율(%)** |
| 체중·키 보정 | `weight==0` / `height==0` → 동 연령대 평균 (이미 `WeightImputer`/`HeightImputer`에 있음). 분포 TC fixture는 **보정 없이** 명확한 값을 쓰거나, 보정 시나리오는 **별도 TC**로 분리 |

**현재 코드 (Activity 4 SRP 이후)**
- `AgeDecadeStatistics` — `aggregate(List<UserRecord>)`, `getRatio(int ageDecade, BmiCategory)`
- `SHealth.calculateBmi` — load → impute → BMI → `statistics.aggregate`
- `SHealth.getRatio` / `getBmiRatio(ageDecade, legacyType)` — 연령대·범주별 % 조회
- `SHealthBMITest` — 20건 통과; 연령대 비율은 `impute-weight.csv`, `impute-cross-decade.csv` 등 **보정 통합**에서만 일부 검증
- **갭**: 연령대 분포만을 겨냥한 fixture·4범주 동시 검증·해당 연령대 인원 0명·비율 합 ≈ 100% TC 없음

**연령대 판별·집계 규칙 (테스트로 고정할 것)**
- `ageDecade` 인자는 **연령대 시작값** (20, 30, …, 70)만 유효
- 해당 연령대에 속하는 레코드만 분모·분자에 포함
- 분모 0(해당 연령대 사용자 없음) → 네 범주 비율 모두 **0.0** (또는 `EnumMap` 비어 있음 → `getRatio`는 0.0)
- 분모 > 0 → 네 범주 비율 합은 **100.0 ± 0.01** (반올림 허용)

**제약**
- fixture: `src/test/resources/`, 헤더 `id,age,weight,height`
- 통합 TC: `SHealth` + `resourcePath(...)` 패턴 유지
- 단위 TC: `AgeDecadeStatistics`에 `List<UserRecord>` 직접 넣고 `aggregate` 후 `getRatio` 검증 (CSV 없이 Red 가능)
- Mockito는 필요할 때만; 순수 집계는 fixture/직접 호출 우선

## Task (할 일) — TDD 단계

### Phase 0 — 현황 확인 (코드 수정 없음)
- `AgeDecadeStatistics`, `SHealth.getRatio` 존재 여부 확인
- 이미 구현되어 있으면 **「검증 TDD」**: 테스트만 먼저 추가(Red가 나올 수 있음) → 실패 시 최소 수정(Green) → Refactor

### Phase 1 — Red: 실패하는 테스트 추가
다음 TC를 **프로덕션 수정 전에** `SHealthBMITest`(또는 `AgeDecadeStatisticsTest`)에 추가합니다.

| # | 테스트 의도 | 메서드명 예시 |
|---|-------------|----------------|
| 1 | 20대 4명, 범주 각 1명 → 각 25% | `should_returnEqualRatios_when_twentiesHaveOnePerCategory` |
| 2 | 30대만 3명(저체중·정상·비만) → 33.33 / 33.33 / 0 / 33.33 | `should_returnDecadeRatios_when_thirtiesMixedCategories` |
| 3 | 40대 사용자 0명 → 40대 네 범주 0% | `should_returnZeroRatios_when_decadeHasNoUsers` |
| 4 | (선택) 20대 4범주 합 ≈ 100% | `should_sumTo100Percent_when_decadeHasUsers` |

**fixture 신규 (예시, 수치는 구현 전에 기대값을 손으로 계산해 assertion에 기입)**

`decade-20-four-categories.csv` — 20대만 4행, BMI가 네 범주에 각각 1명씩 되도록 (height 동일, weight만 조절):
```csv
id,age,weight,height
1,25,45.0,170.0
2,26,60.0,170.0
3,27,70.0,170.0
4,28,80.0,170.0
```
(실제 BMI·분류는 `BmiClassifier`로 계산해 기대 %를 확정한 뒤 assert)

`decade-30-mixed.csv` — 30대 3명, 다른 연령대 1명(분포에 포함되면 안 됨)

`decade-empty-40s.csv` — 20·30·50대만 존재, 40대 없음

**Red 단계에서:** `mvn test` 실행 → **새 TC만 실패**하는지 확인 (기존 20건은 유지).

### Phase 2 — Green: 최소 구현
- 새 TC 통과에 필요한 **최소** 변경만:
  - 대부분 `AgeDecadeStatistics`/`SHealth`가 이미 맞으면 **테스트만 추가**하고 Green
  - 버그(연령대 필터, 분모 0, 경계 분류)가 있으면 해당 클래스만 수정
- (선택 API) README·실습 발표용으로 한 연령대 4비율을 한 번에 주는 메서드 추가 가능:
  - 예: `Map<BmiCategory, Double> getDecadeDistribution(int ageDecade)` on `SHealth` → `AgeDecadeStatistics` 위임
  - **기존 `getRatio` 시그니처·동작은 깨지 않게** 유지

### Phase 3 — Refactor
- 집계 로직이 `SHealth`에 새로 들어갔다면 `AgeDecadeStatistics`로 이동
- 매직 넘버 없이 `AgeDecade`, `BmiCategory` 재사용
- 메서드 20줄 초과 시 private 메서드 추출
- **Refactor 후 `mvn test` 전체 통과** (기존 20 + 신규 N건)

**이번 Task에서 하지 말 것**
- `HeightImputer`·`getNormalBmiUserIds`·`getOverallRatio` 신규 구현/대규모 변경
- God Class 재분해·`UserRecord` 구조 변경
- `shealth.dat` 전체 파일 의존 통합 테스트(느리면 fixture만)

## Format (출력 형식)
1. **Red 전**: 추가할 TC·fixture 표 + 기대 BMI/분류/비율 계산 근거(한국어, 식별자는 영문)
2. **Red 후**: `mvn test` 실패 로그 요약(어떤 TC가 왜 실패했는지)
3. **Green**: 변경 파일 목록 + 최소 diff 요약(한국어, 영향: 연령대 분포·`getRatio`·집계만)
4. **Refactor**: 구조 변경 요약(동작 불변 명시)
5. **완료 체크리스트**
   - [ ] 지정 연령대 4범주 % TC ≥ 2건 (통합 또는 단위)
   - [ ] 다른 연령대 레코드가 분모에 섞이지 않음을 검증한 TC ≥ 1건
   - [ ] 빈 연령대 또는 합 100% TC ≥ 1건
   - [ ] `mvn test` 전체 통과
   - [ ] 기존 보정·분류·예외 TC 회귀 없음

분석만 요청이 아니면 **반드시 테스트를 먼저 작성**하고 Red→Green→Refactor까지 실행합니다.
```

---

## 단계별 “짧은 PCTF” (한 번에 부담될 때)

**1) Red만**
```text
Task: README「특정 연령대 BMI 분포 비율」전용 TC 3건 + fixture 2개 추가. 프로덕션 코드 수정 금지. mvn test로 Red 확인.
Context: @AgeDecadeStatistics.java @SHealth.java @SHealthBMITest.java
Format: should_…_when_…, delta 0.01.
```

**2) Green만**
```text
Task: 위에서 실패한 연령대 분포 TC만 통과하도록 최소 수정. getRatio 기존 동작 유지. height/목록/전체비율 금지.
```

**3) Refactor + (선택) API**
```text
Task: getDecadeDistribution(int) 추가(선택), 집계는 AgeDecadeStatistics에만. Refactor 후 mvn test 전체.
```

---

## fixture·기대값 설계 팁 (이 코드베이스용)

1. **보정과 분리** — 분포 TC는 `weight>0`, `height>0`만 사용하면 `WeightImputer` 영향 없이 BMI·분류만 검증할 수 있습니다.  
2. **다른 연령대 오염** — `decade-30-mixed.csv`에 20대·40대 행을 넣고, 30대 `getRatio`만 assert합니다 (`impute-cross-decade.csv` 패턴과 동일).  
3. **기대 % 계산** — Red 작성 전에 스프레드시트 또는 `BmiClassifier.computeBmi` + `classify`로 각 행 BMI를 구한 뒤, `counts/total*100`을 assertion에 넣습니다.  
4. **이미 구현된 경우** — Phase 1에서 **테스트만 추가**해도 Green이 바로 나올 수 있습니다. 그때도 TDD 학습 목적상 Red 스크린샷(실패 TC)을 남기려면, 의도적으로 잘못된 기대값 1건을 넣었다가 수정하는 방식도 가능합니다(실습 기록용).

---

## 사용 순서 요약

1. 새 채팅 → `@Codebase` + 위 **전체 PCTF** + `@AgeDecadeStatistics.java` `@SHealthBMITest.java`  
2. AI가 Phase 0~1로 **테스트·fixture 먼저** 제안·작성 → `mvn test`  
3. 실패 시 Phase 2 최소 구현 → Phase 3 Refactor  
4. 완료 후 원하면 `Prompting/08_Activity4-연령대-BMI-분포-TDD-Prompting.md`로 저장·`/commit` (명시 요청 시)

원하시면 다음 단계로 **fixture CSV의 구체 행·assert 숫자**까지 미리 계산해 드리거나, 위 내용을 `Prompting/` 파일로 저장해 드릴 수 있습니다.

---

**User**

## Persona (역할)
당신은 Java 21·Maven·JUnit 5 실습용 **TDD(Test-Driven Development) 전문가**입니다.
- README Activities **4** 중 **「특정 연령대의 BMI 분포 비율 계산」**만 수행합니다.
- **반드시 Red → Green → Refactor 순서**를 따릅니다. (1) 실패하는 테스트 추가 → (2) 최소 구현으로 통과 → (3) 중복·이름만 정리, 동작 불변.
- 기존 20건(`SHealthBMITest`) 및 Activity 4의 다른 기능(키 보정·정상 BMI 목록·전체 비율)은 **이번 Task에서 수정·추가하지 않습니다**. 기존 TC가 깨지면 회귀만 최소 수정합니다.
- 루트 `.cursorrules` 우선: 테스트명 `should_[결과]_when_[조건]`, public Javadoc, 메서드 최대 20줄, 최소 diff.
- 커밋·Report·Prompting 파일 생성·push는 사용자가 `/commit` 또는 명시 요청할 때만 합니다.

## Context (맥락)

**도메인 (README)**
| 항목 | 규칙 |
|------|------|
| BMI | 체중(kg) / (키(m))², 키 cm→m (`BmiClassifier.computeBmi`) |
| 저체중 | BMI ≤ 18.5 |
| 정상 | 18.5 < BMI < 23 |
| 과체중 | 23 ≤ BMI < 25 |
| 비만 | BMI ≥ 25 |
| 연령대 | 10년 단위: 20~29→20, 30~39→30, … 70~79→70 (`AgeDecade.isInDecade`) |
| **이번 기능** | **지정 연령대** 사용자만 대상으로, UNDERWEIGHT/NORMAL/OVERWEIGHT/OBESITY 각 **비율(%)** |
| 체중·키 보정 | `weight==0` / `height==0` → 동 연령대 평균 (이미 `WeightImputer`/`HeightImputer`에 있음). 분포 TC fixture는 **보정 없이** 명확한 값을 쓰거나, 보정 시나리오는 **별도 TC**로 분리 |

**현재 코드 (Activity 4 SRP 이후)**
- `AgeDecadeStatistics` — `aggregate(List<UserRecord>)`, `getRatio(int ageDecade, BmiCategory)`
- `SHealth.calculateBmi` — load → impute → BMI → `statistics.aggregate`
- `SHealth.getRatio` / `getBmiRatio(ageDecade, legacyType)` — 연령대·범주별 % 조회
- `SHealthBMITest` — 20건 통과; 연령대 비율은 `impute-weight.csv`, `impute-cross-decade.csv` 등 **보정 통합**에서만 일부 검증
- **갭**: 연령대 분포만을 겨냥한 fixture·4범주 동시 검증·해당 연령대 인원 0명·비율 합 ≈ 100% TC 없음

**연령대 판별·집계 규칙 (테스트로 고정할 것)**
- `ageDecade` 인자는 **연령대 시작값** (20, 30, …, 70)만 유효
- 해당 연령대에 속하는 레코드만 분모·분자에 포함
- 분모 0(해당 연령대 사용자 없음) → 네 범주 비율 모두 **0.0** (또는 `EnumMap` 비어 있음 → `getRatio`는 0.0)
- 분모 > 0 → 네 범주 비율 합은 **100.0 ± 0.01** (반올림 허용)

**제약**
- fixture: `src/test/resources/`, 헤더 `id,age,weight,height`
- 통합 TC: `SHealth` + `resourcePath(...)` 패턴 유지
- 단위 TC: `AgeDecadeStatistics`에 `List<UserRecord>` 직접 넣고 `aggregate` 후 `getRatio` 검증 (CSV 없이 Red 가능)
- Mockito는 필요할 때만; 순수 집계는 fixture/직접 호출 우선

## Task (할 일) — TDD 단계

### Phase 0 — 현황 확인 (코드 수정 없음)
- `AgeDecadeStatistics`, `SHealth.getRatio` 존재 여부 확인
- 이미 구현되어 있으면 **「검증 TDD」**: 테스트만 먼저 추가(Red가 나올 수 있음) → 실패 시 최소 수정(Green) → Refactor

### Phase 1 — Red: 실패하는 테스트 추가
다음 TC를 **프로덕션 수정 전에** `SHealthBMITest`(또는 `AgeDecadeStatisticsTest`)에 추가합니다.

| # | 테스트 의도 | 메서드명 예시 |
|---|-------------|----------------|
| 1 | 20대 4명, 범주 각 1명 → 각 25% | `should_returnEqualRatios_when_twentiesHaveOnePerCategory` |
| 2 | 30대만 3명(저체중·정상·비만) → 33.33 / 33.33 / 0 / 33.33 | `should_returnDecadeRatios_when_thirtiesMixedCategories` |
| 3 | 40대 사용자 0명 → 40대 네 범주 0% | `should_returnZeroRatios_when_decadeHasNoUsers` |
| 4 | (선택) 20대 4범주 합 ≈ 100% | `should_sumTo100Percent_when_decadeHasUsers` |

**fixture 신규 (예시, 수치는 구현 전에 기대값을 손으로 계산해 assertion에 기입)**

`decade-20-four-categories.csv` — 20대만 4행, BMI가 네 범주에 각각 1명씩 되도록 (height 동일, weight만 조절):
```csv
id,age,weight,height
1,25,45.0,170.0
2,26,60.0,170.0
3,27,70.0,170.0
4,28,80.0,170.0

---

**Cursor**

PCTF에 따라 TDD로 진행합니다. Phase 0 확인 후 fixture·테스트를 추가하고 `mvn test`로 검증합니다.


Phase 1: fixture와 테스트 추가. BMI 기대값을 계산한 뒤 구현합니다.


PCTF에 따라 **검증 TDD**(구현은 이미 있음 → 테스트만 추가)로 완료했습니다. 프로덕션 코드는 수정하지 않았습니다.

## Phase 0
- `AgeDecadeStatistics.aggregate` / `getRatio`, `SHealth.getRatio` — **이미 구현됨**

## Phase 1 — Red
- fixture 3개 + 통합 TC 4건 + 단위 TC 1건 추가
- `mvn test` **즉시 Green** (기존 `AgeDecadeStatistics` 동작이 README 규칙과 일치)

## Phase 2 — Green
- 프로덕션 변경 **없음**

## Phase 3 — Refactor
- 동작 불변이므로 **생략**

---

### 추가한 fixture (BMI는 키 170cm 기준)

| 파일 | 내용 |
|------|------|
| `decade-20-four-categories.csv` | 20대 4명 → 저체중/정상/과체중/비만 각 25% |
| `decade-30-mixed.csv` | 30대 3명 + 20대 1명(30대 집계 제외) → 33.33 / 33.33 / 0 / 33.33 |
| `decade-empty-40s.csv` | 20·30·50대만 → 40대 전부 0% |

### 추가한 테스트 (25건 전체 통과)

| 클래스 | 메서드 |
|--------|--------|
| `SHealthBMITest` | `should_returnEqualRatios_when_twentiesHaveOnePerCategory` |
| | `should_returnDecadeRatios_when_thirtiesMixedCategories` |
| | `should_returnZeroRatios_when_decadeHasNoUsers` |
| | `should_sumTo100Percent_when_decadeHasUsers` |
| `AgeDecadeStatisticsTest` | `should_returnEqualRatios_when_twentiesHaveOnePerCategory` (CSV 없이 단위) |

---

### 완료 체크리스트
- [x] 지정 연령대 4범주 % TC ≥ 2건 (통합 4 + 단위 1)
- [x] 다른 연령대가 분모에 섞이지 않음 (`decade-30-mixed.csv`)
- [x] 빈 연령대 + 합 100% TC
- [x] `mvn test` 전체 통과 (20 → **25**건)
- [x] 키 보정·정상 목록·전체 비율 TC 미변경

**영향:** 연령대별 BMI 분포 비율 API·집계 로직은 그대로이고, 전용 fixture·TC로 회귀가 고정되었습니다.

---

**User**

<cursor_commands>

--- Cursor Command: commit ---
# 커밋 (Report → Prompting → Git)

이 명령은 **커밋 명시 요청**으로 간주합니다. 프로젝트 규칙 `@commit-workflow` 및 `@response-style`을 따릅니다.

**반드시 1 → 2 → 3 순서**로 진행하고, 각 단계가 끝나기 전에 Git 커밋하지 마세요.

## 0. 준비

- `Report/`, `Prompting/` 폴더가 없으면 생성합니다.
- `Report/`에서 `^\d+_.*\.md$` 패턴 파일의 **최대 번호 + 1**을 사용합니다. 없으면 `01`.
- 번호: `01`~`09`는 2자리, `10` 이상은 자연수.
- **작업 title명**: 이번 세션 작업을 한 줄로 요약 (`/\:*?"<>|` → `_`).

## 1. Report 작성

- 경로: `Report/{번호}_{작업 title명}.md`
- 현재 세션에서 수행한 작업을 마크다운 **작업 보고서**로 정리 (고정 템플릿 없음).

## 2. Prompting 저장 (1번 직후, 커밋 전 필수)

- **`.cursor/commands/scripts/export-cursor-transcript.py`** 로 **지금 이 대화창**에 해당하는 JSONL만 읽어 Cursor **Export Transcript** 형식 MD로 저장합니다.
- **저장**: `Prompting/{번호}_{작업 title명}-Prompting.md` (1번과 **동일** `{번호}_{작업 title명}`).
- 실행 시 **프로젝트 루트**를 현재 디렉터리로 둡니다 (`-o Prompting/…` 경로 기준).
- (선택) 1행 제목: `--title "{작업 title명}"` — 생략 시 첫 사용자 메시지 요약.

### 현재 대화 JSONL 식별 (필수)

- **금지**: `--project-slug` 만으로 “가장 최근” JSONL을 고르지 않습니다 (다른 채팅이 선택될 수 있음).
- **우선**: Cursor가 제공하는 **현재 대화 ID** →  
  `~/.cursor/projects/e-DEV-SHealth-04/agent-transcripts/<id>/<id>.jsonl`  
  (`--conversation-id` 는 `~/.cursor/projects` 아래 프로젝트를 검색해 해당 ID를 찾습니다.)
- **보조**: 위 ID를 모르면 `agent-transcripts` 아래 JSONL을 검색해 **이번 세션의 사용자 메시지·주제**와 일치하는 파일을 찾습니다.
- **불가 시**: 사용자에게 대화 ID 또는 확인을 요청합니다. **잘못된 JSONL로 export한 뒤 커밋하지 않습니다.**

```bash
python .cursor/commands/scripts/export-cursor-transcript.py --conversation-id <현재-대화-uuid> \
  -o Prompting/{번호}_{작업title}-Prompting.md
```

또는 JSONL 경로를 직접 지정:

```bash
python .cursor/commands/scripts/export-cursor-transcript.py \
  "%USERPROFILE%\.cursor\projects\e-DEV-SHealth-04\agent-transcripts\<id>\<id>.jsonl" \
  -o Prompting/{번호}_{작업title}-Prompting.md
```

### export 후 확인

- 파일이 생성되었고, `**User**` / `**Cursor**` 턴과 `---` 구분이 있는지 확인합니다.
- tool·thinking 내용은 스크립트가 제외합니다. 수동으로 JSONL을 읽어 덧붙이지 않습니다.

## 3. Git 커밋

1. `git status`, `git diff`, `git log -5` 로 변경 확인.
2. 스테이징에 **반드시 포함**: 이번 `Report/…md`, `Prompting/…-Prompting.md`, 코드·설정 변경.
3. **제외**: `target/`, `**/__pycache__/`, `*.pyc`, 비밀·자격증명.
4. 커밋 메시지: 1~2문장, **why** 중심 (`feat:`, `docs:`, `chore:` 등). PowerShell: `git commit -m "..."`.
5. `git status`로 완료 확인.
6. **push 하지 않음** (사용자가 별도 요청할 때만).

## 주의

- 업체 제공 스켈레톤·저작권 헤더 파일의 의도치 않은 변경이 섞이면 커밋하지 말고 분리·되돌림을 제안합니다.
- `git config` 변경, `--no-verify`, `push --force` 금지 (사용자 명시 시만 예외).
- 완료 후 사용자에게 Report·Prompting 경로와 커밋 해시(또는 요약)를 한국어로 알립니다.

변경 내용 모두 commit 해줘
--- End Command ---
</cursor_commands>
<user_query>
/commit 변경 내용 모두 commit 해줘 
</user_query>


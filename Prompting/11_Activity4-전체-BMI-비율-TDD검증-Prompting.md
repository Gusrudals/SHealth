# Activity4 전체 BMI 비율 TDD 검증
_Exported on 5/20/2026 at 11:18:46 GMT+9 from Cursor (unknown)_

---

**User**

전체 사용자 대비 각 BMI 범주 비율 계산 기능 추가 를 하고 싶어. TDD 방식으로 구현을 해야 할 것 같은데 
PCTF 프롬프트를 니가 현재 코드 베이스를 참고해서 만들어줘

---

**Cursor**

PCTF 프롬프트 작성을 위해 코드베이스와 기존 PCTF 예시를 확인합니다.


현재 코드베이스 기준으로 정리하면, **전체 사용자 대비 BMI 범주 비율**은 `UserQueryService.getOverallRatio` → `SHealth.getOverallRatio(BmiCategory)`로 **이미 구현**되어 있고, 통합 TC는 `overall-ratio.csv` 1건뿐입니다. README 43행을 **TDD로 명시적으로** 달성하려면, 연령대 비율(`getRatio`)과 구분되는 **전체 집계** 전용 fixture·단위 TC·합 100%·빈 데이터·보정 후 비율을 Red → Green → Refactor로 보강하는 PCTF가 맞습니다.

---

## PCTF란 (이 프로젝트 기준)

| 구분 | 역할 |
|------|------|
| **P**ersona | TDD·BMI 경계·SRP 구조에 익숙한 Java 실습 담당 |
| **C**ontext | Activity 4 일부, `UserQueryService`·`AgeDecadeStatistics`와의 차이, 기존 ~33건 테스트 |
| **T**ask | **전체** 사용자 기준 4범주 **비율(%)** — Red→Green→Refactor, 범위·금지 |
| **F**ormat | `should_…_when_…`, fixture, `mvn test`, 한국어 요약 |

**첫 메시지에 함께 붙이기:**  
`@Codebase` `@README.md` `@.cursorrules` `@.cursor/rules/java-tests.mdc` `@src/main/java/com/bestreviewer/UserQueryService.java` `@src/main/java/com/bestreviewer/AgeDecadeStatistics.java` `@src/main/java/com/bestreviewer/BmiClassifier.java` `@src/main/java/com/bestreviewer/SHealth.java` `@src/test/java/com/bestreviewer/SHealthBMITest.java` `@src/test/java/com/bestreviewer/UserQueryServiceTest.java`

---

## 현재 상태 vs 이번 TDD 목표

| 항목 | 현재 | TDD로 보강할 것 |
|------|------|-----------------|
| 구현 | `UserQueryService.getOverallRatio`, `SHealth.getOverallRatio` | 없으면 Green에서 최소 추가, 있으면 **검증 TDD** |
| 통합 TC | `should_returnOverallRatios_when_allCategoriesPresent` + `overall-ratio.csv` | 불균형 분포·합 100%·미로드·보정 후 전체 비율 |
| 단위 TC | `UserQueryServiceTest`는 **정상 BMI 목록만** | `setBmi`로 전체 비율 Red 가능 |
| 연령대 API | `getRatio(ageDecade, …)` TC 다수 | **전체 vs 연령대** 혼동 방지 TC 1건 |
| 출력 | `SHealthBMI`에 `overall - …` 출력 있음 | `main` 대규모 변경 금지 |

**연령대 비율 vs 전체 비율 (혼동 금지)**

| API | 분모 | 용도 |
|-----|------|------|
| `SHealth.getRatio(20, category)` | **해당 연령대** 사용자만 | README 40행 |
| `SHealth.getOverallRatio(category)` | **로드된 전체** 사용자 | README 43행 |

**회귀 앵커 (`overall-ratio.csv`)**

```csv
id,age,weight,height
1,25,50.0,170.0
2,30,60.0,175.0
3,40,80.0,175.0
4,50,70.0,170.0
```

| id | BMI(대략) | 범주 |
|----|-----------|------|
| 1 | ≈17.30 | UNDERWEIGHT |
| 2 | ≈19.59 | NORMAL |
| 3 | ≈26.12 | OBESITY |
| 4 | ≈24.22 | OVERWEIGHT |

- 기대: 네 범주 각 **25.0** (±0.01) — **기존 assertion 변경 금지**

---

## 복사용 — 전체 사용자 대비 BMI 범주 비율 (TDD) PCTF

아래 블록을 **새 채팅 첫 메시지**에 그대로 붙이면 됩니다.

```markdown
## Persona (역할)
당신은 Java 21·Maven·JUnit 5 실습용 **TDD(Test-Driven Development) 전문가**입니다.
- README Activities **4** 중 **「전체 사용자 대비 각 BMI 범주 비율 계산 기능 추가」**만 수행합니다.
- **반드시 Red → Green → Refactor 순서**를 따릅니다. (1) 실패하는 테스트 추가 → (2) 최소 구현으로 통과 → (3) 중복·이름만 정리, 동작 불변.
- 이번 Task에서 **수정·추가하지 않을 것**: 연령대 전용 TC 대량 추가(`getRatio` 회귀만 유지), `findNormalBmiUserIds` 동작 변경, `HeightImputer`/`WeightImputer`/`AgeDecadeStatistics` 로직 변경(보정 시나리오는 **전체 비율 결과만** 검증), 커밋·Report·Prompting·push(사용자 `/commit` 또는 명시 요청 시만).
- 기존 TC(특히 `should_returnOverallRatios_when_allCategoriesPresent`의 25.0×4)는 **값 변경 없이** 통과해야 합니다.
- 루트 `.cursorrules` 우선: 테스트명 `should_[결과]_when_[조건]`, public Javadoc, 메서드 최대 20줄, 최소 diff.

## Context (맥락)

**도메인 (README·shealth-project.mdc)**
| 항목 | 규칙 |
|------|------|
| BMI | 체중(kg) / (키(m))², 키 cm→m (`BmiClassifier.computeBmi`) |
| 저체중 | BMI ≤ 18.5 |
| 정상 | 18.5 < BMI < 23 |
| 과체중 | 23 ≤ BMI < 25 |
| 비만 | BMI ≥ 25 |
| 분류 일원화 | 집계 시 `BmiClassifier.classify(bmi)` 사용 (목록 API의 `isNormalBmi`와 동일 경계) |
| **이번 기능** | `calculateBmi` **후** 로드된 **전체** 사용자 중 각 `BmiCategory` 인원 비율을 **퍼센트(0~100)** 로 반환 |
| 체중·키 보정 | `weight==0` / `height==0` → 동 연령대 평균. 보정으로 범주가 바뀌면 **전체 비율**에 반영 — 별도 통합 TC 권장 |

**현재 코드 (Activity 4 SRP 이후)**
- `UserQueryService.getOverallRatio(List<UserRecord>, BmiCategory)` — `computeOverallRatios` → `BmiClassifier.classify`, `PERCENT_SCALE = 100.0`
- `SHealth.getOverallRatio(BmiCategory)` — `records`에 대해 위 서비스 위임 (`calculateBmi` 이후에만 의미 있음)
- `SHealth.getRatio(ageDecade, category)` — **`AgeDecadeStatistics`**, 연령대 분모 (이번 Task의 **다른 API**)
- `SHealthBMITest.should_returnOverallRatios_when_allCategoriesPresent` — `overall-ratio.csv` → 25%×4
- `UserQueryServiceTest` — 정상 BMI 경계만 있음, **전체 비율 단위 TC 없음**
- **갭**: 불균형 분포·합 100%·사용자 0명·미로드·경계 BMI·보정 후 전체 비율·연령대≠전체 대조·`UserQueryService` 단위 TC

**전체 비율 API 규칙 (테스트로 고정할 것)**
- 반환: `double` 퍼센트 (예: 25.0 = 25%). 소수 비교 `assertEquals(expected, actual, 0.01)`
- `records` 크기 0 → 모든 범주 **0.0**
- `calculateBmi` 호출 전 `SHealth` → `records` 비어 있음 → 모든 범주 **0.0**
- 분모 > 0 → 네 범주 비율 합 **100.0 ± 0.01** (반올림 허용)
- 한 범주만 인원 있으면 해당 범주 100.0, 나머지 0.0
- BMI **18.5** → UNDERWEIGHT (`classify`의 `<= 18.5`), **23.0** → OVERWEIGHT (`>= 23`이고 `< 25`)

**제약**
- fixture: `src/test/resources/`, 헤더 `id,age,weight,height`
- 통합 TC: `SHealth` + `resourcePath(...)` 패턴 유지
- 단위 TC: `UserQueryServiceTest` — `List<UserRecord>`에 `setBmi` 후 `getOverallRatio` (CSV·파이프라인 없이 Red 가능)
- Mockito 불필요

## Task (할 일) — TDD 단계

### Phase 0 — 현황 확인 (코드 수정 없음)
- `UserQueryService.getOverallRatio`, `SHealth.getOverallRatio` 존재 여부 확인
- `getRatio`와 **분모 범위**가 다름을 코드로 확인 (`AgeDecadeStatistics` vs `UserQueryService`)
- 이미 구현되어 있으면 **「검증 TDD」**: 테스트만 먼저 추가 → 실패 시 최소 수정(Green) → Refactor
- `mvn test` 기준선(전체 통과 건수) 기록

### Phase 1 — Red: 실패하는 테스트 추가
프로덕션 수정 **전에** 아래 TC를 추가합니다.

| # | 테스트 의도 | 클래스 | 메서드명 예시 |
|---|-------------|--------|----------------|
| 1 | 4범주 각 1명 → 25%×4 | `SHealthBMITest` | `should_returnOverallRatios_when_allCategoriesPresent` (**기존 유지**) |
| 2 | 불균형(예: 정상 2·비만 1) | `SHealthBMITest` | `should_returnOverallRatios_when_categoriesUneven` |
| 3 | 네 범주 합 ≈ 100% | `SHealthBMITest` | `should_sumTo100Percent_when_allUsersLoaded` |
| 4 | `calculateBmi` 전 → 전부 0 | `SHealthBMITest` | `should_returnZeroOverallRatios_when_notLoaded` |
| 5 | 빈 CSV → 전부 0 | `SHealthBMITest` | `should_returnZeroOverallRatios_when_noUsers` |
| 6 | 연령대 비율 ≠ 전체 비율 | `SHealthBMITest` | `should_differFromDecadeRatio_when_agesSpanDecades` |
| 7 | 보정 후 전체 비율 반영 | `SHealthBMITest` | `should_reflectImputedBmi_when_weightOrHeightZero` (선택, fixture 1개) |
| 8 | 경계 BMI 집계 | `UserQueryServiceTest` | `should_countUnderweightAt18_5_when_classifyingOverall` |
| 9 | 단일 범주 100% | `UserQueryServiceTest` | `should_return100PercentForOneCategory_when_allSame` |
| 10 | 빈 리스트 | `UserQueryServiceTest` | `should_returnZero_when_noRecords` |

**fixture 예시 (불균형, 손계산 후 assertion 고정)**

`overall-uneven.csv` (3명: 저체중 1, 정상 2):
```csv
id,age,weight,height
1,25,50.0,170.0
2,30,60.0,175.0
3,31,58.0,175.0
```
- id1 ≈17.30 UNDERWEIGHT, id2·3 NORMAL → UNDERWEIGHT 33.33, NORMAL 66.67, OVERWEIGHT 0, OBESITY 0 (±0.01)

**연령대 vs 전체 대조 예시**
- 20대 2명 모두 저체중, 30대 2명 모두 정상 → `getRatio(20, UNDERWEIGHT)=100`, `getOverallRatio(UNDERWEIGHT)=50` 등

### Phase 2 — Green
- 실패 TC만 통과하도록 **최소** 수정
- 구현이 이미 있으면 대부분 **프로덕션 변경 없이** Green
- `getOverallRatio`가 `getRatio`와 섞이지 않았는지 확인

### Phase 3 — Refactor
- `UserQueryService`와 `AgeDecadeStatistics`의 퍼센트 계산 중복이 있으면 **private 공통화는 선택**(동작 불변, 20줄 규칙)
- 테스트·fixture 이름 정리만; assertion 값 변경 금지

## Format (출력 형식)

각 Phase마다:
1. **Red**: 추가한 테스트·fixture 표, `mvn test` 실패/통과 요약
2. **Green**: 변경 파일·이유 1줄 + 영향(전체 비율 API, 연령대 비율·목록·보정은 불변 명시)
3. **Refactor**: diff 최소, `mvn test` 전체 통과
4. **완료 체크리스트**(한국어): README 43행, `getOverallRatio` 4범주, 단위+통합 TC, `overall-ratio.csv` 회귀 유지

분석만 요청이 아니면 **반드시 코드·테스트를 수정**하고 `mvn test`까지 실행합니다.
```

---

## 단계별 “짧은 PCTF” (한 번에 부담될 때)

**1단계만 (단위 Red)**  
> Activity 4 PCTF 중 **전체 비율 1단계만**: `UserQueryServiceTest`에 `getOverallRatio` TC 3건(빈 리스트·18.5 경계·단일 범주 100%). `SHealth`·fixture·`getRatio` 금지. `mvn test`.

**2단계만 (통합 보강)**  
> 1단계 완료 가정. **통합만**: `SHealthBMITest` + fixture(`overall-uneven.csv` 등), 합 100%·미로드·연령대≠전체. `overall-ratio.csv` 25%×4 **변경 금지**. `mvn test`.

**3단계만 (보정·Refactor)**  
> 1·2단계 완료 가정. 보정 후 `getOverallRatio` 통합 TC 1건 + Refactor(동작 불변). `mvn test`.

---

## 사용 팁

1. **검증 TDD**: 구현이 있으므로 Phase 1에서 TC를 먼저 넣고 Green이면 Refactor만 해도 README 43행 “TDD로 달성” 기록에 충분합니다.
2. **회귀 앵커**: `overall-ratio.csv`의 **25.0×4**를 PCTF에 넣어 두면 임의 변경을 줄일 수 있습니다.
3. **순수 Red 연습**: `UserQueryService.getOverallRatio` 본문을 잠시 비운 뒤 위 PCTF로 Red→Green을 밟는 변형도 가능합니다.

원하시면 위 내용을 `Prompting/11_Activity4-전체-BMI-비율-TDD-Prompting.md`로 프로젝트에 저장해 드리거나, **처음부터 구현이 없는 순수 Red 시나리오**용 PCTF 변형도 만들어 드리겠습니다.

---

**User**

## Persona (역할)
당신은 Java 21·Maven·JUnit 5 실습용 **TDD(Test-Driven Development) 전문가**입니다.
- README Activities **4** 중 **「전체 사용자 대비 각 BMI 범주 비율 계산 기능 추가」**만 수행합니다.
- **반드시 Red → Green → Refactor 순서**를 따릅니다. (1) 실패하는 테스트 추가 → (2) 최소 구현으로 통과 → (3) 중복·이름만 정리, 동작 불변.
- 이번 Task에서 **수정·추가하지 않을 것**: 연령대 전용 TC 대량 추가(`getRatio` 회귀만 유지), `findNormalBmiUserIds` 동작 변경, `HeightImputer`/`WeightImputer`/`AgeDecadeStatistics` 로직 변경(보정 시나리오는 **전체 비율 결과만** 검증), 커밋·Report·Prompting·push(사용자 `/commit` 또는 명시 요청 시만).
- 기존 TC(특히 `should_returnOverallRatios_when_allCategoriesPresent`의 25.0×4)는 **값 변경 없이** 통과해야 합니다.
- 루트 `.cursorrules` 우선: 테스트명 `should_[결과]_when_[조건]`, public Javadoc, 메서드 최대 20줄, 최소 diff.

## Context (맥락)

**도메인 (README·shealth-project.mdc)**
| 항목 | 규칙 |
|------|------|
| BMI | 체중(kg) / (키(m))², 키 cm→m (`BmiClassifier.computeBmi`) |
| 저체중 | BMI ≤ 18.5 |
| 정상 | 18.5 < BMI < 23 |
| 과체중 | 23 ≤ BMI < 25 |
| 비만 | BMI ≥ 25 |
| 분류 일원화 | 집계 시 `BmiClassifier.classify(bmi)` 사용 (목록 API의 `isNormalBmi`와 동일 경계) |
| **이번 기능** | `calculateBmi` **후** 로드된 **전체** 사용자 중 각 `BmiCategory` 인원 비율을 **퍼센트(0~100)** 로 반환 |
| 체중·키 보정 | `weight==0` / `height==0` → 동 연령대 평균. 보정으로 범주가 바뀌면 **전체 비율**에 반영 — 별도 통합 TC 권장 |

**현재 코드 (Activity 4 SRP 이후)**
- `UserQueryService.getOverallRatio(List<UserRecord>, BmiCategory)` — `computeOverallRatios` → `BmiClassifier.classify`, `PERCENT_SCALE = 100.0`
- `SHealth.getOverallRatio(BmiCategory)` — `records`에 대해 위 서비스 위임 (`calculateBmi` 이후에만 의미 있음)
- `SHealth.getRatio(ageDecade, category)` — **`AgeDecadeStatistics`**, 연령대 분모 (이번 Task의 **다른 API**)
- `SHealthBMITest.should_returnOverallRatios_when_allCategoriesPresent` — `overall-ratio.csv` → 25%×4
- `UserQueryServiceTest` — 정상 BMI 경계만 있음, **전체 비율 단위 TC 없음**
- **갭**: 불균형 분포·합 100%·사용자 0명·미로드·경계 BMI·보정 후 전체 비율·연령대≠전체 대조·`UserQueryService` 단위 TC

**전체 비율 API 규칙 (테스트로 고정할 것)**
- 반환: `double` 퍼센트 (예: 25.0 = 25%). 소수 비교 `assertEquals(expected, actual, 0.01)`
- `records` 크기 0 → 모든 범주 **0.0**
- `calculateBmi` 호출 전 `SHealth` → `records` 비어 있음 → 모든 범주 **0.0**
- 분모 > 0 → 네 범주 비율 합 **100.0 ± 0.01** (반올림 허용)
- 한 범주만 인원 있으면 해당 범주 100.0, 나머지 0.0
- BMI **18.5** → UNDERWEIGHT (`classify`의 `<= 18.5`), **23.0** → OVERWEIGHT (`>= 23`이고 `< 25`)

**제약**
- fixture: `src/test/resources/`, 헤더 `id,age,weight,height`
- 통합 TC: `SHealth` + `resourcePath(...)` 패턴 유지
- 단위 TC: `UserQueryServiceTest` — `List<UserRecord>`에 `setBmi` 후 `getOverallRatio` (CSV·파이프라인 없이 Red 가능)
- Mockito 불필요

## Task (할 일) — TDD 단계

### Phase 0 — 현황 확인 (코드 수정 없음)
- `UserQueryService.getOverallRatio`, `SHealth.getOverallRatio` 존재 여부 확인
- `getRatio`와 **분모 범위**가 다름을 코드로 확인 (`AgeDecadeStatistics` vs `UserQueryService`)
- 이미 구현되어 있으면 **「검증 TDD」**: 테스트만 먼저 추가 → 실패 시 최소 수정(Green) → Refactor
- `mvn test` 기준선(전체 통과 건수) 기록

### Phase 1 — Red: 실패하는 테스트 추가
프로덕션 수정 **전에** 아래 TC를 추가합니다.

| # | 테스트 의도 | 클래스 | 메서드명 예시 |
|---|-------------|--------|----------------|
| 1 | 4범주 각 1명 → 25%×4 | `SHealthBMITest` | `should_returnOverallRatios_when_allCategoriesPresent` (**기존 유지**) |
| 2 | 불균형(예: 정상 2·비만 1) | `SHealthBMITest` | `should_returnOverallRatios_when_categoriesUneven` |
| 3 | 네 범주 합 ≈ 100% | `SHealthBMITest` | `should_sumTo100Percent_when_allUsersLoaded` |
| 4 | `calculateBmi` 전 → 전부 0 | `SHealthBMITest` | `should_returnZeroOverallRatios_when_notLoaded` |
| 5 | 빈 CSV → 전부 0 | `SHealthBMITest` | `should_returnZeroOverallRatios_when_noUsers` |
| 6 | 연령대 비율 ≠ 전체 비율 | `SHealthBMITest` | `should_differFromDecadeRatio_when_agesSpanDecades` |
| 7 | 보정 후 전체 비율 반영 | `SHealthBMITest` | `should_reflectImputedBmi_when_weightOrHeightZero` (선택, fixture 1개) |
| 8 | 경계 BMI 집계 | `UserQueryServiceTest` | `should_countUnderweightAt18_5_when_classifyingOverall` |
| 9 | 단일 범주 100% | `UserQueryServiceTest` | `should_return100PercentForOneCategory_when_allSame` |
| 10 | 빈 리스트 | `UserQueryServiceTest` | `should_returnZero_when_noRecords` |

**fixture 예시 (불균형, 손계산 후 assertion 고정)**

`overall-uneven.csv` (3명: 저체중 1, 정상 2):
```csv
id,age,weight,height
1,25,50.0,170.0
2,30,60.0,175.0
3,31,58.0,175.0

---

**Cursor**

PCTF에 따라 검증 TDD로 진행합니다. Phase 0 현황 확인 후 테스트·fixture를 추가합니다.


Phase 1: fixture와 테스트 추가 중입니다.


검증 TDD로 **전체 사용자 대비 BMI 범주 비율** 테스트를 보강했습니다. `UserQueryService`·`SHealth.getOverallRatio`는 이미 있어 **프로덕션 코드는 변경하지 않았습니다.**

## Phase 0
- `UserQueryService.getOverallRatio` / `SHealth.getOverallRatio` 존재 확인
- 연령대: `AgeDecadeStatistics` / 전체: `UserQueryService`로 분모 분리 확인

## Phase 1–2 (Red → Green)
구현이 있어 추가 TC는 모두 **즉시 Green**입니다.

### 추가 fixture (3개)
| 파일 | 용도 |
|------|------|
| `overall-uneven.csv` | 저체중 1·정상 2 → 33.33% / 66.67% |
| `overall-decade-contrast.csv` | 20대 100% 저체중 vs 전체 50% 저체중·50% 정상 |
| `overall-after-weight-impute.csv` | 체중 보정 후 전체 100% 정상 |

### `SHealthBMITest` (+6건)
- `should_returnOverallRatios_when_categoriesUneven`
- `should_sumTo100Percent_when_allUsersLoaded`
- `should_returnZeroOverallRatios_when_notLoaded`
- `should_returnZeroOverallRatios_when_noUsers`
- `should_differFromDecadeRatio_when_agesSpanDecades`
- `should_reflectImputedBmi_when_weightOrHeightZero`

### `UserQueryServiceTest` (+3건)
- `should_returnZero_when_noRecords`
- `should_countUnderweightAt18_5_when_classifyingOverall`
- `should_return100PercentForOneCategory_when_allSame`

## Phase 3 (Refactor)
- `UserQueryServiceTest`에 `service` 필드만 정리, 동작 불변

## 검증
- `mvn test` **전체 통과**
- `should_returnOverallRatios_when_allCategoriesPresent`의 **25.0×4** 유지

## README 43행 체크리스트
- [x] 전체 사용자 기준 4범주 비율 API (`getOverallRatio`)
- [x] 통합 TC: 불균형·합 100%·미로드·빈 데이터·연령대≠전체·보정 반영
- [x] 단위 TC: 빈 리스트·경계 18.5·단일 범주 100%
- [x] `overall-ratio.csv` 회귀 유지

`/commit` 요청 시 Report·Prompting·Git까지 진행할 수 있습니다.

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

변경 내용 모두 커밋해줘

변경 내용 모두 커밋해줘

변경 사항 모두 커밋해줘
--- End Command ---
</cursor_commands>
<user_query>
/commit 변경 사항 모두 커밋해줘
</user_query>


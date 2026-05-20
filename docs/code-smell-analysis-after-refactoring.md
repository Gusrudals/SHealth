# 코드 스멜 분석 보고서 (리팩토링 이후)

| 항목 | 내용 |
|------|------|
| 프로젝트 | SHealth BMI (`e:\DEV\SHealth_04`) |
| 실습 단계 | Activities **1** (분석) — SRP 분리 **이후** 코드 |
| 분석 일자 | 2026-05-20 |
| 대상 소스 | `src/main/java/com/bestreviewer/*.java` (11개 클래스) |
| 참조 | [README.md](../README.md), `.cursorrules`, `java-refactoring.mdc` |
| 관련 문서 | [리팩토링 전 분석](./code-smell-analysis.md) |

---

## 1. 요약

Activity 4 방향의 SRP 분리 이후, **God Class·Long Method·대규모 Magic Number·BMI=25 미분류** 등 **높음** 등급 스멜은 해소되었다. `SHealth`는 로드·보정·BMI·통계·조회를 협력 객체에 위임하는 **Facade**(81줄)이며, `calculateBmi` 본문은 약 7줄이다.

**현재 1순위 개선 대상**은 **Duplicated Code**다.

- `WeightImputer` ↔ `HeightImputer` 구조 중복
- `AgeDecadeStatistics` ↔ `UserQueryService`의 `toPercentRatios` 및 집계 패턴 중복

**잔여 Magic Number**는 legacy type(100~400), `PERCENT_SCALE` 이중 정의, `AgeDecade.VALUES` 리터럴 배열 수준이다.

---

## 2. 분석 범위·도메인 기준

### 2.1 포함·제외

| 구분 | 파일 |
|------|------|
| 분석 대상 | `SHealth`, `SHealthBMI`, `BmiClassifier`, `BmiCategory`, `AgeDecade`, `AgeDecadeStatistics`, `WeightImputer`, `HeightImputer`, `CsvHealthRecordLoader`, `UserQueryService`, `UserRecord` |
| 참고 | `src/test/java/com/bestreviewer/*Test.java` |
| 제외 | `target/`, 빌드 산출물 |

### 2.2 README 도메인 규칙 (리팩토링 시 유지)

| 항목 | 규칙 |
|------|------|
| BMI | `체중(kg) / (키(m))²`, 키는 cm → m |
| 저체중 | BMI ≤ 18.5 |
| 정상체중 | 18.5 < BMI < 23 |
| 과체중 | 23 ≤ BMI < 25 |
| 비만 | BMI ≥ 25 |
| 체중 누락 | `weight == 0` → 동일 연령대(10년 단위) 평균 체중 보정 |
| 키 누락 | `height == 0` → 동일 연령대 평균 키 보정 |
| 통계 | 20·30·40·50·60·70대별 BMI 범주 **비율(%)** |

### 2.3 품질 기준 (`.cursorrules` / `java-refactoring.mdc`)

- 매직 넘버 → named constant (18.5, 23, 25 등)
- 메서드 본문 **최대 20줄**
- SRP: 로더 / 보정 / BMI 계산 / 통계 / 조회 분리

---

## 3. 코드 스멜 종합표 (심각도 내림차순)

| 심각도 | 스멜 유형 | 위치 | 설명 | 권장 조치 |
|--------|-----------|------|------|-----------|
| 중간 | Duplicated Code | `WeightImputer` ↔ `HeightImputer` | `impute` / `imputeDecade` / `averageValid*InDecade` 구조가 필드명만 다르고 동일 | `DecadeAverageImputer` 추상화 또는 공통 imputer 클래스 |
| 중간 | Duplicated Code | `AgeDecadeStatistics.toPercentRatios` ↔ `UserQueryService.toPercentRatios` | `PERCENT_SCALE`, `counts[]`→`EnumMap` 변환 로직 완전 동일 | `BmiRatioAggregator` 등 공통 유틸/클래스 추출 |
| 중간 | Duplicated Code | `AgeDecadeStatistics.computeDecadeRatios` ↔ `UserQueryService.computeOverallRatios` | 연령대 필터 유무만 다르고 `classify` + `counts[]` 집계 패턴 동일 | 집계기에 decade 필터 또는 `Predicate`로 통합 |
| 낮음 | Magic Number | `BmiCategory` 7~10행 | legacy `type` 100/200/300/400 — `getBmiRatio(int,int)` 호환용 | 레거시 API 제거 시 enum만 유지 |
| 낮음 | Magic Number | `AgeDecade.VALUES` 12행 | `{20,30,…,70}` 리터럴이 `MIN_DECADE`/`MAX_DECADE`/`STEP`와 이중 정의 | `VALUES`를 루프로 생성 |
| 낮음 | Duplicated Code | `SHealthBMI` `printDecadeRatios` / `printOverallRatios` | 4개 `BmiCategory`를 `printf`에 나열하는 패턴 반복 | `BmiCategory.values()` 루프 + 출력 헬퍼 |
| 낮음 | Magic Number (잔여) | Javadoc | `SHealth`, `UserQueryService`에 18.5·23 경계 문구 — 구현은 `BmiClassifier` 상수 사용 | `{@link BmiClassifier#NORMAL_MAX}` 등으로 참조 |
| 해소됨 | God Class | ~~`SHealth` (단일 클래스)~~ | I/O·보정·BMI·통계·24필드 일체 → Facade + 협력 객체 위임 | 유지 |
| 해소됨 | Long Method | ~~`calculateBmi` (~99줄)~~ | 현재 본문 약 7줄; 메인 소스 전 메서드 20줄 이내 | — |
| 해소됨 | Duplicated Code | ~~`getBmiRatio` 24분기~~ | `AgeDecadeStatistics` + `EnumMap` 조회 | — |
| 해소됨 | Magic Number | ~~BMI 경계 리터럴~~ | `BmiClassifier.UNDERWEIGHT_MAX` 등 named constant | — |
| 해소됨 | 도메인 불일치 | ~~BMI=25 미분류~~ | `classify(25.0)` → `OBESITY` | `should_classifyAsObesity_when_bmiIs25` |

---

## 4. 스멜 유형별 상세

### 4.1 God Class — 해소됨

**이전:** `SHealth` 한 클래스에 CSV I/O·파싱·체중 보정·BMI·연령대 통계·24개 비율 필드·`getBmiRatio` 조회.

**현재 구조:**

```
SHealth (Facade)
  ├── CsvHealthRecordLoader   — CSV 로드·파싱
  ├── WeightImputer           — weight == 0 보정
  ├── HeightImputer           — height == 0 보정
  ├── BmiClassifier           — BMI 계산·분류 (static)
  ├── AgeDecadeStatistics     — 연령대별 비율
  └── UserQueryService        — 정상 BMI id, 전체 비율
```

`SHealth.calculateBmi`는 위 단계를 순서대로 호출하는 오케스트레이션만 담당한다.

---

### 4.2 Long Method — 해당 없음 (메인 소스)

| 메서드 | 클래스 | 본문 줄 수(약) | 비고 |
|--------|--------|----------------|------|
| `calculateBmi` | `SHealth` | 7 | 로드→보정→BMI→집계 |
| `parseCsvLine` | `CsvHealthRecordLoader` | 12 | 20줄 이내 |
| `averageValidWeightInDecade` | `WeightImputer` | 14 | 20줄 이내 |
| `main` | `SHealthBMI` | 7 | 출력은 private 메서드로 분리 |

`.cursorrules` 20줄 제한을 **메인 소스는 충족**한다.

---

### 4.3 Duplicated Code — 현재 1순위

#### A. Imputer 쌍 (`WeightImputer` / `HeightImputer`)

동일 패턴:

1. `for (ageDecade = MIN; ageDecade <= MAX; ageDecade += STEP)` 외곽 루프
2. 연령대 유효값 평균 계산 (`NO_DECADE_AVERAGE == -1.0`이면 스킵)
3. `AgeDecade.isInDecade` + `== MISSING_*` 조건으로 대입

차이는 `getWeight`/`setWeight` vs `getHeight`/`setHeight` 뿐이다.

#### B. 비율 변환 (`toPercentRatios`)

`AgeDecadeStatistics` 55~64행과 `UserQueryService` 53~62행이 동일:

```java
double percent = (double) counts[category.ordinal()] * PERCENT_SCALE / total;
```

`PERCENT_SCALE = 100.0`도 두 클래스에 각각 정의되어 있다.

#### C. 집계 루프

- `computeDecadeRatios`: 연령대 필터 후 `BmiClassifier.classify` → `counts[]`
- `computeOverallRatios`: 전체 레코드에 동일 집계

→ 공통 `BmiRatioAggregator` 추출 시 Activity 2 **함수 추출**과 맞는다.

#### D. `SHealthBMI` 출력

`printDecadeRatios`와 `printOverallRatios`에서 4개 카테고리를 `printf` 인자로 나열. 연령대 루프는 `SHealth.AGE_DECADES`로 이미 개선됨.

---

### 4.4 Magic Number

| 값 | 위치 | 상태 |
|----|------|------|
| `18.5`, `23`, `25` | `BmiClassifier` | ✅ `UNDERWEIGHT_MAX`, `NORMAL_MAX`, `OVERWEIGHT_MAX` |
| `100` (cm→m) | `BmiClassifier.CM_PER_M` | ✅ |
| `0.0` (누락) | `WeightImputer`, `HeightImputer` | ✅ `MISSING_WEIGHT` / `MISSING_HEIGHT` |
| `20`~`70`, `10` | `AgeDecade` | ✅ `MIN_DECADE`, `MAX_DECADE`, `STEP`, `WIDTH` |
| `100`~`400` | `BmiCategory` | ⚠️ legacy `getBmiRatio(int,int)` 호환 |
| `100.0` (%) | `AgeDecadeStatistics`, `UserQueryService` | ⚠️ `PERCENT_SCALE` 중복 |
| `{20,30,…,70}` | `AgeDecade.VALUES` | ⚠️ 상수와 이중 정의 |

---

## 5. 도메인 경계 (현재 구현)

`BmiClassifier.classify`:

| BMI | 결과 |
|-----|------|
| ≤ 18.5 | `UNDERWEIGHT` |
| < 23 | `NORMAL` |
| < 25 | `OVERWEIGHT` |
| 그 외 (≥ 25) | `OBESITY` |

**BMI = 25** → `OBESITY` (README “25 이상 비만”과 일치).  
테스트: `SHealthBMITest.should_classifyAsObesity_when_bmiIs25`.

---

## 6. 테스트 현황

| 파일 | 상태 |
|------|------|
| `SHealthBMITest.java` | BMI 계산·분류 경계·보정·예외·연령대/전체 비율 등 **다수 TC 구현** |
| `HeightImputerTest.java` | 키 보정 |
| `AgeDecadeStatisticsTest.java` | 연령대 통계 |
| `UserQueryServiceTest.java` | 정상 BMI·전체 비율 |

이전 `failedTest()` 플레이스홀더는 제거됨.

---

## 7. Activity 2 연계 — 권장 작업 순서 (잔여 스멜)

1. **`toPercentRatios` + 집계 루프 통합** — `BmiRatioAggregator` (또는 유사 이름)
2. **`WeightImputer` / `HeightImputer` 통합** — 제네릭 또는 함수형 추출
3. **`AgeDecade.VALUES` 단일 출처** — `MIN`/`MAX`/`STEP`에서 생성
4. **`SHealthBMI` 출력 루프** — 카테고리 순회 출력
5. (선택) **legacy `getBmiRatio(int,int)`** — `BmiCategory` API만 노출

---

## 8. Before / After 요약

| 항목 | 리팩토링 전 ([code-smell-analysis.md](./code-smell-analysis.md)) | 리팩토링 후 |
|------|---------------------------------------------------------------------|-------------|
| `SHealth` 줄 수 | ~208줄, God Class | ~81줄, Facade |
| `calculateBmi` | ~99줄 | ~7줄 |
| `getBmiRatio` | 24분기 if-else | `AgeDecadeStatistics.getRatio` |
| BMI 경계 | 리터럴 분산 | `BmiClassifier` 상수 |
| BMI = 25 | 미분류 버그 | `OBESITY` |
| 최우선 스멜 | God Class, Long Method | Duplicated Code (Imputer, 비율 집계) |

---

## 9. 부록 — 분석 프롬프트 (PCTF 요약)

| 구분 | 내용 |
|------|------|
| Persona | Java 클린코드·리팩토링 코드 리뷰어, README 도메인 준수 |
| Context | Maven Java 21, `src/main/java/com/bestreviewer/`, `shealth.dat` |
| Task | Magic Number, Long Method, Duplicated Code, God Class 식별 |
| Format | 심각도 내림차순 표, 한국어 설명·영문 식별자, 분석만 |

---

*본 문서는 SRP 분리 이후 코드베이스(2026-05-20) 기준이다. 리팩토링 전 상태는 [code-smell-analysis.md](./code-smell-analysis.md)를 참고한다.*

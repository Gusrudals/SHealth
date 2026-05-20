# 작업 보고서: Activity 2 1차 리팩토링

**일자**: 2026-05-20  
**브랜치**: refactoring  
**작업 번호**: 04

## 요약

README Activity 2(클린코드 1차 리팩토링) PCTF 프롬프트에 따라 `SHealth`·`SHealthBMI`를 리팩토링하고, 분류·BMI 계산을 `BmiClassifier`·`BmiCategory`로 분리했다. BMI=25 비만 분류 버그를 README 기준(`≥ 25`)으로 수정했으며, 단위 테스트 6건을 추가해 `mvn test` 통과를 확인했다.

## 수행 내용

### 1. 도메인·상수·enum (1단계)

| 항목 | 내용 |
|------|------|
| 신규 | `BmiCategory` enum (UNDERWEIGHT/NORMAL/OVERWEIGHT/OBESITY, legacy type 100~400) |
| 신규 | `BmiClassifier` — 경계 상수, `computeBmi`, `classify` |
| 수정 | 비만 조건 `> 25` → `≥ 25` (`classify` 단일 진입점) |

### 2. 네이밍·하드코드 (2단계)

- `MISSING_WEIGHT`, `PERCENT_SCALE`, `AGE_DECADES`, `MAX_RECORDS` 등 상수화
- `SHealthBMI`: `DEFAULT_DATA_FILE`, CLI 인자 지원

### 3. 함수 추출 (3단계)

`calculateBmi`를 20줄 이내 오케스트레이션 + 단계별 private 메서드로 분해:

- `loadRecordsFromCsv`
- `imputeZeroWeightsByAgeDecade` / `imputeDecade`
- `computeAllBmis`
- `aggregateRatiosByAgeDecade` / `computeDecadeRatios`

### 4. 중복 제거 (4단계)

- 24개 연령대별 비율 필드 제거 → `Map<Integer, EnumMap<BmiCategory, Double>>`
- `getBmiRatio(int, int)` 유지(legacy type 위임), `getRatio(int, BmiCategory)` 추가
- `isInAgeDecade` 추출, `SHealthBMI.main` 연령대 루프화
- `IOException` 전파 (`printStackTrace` 제거)

### 5. 테스트 (5단계)

| TC | 검증 |
|----|------|
| `should_computeExpectedBmi_when_weightAndHeightGiven` | BMI 공식 |
| `should_classifyAs*_when_*` | 경계 18.5, 23, 25 |
| `should_imputeDecadeAverageWeight_when_weightIsZero` | `impute-weight.csv` fixture |

`failedTest()` 플레이스홀더 제거.

## 검증

```bash
mvn test  # 6 tests, BUILD SUCCESS
```

## 변경 파일 (커밋 대상)

| 구분 | 파일 |
|------|------|
| 신규 | `BmiCategory.java`, `BmiClassifier.java` |
| 수정 | `SHealth.java`, `SHealthBMI.java`, `SHealthBMITest.java` |
| 신규 | `src/test/resources/impute-weight.csv` |
| 문서 | `Report/04_…`, `Prompting/04_…` |

## 제외

- `target/` (빌드 산출물)

## 다음 단계 (권장)

- Activity 3: 예외 TC(빈 파일, 잘못된 컬럼, 평균 불가 연령대)
- Activity 4: SRP 클래스 분리, `height==0` 보정, 정상 BMI 목록, 전체 비율 API

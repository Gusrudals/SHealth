# 작업 보고서: Activity 3 UnitTest 작성

**일자**: 2026-05-20  
**브랜치**: tc  
**작업 번호**: 06

## 요약

README Activity 3(단위 테스트 작성) PCTF 프롬프트에 따라 BMI·연령대 체중 보정·분류·예외 네 가지를 커버하는 테스트를 보강했다. `SHealthBMITest`는 6건에서 17건으로 늘렸고, fixture CSV 5개를 추가했다. 예외·보정 엣지 케이스를 위해 `SHealth`에 최소 방어 로직만 반영했다.

## 수행 내용

### 1. BMI 계산 TC

| TC | 검증 |
|----|------|
| `should_computeExpectedBmi_when_weightAndHeightGiven` | 70kg / 170cm → 24.22 |
| `should_computeExpectedBmi_when_anotherWeightAndHeightGiven` | 80kg / 180cm → 24.69 |
| `should_computeExpectedBmi_when_sampleDataWeightAndHeightGiven` | 53.5kg / 150.2cm → 23.71 |

### 2. 연령대 평균 체중 보정 TC

| TC | fixture | 검증 |
|----|---------|------|
| `should_imputeDecadeAverageWeight_when_weightIsZero` | `impute-weight.csv` | 20대 0→평균 보정 후 과체중 100% |
| `should_imputeOnlySameDecade_when_multipleDecadesPresent` | `impute-cross-decade.csv` | 20대만 보정, 30대 독립 |
| `should_keepZeroWeight_when_noValidWeightInDecade` | `impute-no-valid-weight.csv` | 유효 체중 없으면 보정 스킵 |

### 3. BMI 분류 TC

- 기존 경계 18.5, 18.51, 23, 25 유지
- 추가: 22.99→NORMAL, 24.99→OVERWEIGHT, 25.01→OBESITY

### 4. 예외 TC

| TC | 기대 |
|----|------|
| `should_throwIOException_when_fileDoesNotExist` | 파일 없음 |
| `should_returnZeroRecords_when_onlyHeaderPresent` | `empty-data.csv` → count 0 |
| `should_throwIOException_when_csvHasTooFewColumns` | `bad-columns.csv` |
| `should_throwNumberFormatException_when_ageIsNotNumeric` | `bad-number.csv` |

### 프로덕션 최소 수정 (`SHealth.java`)

- CSV 컬럼 4개 미만 → `IOException`
- 연령대에 유효 체중 0건 → `NO_DECADE_AVERAGE` 반환 후 보정 스킵 (NaN 방지)

## 검증

```bash
mvn test  # 17 tests, BUILD SUCCESS
```

## 변경 파일 (커밋 대상)

| 구분 | 파일 |
|------|------|
| 수정 | `SHealth.java`, `SHealthBMITest.java` |
| 신규 | `src/test/resources/impute-cross-decade.csv`, `impute-no-valid-weight.csv`, `empty-data.csv`, `bad-columns.csv`, `bad-number.csv` |
| 문서 | `Report/06_…`, `Prompting/06_…` |

## 제외

- `target/`, `__pycache__/`, `*.pyc`
- Activity 3와 무관한 `Prompting/02_…-script-export.md` (별도 세션 산출물)

## 다음 단계 (권장)

- Activity 4: SRP 분리, `height==0` 보정, 정상 BMI 목록, 전체 비율 API

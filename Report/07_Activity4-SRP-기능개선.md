# 작업 보고서: Activity 4 SRP·기능 개선

**일자**: 2026-05-20  
**브랜치**: feature  
**작업 번호**: 07

## 요약

README Activity 4 PCTF에 따라 `SHealth` God Class를 SRP 단위로 분리하고, 키 보정·정상 BMI 사용자 목록·전체 범주 비율 기능을 추가했다. 기존 17건 테스트는 회귀 없이 유지되며, 신규 3건·fixture 3개를 추가해 총 20건이 통과한다.

## 수행 내용

### 1단계 — 데이터 모델·로딩 분리

| 구분 | 내용 |
|------|------|
| 신규 | `UserRecord`, `AgeDecade`, `CsvHealthRecordLoader` |
| 변경 | `SHealth` — 고정 배열(`MAX_RECORDS`) 제거, `List<UserRecord>` + 로더 위임 |
| 유지 | `calculateBmi(String)` public 시그니처·반환값(레코드 수) |

### 2단계 — 보정·통계 책임 분리

| 구분 | 내용 |
|------|------|
| 신규 | `WeightImputer`, `AgeDecadeStatistics` |
| 변경 | `getRatio`, `getBmiRatio` → `AgeDecadeStatistics` 위임 |
| 유지 | 체중 보정: `weight==0` → 동 연령대 평균, 유효 체중 0건 연령대는 스킵 |

### 3단계 — 신규 기능·테스트

| README 항목 | 구현 | 테스트 |
|-------------|------|--------|
| 키 보정 (`height==0`) | `HeightImputer` | `impute-height.csv` |
| 연령대 BMI 분포 비율 | 기존 `getRatio` | 기존 TC 유지 |
| 정상 BMI 사용자 목록 | `getNormalBmiUserIds()` | `normal-users.csv` → id 2, 4 |
| 전체 범주별 비율 | `getOverallRatio(BmiCategory)` | `overall-ratio.csv` → 각 25% |

### 클래스 책임 (After)

| 클래스 | 책임 |
|--------|------|
| `SHealth` | 파사드·파이프라인 조립 |
| `CsvHealthRecordLoader` | CSV 파싱 |
| `WeightImputer` / `HeightImputer` | 연령대 평균 보정 |
| `AgeDecadeStatistics` | 연령대별 BMI 비율(%) |
| `UserQueryService` | 정상 id 목록·전체 비율 |
| `BmiClassifier` | BMI 계산·분류 (변경 없음) |

### `SHealthBMI`

- 연령대별 비율 출력 유지
- 정상 BMI 사용자 id·전체 비율 데모 출력 추가

## 검증

```text
mvn test → 20 tests, BUILD SUCCESS
```

## 변경 파일

**신규 (main)**  
`AgeDecade`, `UserRecord`, `CsvHealthRecordLoader`, `WeightImputer`, `HeightImputer`, `AgeDecadeStatistics`, `UserQueryService`

**수정**  
`SHealth`, `SHealthBMI`, `SHealthBMITest`

**신규 (test/resources)**  
`impute-height.csv`, `normal-users.csv`, `overall-ratio.csv`

## Activity 4 체크리스트

- [x] SRP 책임 분리
- [x] 특정 연령대 BMI 분포 비율
- [x] height==0 연령대 평균 키 보정
- [x] BMI 정상 범위 사용자 목록
- [x] 전체 사용자 대비 범주별 비율

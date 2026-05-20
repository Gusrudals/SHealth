# 작업 보고서: Activity 4 연령대 BMI 분포 TDD

**일자**: 2026-05-20  
**브랜치**: feature  
**작업 번호**: 08

## 요약

README Activity 4 중 **「특정 연령대의 BMI 분포 비율 계산」**을 TDD(검증 TDD)로 보강했다. `AgeDecadeStatistics`·`SHealth.getRatio`는 이미 구현되어 있어 프로덕션 변경 없이, 연령대 분포 전용 fixture 3개·통합 TC 4건·단위 TC 1건을 추가했다. `mvn test`는 20건에서 **25건** 전부 통과한다.

## 배경

- Activity 4 SRP 이후 연령대 비율은 `impute-*.csv` 등 보정 통합 TC에서만 부분 검증됨.
- 4범주 동시 검증, 타 연령대 제외, 빈 연령대, 비율 합 100%에 대한 전용 TC가 없었음.

## 수행 내용 (TDD 단계)

### Phase 0 — 현황

| 항목 | 결과 |
|------|------|
| `AgeDecadeStatistics.aggregate` / `getRatio` | 존재 |
| `SHealth.getRatio` | 위임 |
| 프로덕션 수정 필요 | 없음 (검증 TDD) |

### Phase 1 — Red / Green

| 구분 | 내용 |
|------|------|
| fixture | `decade-20-four-categories.csv`, `decade-30-mixed.csv`, `decade-empty-40s.csv` |
| 통합 TC | `SHealthBMITest` 4건 추가 |
| 단위 TC | `AgeDecadeStatisticsTest` 1건 (CSV 없이 `List<UserRecord>`) |
| 결과 | 추가 직후 `mvn test` Green (기존 구현이 README 규칙과 일치) |

### Phase 2·3

- Green: 프로덕션 변경 없음.
- Refactor: 생략 (동작 불변).

## 추가 테스트·fixture

| TC | 검증 |
|----|------|
| `should_returnEqualRatios_when_twentiesHaveOnePerCategory` | 20대 4범주 각 25% |
| `should_returnDecadeRatios_when_thirtiesMixedCategories` | 30대 3명 → 33.33 / 33.33 / 0 / 33.33, 20대 레코드는 30대 집계 제외 |
| `should_returnZeroRatios_when_decadeHasNoUsers` | 40대 사용자 0명 → 네 범주 0% |
| `should_sumTo100Percent_when_decadeHasUsers` | 20대 네 비율 합 ≈ 100% |
| `AgeDecadeStatisticsTest.should_returnEqualRatios_when_twentiesHaveOnePerCategory` | 집계 클래스 단위 검증 |

## 영향 범위

- **변경 없음**: BMI 계산·분류 경계·체중·키 보정·정상 목록·전체 비율 API.
- **고정됨**: 연령대별 BMI 분포 비율 회귀 (README 40행 목표).

## 검증

```bash
mvn test
```

- Tests run: 25, Failures: 0

## 산출물

| 경로 | 설명 |
|------|------|
| `Report/08_Activity4-연령대-BMI-분포-TDD.md` | 본 보고서 |
| `Prompting/08_Activity4-연령대-BMI-분포-TDD-Prompting.md` | 세션 프롬프트·대화 export |

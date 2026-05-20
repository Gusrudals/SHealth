# 작업 보고서: Activity 4 전체 BMI 범주 비율 TDD 검증

**일자**: 2026-05-20  
**브랜치**: feature  
**작업 번호**: 11

## 요약

README Activity 4 중 **「전체 사용자 대비 각 BMI 범주 비율 계산 기능 추가」**를 PCTF 기준 **검증 TDD**로 보강했다. `UserQueryService.getOverallRatio`·`SHealth.getOverallRatio(BmiCategory)`는 이미 구현되어 있어 프로덕션 변경 없이, 불균형 분포·합 100%·미로드·연령대≠전체·보정 반영 통합 TC와 `UserQueryServiceTest` 단위 TC를 추가했다. `mvn test` **42건** 전부 통과.

## 배경

- Activity 4 SRP 단계에서 전체 비율 API·`overall-ratio.csv` 통합 TC(25%×4) 1건은 이미 존재.
- 갭: 불균형 분포, 네 범주 합 100%, 사용자 0명·미로드, `getRatio`와 분모 구분, 보정 후 전체 비율, `UserQueryService` 단위 검증.

## 수행 내용 (TDD 단계)

### Phase 0 — 현황

| 항목 | 결과 |
|------|------|
| API | `SHealth.getOverallRatio` → `UserQueryService.getOverallRatio` |
| 연령대 API | `SHealth.getRatio` → `AgeDecadeStatistics` (분모: 해당 연령대만) |
| 기준선 | 구현 존재 → 검증 TDD |

### Phase 1–2 — Red / Green

| 구분 | 내용 |
|------|------|
| 통합 TC | `SHealthBMITest` 6건 추가 |
| 단위 TC | `UserQueryServiceTest` 3건 추가 |
| fixture | `overall-uneven.csv`, `overall-decade-contrast.csv`, `overall-after-weight-impute.csv` |
| 프로덕션 | 변경 없음 (추가 TC 즉시 Green) |

### Phase 3 — Refactor

- `UserQueryServiceTest`에 `service` 필드 추출 (동작 불변)

## 추가 테스트·fixture

| 테스트 | fixture / 범위 | 검증 의도 |
|--------|----------------|-----------|
| `should_returnOverallRatios_when_categoriesUneven` | `overall-uneven.csv` | 33.33% / 66.67% / 0 / 0 |
| `should_sumTo100Percent_when_allUsersLoaded` | `overall-ratio.csv` | 전체 비율 합 100% |
| `should_returnZeroOverallRatios_when_notLoaded` | 미호출 | 전 범주 0.0 |
| `should_returnZeroOverallRatios_when_noUsers` | `empty-data.csv` | 전 범주 0.0 |
| `should_differFromDecadeRatio_when_agesSpanDecades` | `overall-decade-contrast.csv` | 20대 100% 저체중 vs 전체 50% |
| `should_reflectImputedBmi_when_weightOrHeightZero` | `overall-after-weight-impute.csv` | 보정 후 전체 100% 정상 |
| `should_returnZero_when_noRecords` | 단위 | 빈 리스트 |
| `should_countUnderweightAt18_5_when_classifyingOverall` | 단위 | BMI 18.5 → 저체중 50% |
| `should_return100PercentForOneCategory_when_allSame` | 단위 | 단일 범주 100% |

## 회귀

- `should_returnOverallRatios_when_allCategoriesPresent` + `overall-ratio.csv` → **25.0×4 변경 없음**.
- `findNormalBmiUserIds`, `AgeDecadeStatistics`, `WeightImputer`/`HeightImputer` **미변경**.

## 산출물

| 경로 | 설명 |
|------|------|
| `src/test/java/com/bestreviewer/SHealthBMITest.java` | 통합 TC 6건 |
| `src/test/java/com/bestreviewer/UserQueryServiceTest.java` | 단위 TC 3건 |
| `src/test/resources/overall-*.csv` | fixture 3개 |

## 검증

```bash
mvn test
```

42건 전부 통과.

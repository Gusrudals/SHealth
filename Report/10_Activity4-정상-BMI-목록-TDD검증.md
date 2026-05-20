# 작업 보고서: Activity 4 정상 BMI 사용자 목록 TDD 검증

**일자**: 2026-05-20  
**브랜치**: feature  
**작업 번호**: 10

## 요약

README Activity 4 중 **「BMI 정상 범위 사용자 목록 조회 기능 추가」**를 PCTF 기준 **검증 TDD**로 보강했다. `UserQueryService.findNormalBmiUserIds`·`SHealth.getNormalBmiUserIds()`는 이미 구현되어 있어, 경계·빈 목록·보정 후 조회 TC와 `UserQueryServiceTest`를 추가하고 `isNormalBmi`를 `BmiClassifier`와 일치시키는 Refactor만 수행했다. 테스트는 29건에서 **33건** 전부 통과.

## 배경

- Activity 4 SRP 단계에서 정상 BMI id 목록 API·`normal-users.csv` 통합 TC 1건은 이미 존재.
- 갭: BMI 경계(18.5·23) 제외, 미로드·정상 0명, 체중 보정 후 정상 포함, `UserQueryService` 단위 검증.

## 수행 내용 (TDD 단계)

### Phase 0 — 현황

| 항목 | 결과 |
|------|------|
| API | `SHealth.getNormalBmiUserIds()` → `UserQueryService.findNormalBmiUserIds` |
| 정상 구간 | 18.5 < BMI < 23 |
| 기준선 | `mvn test` 29건 통과 |

### Phase 1 — Red / Green

| 구분 | 내용 |
|------|------|
| 단위 TC | `UserQueryServiceTest.should_excludeBoundaryBmis_when_at18_5And23` |
| 통합 TC | 미로드 빈 목록, `normal-none.csv`, `normal-after-weight-impute.csv` |
| fixture | `normal-none.csv`, `normal-after-weight-impute.csv` |
| 결과 | 기존 구현으로 추가 직후 Green |

### Phase 3 — Refactor

- `UserQueryService.isNormalBmi` → `BmiClassifier.classify(bmi) == BmiCategory.NORMAL` (경계 로직 중복 제거, 동작 동일)

## 추가 테스트·fixture

| 테스트 | fixture / 범위 | 검증 의도 |
|--------|----------------|-----------|
| `should_excludeBoundaryBmis_when_at18_5And23` | `UserQueryServiceTest` + `setBmi` | 18.5·23.0 제외, 18.51·22.99 포함 |
| `should_returnEmptyList_when_dataNotLoaded` | `SHealth` 미호출 | 빈 리스트 |
| `should_returnEmptyList_when_noNormalUsers` | `normal-none.csv` | 정상 사용자 0명 |
| `should_includeUser_when_weightImputedToNormalBmi` | `normal-after-weight-impute.csv` | `weight==0` 보정 후 id 1·2 정상 |

## 회귀

- `should_returnNormalBmiUserIds_when_mixedCategoriesPresent` + `normal-users.csv` → `List.of(2, 4)` **변경 없음**.
- 연령대 분포·전체 비율·`HeightImputer`/`WeightImputer` 로직 **미변경**.

## 산출물

| 경로 | 설명 |
|------|------|
| `src/test/java/com/bestreviewer/UserQueryServiceTest.java` | 신규 |
| `src/test/java/com/bestreviewer/SHealthBMITest.java` | 통합 TC 3건 |
| `src/test/resources/normal-none.csv` | 신규 |
| `src/test/resources/normal-after-weight-impute.csv` | 신규 |
| `src/main/java/com/bestreviewer/UserQueryService.java` | `isNormalBmi` Refactor |

## 검증

```bash
mvn test
```

33건 전부 통과.

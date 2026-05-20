# 작업 보고서: Activity 4 키(height) 보정 TDD 검증

**일자**: 2026-05-20  
**브랜치**: feature  
**작업 번호**: 09

## 요약

README Activity 4 중 **「Height가 0인 경우에 대한 평균치 보정 로직 추가」**를 PCTF 기준 **검증 TDD**로 보강했다. `HeightImputer`·`SHealth.calculateBmi` 파이프라인은 이미 구현되어 있어 프로덕션 변경 없이, 체중 보정과 대칭인 통합 TC 2건·단위 TC(`HeightImputerTest`) 2건·fixture 2개를 추가했다. `mvn test` 전체 통과.

## 배경

- `HeightImputer`와 `impute-height.csv` 통합 TC 1건은 Activity 4 SRP 단계에서 이미 존재.
- 체중 보정 대비 갭: 연령대 격리(`impute-cross-decade` 대칭), 유효 키 0건 시 스킵(`impute-no-valid-weight` 대칭), `HeightImputer` 단위 검증.

## 수행 내용 (TDD 단계)

### Phase 0 — 현황

| 항목 | 결과 |
|------|------|
| `HeightImputer` | `WeightImputer`와 동일 패턴 |
| `SHealth.calculateBmi` | load → weight → **height** → BMI → aggregate |
| 프로덕션 수정 | 불필요 |

### Phase 1 — Red / Green

| 구분 | 내용 |
|------|------|
| fixture | `impute-cross-decade-height.csv`, `impute-no-valid-height.csv` |
| 통합 TC | `SHealthBMITest` 2건 추가 |
| 단위 TC | `HeightImputerTest` 2건 |
| 결과 | 추가 직후 `mvn test` Green |

### Phase 2·3

- Green: 기존 `HeightImputer`로 통과.
- Refactor: `WeightImputer`/`HeightImputer` 공통 추상화는 범위 외로 생략.

## 추가 테스트·fixture

| 테스트 | fixture / 범위 | 검증 의도 |
|--------|----------------|-----------|
| `should_imputeOnlySameDecadeHeight_when_multipleDecadesPresent` | `impute-cross-decade-height.csv` | 20대·30대 키 평균 격리, 비율 반영 |
| `should_keepZeroHeight_when_noValidHeightInDecade` | `impute-no-valid-height.csv` | 유효 키 0건 → height 0 유지 |
| `should_setDecadeAverageHeight_when_heightIsZero` | `HeightImputerTest` | 동 연령대 평균 175.0 cm |
| `should_keepZeroHeight_when_noValidHeightInDecade` | `HeightImputerTest` | 30대 전원 height 0 유지 |

## 회귀

- `impute-weight.csv`, `impute-cross-decade.csv`, `impute-no-valid-weight.csv` 관련 assertion **변경 없음**.
- `should_imputeDecadeAverageHeight_when_heightIsZero` + `impute-height.csv` **변경 없음**.

## 산출물

| 경로 | 설명 |
|------|------|
| `src/test/java/com/bestreviewer/HeightImputerTest.java` | 신규 |
| `src/test/java/com/bestreviewer/SHealthBMITest.java` | 통합 TC 2건 |
| `src/test/resources/impute-cross-decade-height.csv` | 신규 |
| `src/test/resources/impute-no-valid-height.csv` | 신규 |

## 검증

```bash
mvn test
```

전체 테스트 통과.

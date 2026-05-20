# 코드 스멜 분석 보고서

| 항목 | 내용 |
|------|------|
| 프로젝트 | SHealth BMI (`e:\DEV\SHealth_04`) |
| 실습 단계 | README Activities **1** — 문제 코드 분석 |
| 분석 일자 | 2026-05-20 |
| 대상 소스 | `src/main/java/com/bestreviewer/SHealth.java`, `SHealthBMI.java` |
| 참조 | [README.md](../README.md), `.cursorrules`, `java-refactoring.mdc` |

---

## 1. 요약

`SHealth` 한 클래스에 **파일 I/O · CSV 파싱 · 체중 보정 · BMI 계산 · 연령대 통계 · 비율 조회**가 모두 들어 있어 **God Class**와 **Long Method**(`calculateBmi` 약 99줄)가 가장 심각하다. 이어서 `getBmiRatio`의 24분기 if-else, 연령대별 24개 필드·6회 반복 할당 등 **대규모 중복**이 리팩토링 1순위다. **Magic Number**(BMI 경계, type 코드, 배열 크기)는 분산되어 있어 상수·enum 추출이 필요하다.

**도메인 주의:** README는 비만을 **BMI ≥ 25**로 정의하나, 현재 분류 코드는 **BMI = 25**를 어느 범주에도 넣지 않는다(103행 `> 25`). 리팩토링 전 경계 TC로 고정할 것.

**테스트:** `SHealthBMITest.failedTest()`는 `fail()`만 호출 — **미구현**(Activity 3 대상).

---

## 2. 분석 범위·도메인 기준

### 2.1 포함·제외

| 구분 | 파일 |
|------|------|
| 분석 대상 | `SHealth.java`, `SHealthBMI.java` |
| 참고만 | `SHealthBMITest.java` (플레이스홀더 여부) |
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
| 통계 | 20·30·40·50·60·70대별 BMI 범주 **비율(%)** |

### 2.3 품질 기준 (`.cursorrules` / `java-refactoring.mdc`)

- 매직 넘버 → named constant (18.5, 23, 25, 100/200/300/400, 10000 등)
- 메서드 본문 **최대 20줄**
- SRP: 로더 / 보정 / BMI 계산 / 통계 분리

---

## 3. 코드 스멜 종합표 (심각도 내림차순)

| 심각도 | 스멜 유형 | 위치 | 설명 | 권장 조치 |
|--------|-----------|------|------|-----------|
| 높음 | God Class | `SHealth` 9~208행 | CSV I/O·파싱·`weight==0` 보정·BMI 계산·연령대 통계·24개 비율 필드·`getBmiRatio` 조회가 한 클래스에 집중 | `CsvLoader` / `WeightImputer` / `BmiCalculator` / `AgeGroupStatistics` 등 SRP 분리 |
| 높음 | Long Method | `SHealth.calculateBmi` 41~141행 (본문 **약 99줄**) | 4단계 파이프라인이 한 메서드에 결합; 20줄 제한 대비 약 5배 | 단계별 private 메서드 또는 협력 클래스로 추출 |
| 높음 | Duplicated Code | `SHealth.getBmiRatio` 143~194행 | `ageClass`×`type` 조합 **24개**의 거의 동일한 if-else | `Map` / `enum` + 2차원 구조·`switch`로 조회 일원화 |
| 높음 | Duplicated Code | `SHealth.calculateBmi` 108~138행 | 연령대마다 동일 4줄 비율 할당을 `a==20`…`70`으로 6회 반복 | `double[6][4]` 또는 `AgeDecade`→`BmiRatios` 맵; 필드 24개 제거 |
| 중간 | Duplicated Code | `SHealth.calculateBmi` 61~80, 88~107행 | `for (a=20; a<=70; a+=10)` 및 `ages[i]>=a && ages[i]<a+10` 반복 | `isInAgeDecade(age, decadeStart)`·연령대 순회 유틸 추출 |
| 중간 | Duplicated Code | `SHealthBMI.main` 11~16행 | 연령대별 `printf` + `getBmiRatio(…,100~400)` 6회 동일 패턴 | 연령대 배열 루프 또는 출력 전용 메서드 |
| 중간 | Magic Number | `SHealth` 11~14, 16~39행 | 배열 `10000`, `underweight20` 등 **24개** 필드에 연령·범주 하드코딩 | `AGE_DECADES`, `BmiCategory` enum, 구조화된 통계 객체 |
| 중간 | Magic Number | `SHealth.calculateBmi` 97~104행 | BMI 경계 `18.5`, `23`, `25` 리터럴 | `BMI_*` named constant + `classify(double bmi)` |
| 중간 | Magic Number | `getBmiRatio`, `SHealthBMI.main` | `type` 100/200/300/400, 연령대 20~70 | `BmiCategory` enum으로 의미 부여 |
| 중간 | Magic Number | `calculateBmi` 61, 66, 84, 88, 109행 등 | `0.0`, `100.0`(cm→m), `100`(%), `10`(연령대 폭), `20`~`70` | `MISSING_WEIGHT`, `CM_PER_M`, `PERCENT_SCALE` 등 상수화 |
| 중간 | Long Method | `SHealth.getBmiRatio` 143~194행 (본문 **약 51줄**) | 조회 전용인데 분기만 과다 | 통계 저장 구조와 맞춘 조회 API로 20줄 이내 축소 |
| 중간 | 도메인 불일치 | `SHealth.calculateBmi` 101~104행 | README 비만 **≥25** vs 코드 `>25` → **BMI=25** 미분류 | 비만 조건 `>= 25`로 통일 + 경계 TC |
| 낮음 | Magic Number | `SHealthBMI.main` 9행 | `"shealth.dat"` 경로 하드코딩 | 상수·CLI 인자·설정으로 분리 |
| 낮음 | 예외 처리 | `SHealth.calculateBmi` 56~57행 | `e.printStackTrace()`로 IOException 삼킴 | 전파 또는 명시적 처리 |
| 낮음 | 입력 처리 | `SHealth.calculateBmi` 48~49행 | `tokens.size()==0`으로 루프 종료(빈 줄 의미 모호) | 빈 줄 스킵 vs 종료 의도 명확화 |

---

## 4. 스멜 유형별 상세

### 4.1 God Class — `SHealth`

**책임이 한곳에 몰린 항목**

| 책임 | 대략적 위치 |
|------|-------------|
| CSV 파일 읽기 | `calculateBmi` 43~58행 |
| 행 파싱 (`split`) | 196~207행 |
| 체중 0 보정 | 61~80행 |
| BMI 계산 | 83~85행 |
| 연령대별 분류·비율 | 88~138행 |
| 비율 저장·조회 | 필드 16~39행, `getBmiRatio` 143~194행 |

**영향:** Activity 4(키 보정, 정상 BMI 목록, 전체 비율 등) 추가 시 변경·회귀 범위가 커짐.

**리팩토링 방향 (예시)**

```
CsvUserLoader → WeightImputer → BmiCalculator → AgeGroupStatistics
                     ↑
              SHealth (얇은 파사드 또는 main만)
```

---

### 4.2 Long Method

| 메서드 | 줄 범위 | 본문 줄 수(약) | 20줄 대비 |
|--------|---------|----------------|-----------|
| `calculateBmi` | 41~141 | 99 | 약 5배 |
| `getBmiRatio` | 143~194 | 51 | 약 2.5배 |
| `split` | 196~207 | 11 | ✅ 이내 |
| `SHealthBMI.main` | 7~17 | 11 | ✅ 이내 |

`calculateBmi` 내부 단계:

1. CSV 로드 (43~58)
2. 체중 보정 (61~80)
3. BMI 계산 (83~85)
4. 연령대 통계 (88~138)

→ 각 단계를 메서드/클래스로 분리하면 Activity 2(함수 추출)와 일치.

---

### 4.3 Duplicated Code

#### A. `getBmiRatio` — 24분기 사다리

```143:151:src/main/java/com/bestreviewer/SHealth.java
    public double getBmiRatio(int ageClass, int type) {
        if (ageClass == 20 && type == 100) {
            return underweight20;
        } else if (ageClass == 20 && type == 200) {
            return normalweight20;
        } else if (ageClass == 20 && type == 300) {
            return overweight20;
        } else if (ageClass == 20 && type == 400) {
            return obesity20;
```

6연령대 × 4범주 = 24회 동일 패턴.

#### B. 연령대별 필드 할당 (108~138행)

동일 식 4줄 × 6연령대:

```java
underweightXX = (double) underweight * 100 / sum;
// normalweight, overweight, obesity 동일
```

#### C. 연령대 루프·필터

- 보정: `for (a=20; …)` + 이중 `for (i…)` (61~80)
- 통계: 동일 외곽 루프 + `ages[i] >= a && ages[i] < a + 10` (88~107)

#### D. `SHealthBMI.main` 출력 (11~16행)

연령대만 바뀌는 `printf` 6줄.

---

### 4.4 Magic Number

| 값 | 사용처 | 의미(도메인) |
|----|--------|--------------|
| `18.5`, `23`, `25` | 97~104행 | BMI 분류 경계 |
| `100`, `200`, `300`, `400` | `getBmiRatio`, `main` | 저체중/정상/과체중/비만 (암묵적) |
| `20`, `30`, …, `70`, `+=10` | 61, 88행 등 | 연령대 시작·폭 |
| `0.0` | 66, 75행 | 누락 체중 |
| `100.0` | 84행 | cm → m |
| `100` | 109행 등 | 백분율 |
| `10000` | 11~14행 | 배열 고정 크기 |

---

## 5. 도메인 불일치 (버그 후보)

**README:** 25 이상 → 비만  
**코드 (101~104행):**

| 조건 | BMI = 25 |
|------|----------|
| `>= 23 && < 25` (과체중) | 해당 없음 (`25 < 25` 거짓) |
| `> 25` (비만) | 해당 없음 |

→ **BMI = 25** 사용자는 `sum`에는 포함되나 네 범주 카운트 어디에도 들어가지 않음. 리팩토링 시 `classify` 단일 진입점 + `should_classifyAsObesity_when_bmiIs25` TC 권장.

**BMI = 23:** `>= 23 && < 25` → 과체중 (README “23 이상 25 미만”과 일치).

**BMI = 18.5:** `<= 18.5` → 저체중 (README “18.5 이하”와 일치).

---

## 6. 테스트 현황

| 파일 | 상태 |
|------|------|
| `SHealthBMITest.java` | `failedTest()` → `fail()`만 존재, **미구현** |

Activity 3에서 필요한 TC(README 기준):

1. BMI 계산
2. `weight == 0` 연령대 평균 보정
3. 분류 경계 (18.5, 23, **25**)
4. 예외 (빈 파일, 잘못된 컬럼, 평균 불가 연령대 등)

---

## 7. Activity 2 연계 — 권장 작업 순서

1. **도메인 경계 수정·TC 초안** — BMI 25 포함 여부 확정
2. **상수·enum** — BMI 경계, `BmiCategory`, 연령대
3. **`calculateBmi` 분해** — 로드 / 보정 / 계산 / 통계
4. **24 필드 + `getBmiRatio` 사다리 제거** — 구조화된 통계 저장
5. **`SHealthBMI.main` 정리** — 출력 루프

---

## 8. 부록 — 분석에 사용한 PCTF 프롬프트 요약

| 구분 | 내용 |
|------|------|
| Persona | Java 클린코드·리팩토링 코드 리뷰어, README 도메인 준수 |
| Context | Maven Java 21, `SHealth`/`SHealthBMI`, `shealth.dat` |
| Task | Magic Number, Long Method, Duplicated Code, God Class 식별 |
| Format | 심각도 내림차순 표, 한국어 설명·영문 식별자 |

전체 프롬프트 문안은 실습 시 `Prompting/`에 별도 보관 가능.

---

*본 문서는 분석 산출물이며, 리팩토링 적용 후 Before/After 비교용으로 갱신할 수 있다.*

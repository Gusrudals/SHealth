# Activity4-SRP-기능개선
_Exported on 5/20/2026 at 10:25:33 GMT+9 from Cursor (unknown)_

---

**User**

현재 코드베이스에서 SRP에 따른 책임 분리등 리팩토링 이 필요한지 판단해줘봐

---

**Cursor**

코드베이스 구조와 책임 분포를 파악한 뒤 SRP 관점에서 판단하겠습니다.


## 판단 요약

**1·2차(네이밍·함수 추출·BMI 분리)는 상당 부분 완료됐지만, README Activity 4 기준으로는 SRP 리팩토링이 여전히 필요합니다.**  
지금 상태로도 동작·테스트는 안정적이나, **앞으로 추가할 기능(키 보정·정상 사용자 목록·전체 비율)**을 `SHealth` 한 클래스에 넣으면 책임이 더 무거워집니다.

---

## 이미 잘 분리된 부분

| 영역 | 현재 위치 | 평가 |
|------|-----------|------|
| BMI 계산·분류 | `BmiClassifier`, `BmiCategory` | 도메인 규칙이 한곳에 모여 있고 단위 테스트도 잘 됨 |
| 진입점 | `SHealthBMI` | `main`이 얇고 출력만 담당 |
| 오케스트레이션 | `SHealth.calculateBmi()` | load → impute → BMI → 통계 파이프라인이 메서드 단위로 나뉨 |

1차 리팩토링 목표(매직 넘버, `enum`, 연령대 네이밍, 메서드 추출)는 **상당히 반영**되어 있습니다.

---

## SRP 관점에서 아직 남은 문제

`SHealth`가 여전히 **다중 책임**을 한 클래스에 가집니다.

```42:48:e:\DEV\SHealth_04\src\main\java\com\bestreviewer\SHealth.java
    public int calculateBmi(String filename) throws IOException {
        count = 0;
        loadRecordsFromCsv(filename);
        imputeZeroWeightsByAgeDecade();
        computeAllBmis();
        aggregateRatiosByAgeDecade();
        return count;
```

1. **CSV I/O·파싱** — `loadRecordsFromCsv`, `parseCsvLine`
2. **체중 보정** — `imputeZeroWeightsByAgeDecade` 등
3. **BMI 일괄 계산** — `computeAllBmis` (계산 자체는 `BmiClassifier`에 위임)
4. **연령대별 통계** — `aggregateRatiosByAgeDecade`, `computeDecadeRatios`
5. **데이터 저장소** — `ages[]`, `weights[]`, `heights[]`, `bmis[]` (고정 크기 10,000)

`.cursor/rules/java-refactoring.mdc`가 지적하는 **“한 메서드/클래스에 I/O + 보정 + BMI + 통계”** 패턴은, 메서드는 쪼갰지만 **클래스 경계**는 아직 그대로입니다.

추가로:

- **병렬 배열** → `UserRecord` 같은 모델 없이 인덱스로만 묶여 있어, 키 보정·사용자 목록 API를 넣기 어렵습니다.
- **보정 로직 단위 테스트** → `BmiClassifier`는 직접 테스트하지만, 체중 보정은 `SHealth.calculateBmi` 통합 테스트에만 의존합니다.
- **키(`height == 0`) 보정·정상 사용자 목록·전체 비율**은 README Activity 4에 있으나 **미구현** → 지금 구조에 그대로 추가하면 `SHealth`가 더 비대해집니다.

---

## “지금 당장” vs “Activity 4 전에”

| 기준 | 판단 |
|------|------|
| 동작·회귀 | `mvn test` 기준으로 현재 파이프라인은 일관됨 |
| 메서드 20줄 규칙 | 대부분 메서드는 짧음 (클래스 전체 ~190줄) |
| 실습 로드맵 | README 4단계가 **명시적으로 SRP 리팩토링**을 요구 |
| 확장성 | 다음 3개 기능을 넣기 전에 분리하는 편이 비용이 낮음 |

**결론:**  
- **유지보수·실습 목표만 보면** → “반드시 지금 깨야 하는 수준”은 아님.  
- **Activity 4·클린코드 목표를 보면** → **리팩토링이 필요하고, 새 기능보다 먼저 하는 것이 맞음.**

---

## 권장 분리 방향 (과하지 않게)

`java-refactoring.mdc` 예시와 맞춘 **최소 단계** 제안:

```text
CsvUserLoader (또는 HealthRecordReader)
    → List<UserRecord>
WeightImputer (체중 0 → 연령대 평균)
HeightImputer (키 0 → 연령대 평균)   ← Activity 4
BmiClassifier (기존 유지)
AgeDecadeStatistics (연령대별 비율)
UserQueryService (정상 BMI 목록, 전체 비율)  ← Activity 4
SHealth 또는 Facade (파이프라인만 조립)
```

`SHealth`는 **파사드/오케스트레이터**로 두고, 도메인 연산은 위 컴포넌트로 옮기면 Activity 4 기능을 클래스별로 테스트하기 쉬워집니다.

---

## 우선순위 제안

1. **`UserRecord` + `List`로 저장 구조 변경** (고정 배열 제거)  
2. **로더 / 체중 보정 / 연령대 통계** 3분할 (기존 테스트 fixture로 회귀 확인)  
3. 그다음 **키 보정·목록 조회·전체 비율** API 추가  

한 번에 전부 쪼개기보다, **2번까지 한 커밋 단위로** 나누는 것이 실습·테스트 모두에 유리합니다.

---

원하시면 다음 단계로 **구체적인 클래스 스케치(메서드 시그니처)와 기존 `SHealthBMITest`를 깨지 않게 옮기는 순서**까지 짜 드리겠습니다.

---

**User**

그럼 내가 너한테 잘 요청할 수 있게 위 내용을 잘 수행할 수 있는 PCTF 프롬프트를 만들어줘

---

**Cursor**

프로젝트에 PCTF 형식 예시가 있는지 확인한 뒤, Activity 4 SRP 리팩토링에 맞는 프롬프트를 작성합니다.


## PCTF란 (이 프로젝트 기준)

| 구분 | 역할 |
|------|------|
| **P**ersona | SRP·리팩토링·도메인에 익숙한 Java 실습 담당 |
| **C**ontext | README Activity 4, 현재 `SHealth` 구조, 규칙 파일 |
| **T**ask | 단계·범위·금지 사항 |
| **F**ormat | 출력 순서, `mvn test`, 한국어 요약 |

**첫 메시지에 함께 붙이기:** `@Codebase` `@README.md` `@.cursorrules` `@.cursor/rules/java-refactoring.mdc` `@.cursor/rules/java-tests.mdc` `@src/test/java/com/bestreviewer/SHealthBMITest.java`

---

## 복사용 — Activity 4 전체 (SRP + 기능) PCTF

아래 블록을 **새 채팅 첫 메시지**에 그대로 붙이면 됩니다.

```markdown
## Persona (역할)
당신은 Java 21·Maven·JUnit 5 실습용 **클린코드·SRP 리팩토링 전문가**입니다.
- README Activities **4**(기능 개선)를 수행합니다: SRP 책임 분리 + README에 명시된 신규 기능.
- Activity 2·3에서 만든 동작(BMI 계산·체중 보정·분류 경계·연령대 비율·예외 처리)을 **깨지 않게** 유지합니다.
- 루트 `.cursorrules` 우선: 매직 넘버 상수화, public Javadoc, 메서드 **최대 20줄**, 테스트명 `should_[결과]_when_[조건]`.
- 변경은 **작은 단계·최소 diff**로 하고, 각 단계마다 `mvn test`로 회귀를 확인합니다.
- 커밋·Report·Prompting·push는 **사용자가 `/commit` 또는 명시 요청할 때만** 수행합니다.

## Context (맥락)

**도메인 (README)**
| 항목 | 규칙 |
|------|------|
| BMI | 체중(kg) / (키(m))², 키 cm→m |
| 저체중 | BMI ≤ 18.5 |
| 정상 | 18.5 < BMI < 23 |
| 과체중 | 23 ≤ BMI < 25 |
| 비만 | BMI ≥ 25 |
| 체중 누락 | weight == 0 → **동일 연령대**(10년 단위, 20~29=20대 등) 평균 체중 |
| 키 누락 | height == 0 → **동일 연령대** 평균 키 (Activity 4 신규) |
| 연령대 통계 | 20·30·40·50·60·70대별 각 BMI 범주 **비율(%)** |

**현재 코드 (Activity 2·3 이후)**
- `BmiClassifier`, `BmiCategory` — BMI 계산·분류 (이미 분리됨, 유지·재사용)
- `SHealth` — CSV 로드·파싱, 체중 보정, BMI 일괄 계산, 연령대 비율 집계, `getRatio`/`getBmiRatio` 조회
- `SHealth` 내부: 고정 배열 `MAX_RECORDS=10000`, 병렬 배열(ages/weights/heights/bmis)
- `SHealthBMI` — 얇은 `main`, 연령대별 출력
- `SHealthBMITest` — 17건 통과 기준(fixture CSV 포함)

**리팩토링 목표 구조 (`java-refactoring.mdc` 방향)**
CsvUserLoader(또는 HealthRecordReader)
  → List<UserRecord>
WeightImputer (weight==0)
HeightImputer (height==0)
BmiClassifier (기존 유지)
AgeDecadeStatistics (연령대별 비율)
UserQueryService (정상 BMI 목록, 전체 비율)
SHealth — **파사드/오케스트레이터**만 (또는 동일 public API 유지)

**제약**
- `shealth.dat` 전체를 매번 통합 테스트에 쓰지 말고, 기존·신규 **소형 fixture** 우선
- Mockito는 I/O 경계 등 필요할 때만
- `e.printStackTrace()` 금지, 예외는 호출부로 전달
- God Class를 더 키우지 말 것 — 신규 기능은 새 클래스에

## Task (할 일)

다음 **3단계 순서**로 진행하세요. 한 번에 전부 하지 말고, **단계마다 `mvn test` 통과 후** 다음 단계로 넘어갑니다.

### 1단계 — 데이터 모델·로딩 분리 (SRP 기반)
- `UserRecord`(또는 동등 모델): id, age, weight, height (+ 필요 시 bmi, category)
- 고정 크기 배열 제거 → `List<UserRecord>` 등 컬렉션
- CSV 읽기·파싱을 `SHealth` 밖 클래스로 추출 (예: `CsvHealthRecordLoader`)
- `SHealth.calculateBmi(String)` **public 시그니처·의미 유지** (기존 테스트 깨지지 않게)
- 기존 `SHealthBMITest` **전부 통과** 유지

### 2단계 — 보정·통계 책임 분리
- 체중 보정 로직 → `WeightImputer`(또는 동등): `weight==0` → 동 연령대 평균, 유효 체중 0건 연령대는 **보정 스킵**(현재 동작 유지)
- 연령대별 BMI 비율 집계 → `AgeDecadeStatistics`(또는 동등)
- `BmiClassifier`는 순수 계산·분류만 담당 (변경 최소)
- `getRatio(int ageDecade, BmiCategory)`, `getBmiRatio(int, int legacyType)` **동작·반환값 호환**

### 3단계 — Activity 4 신규 기능 + 조회 API
README 38~43행 기준으로 구현하고 테스트 추가:

1. **키 보정**: `height == 0` → 동일 연령대 평균 키 (체중 보정과 대칭)
2. **특정 연령대 BMI 분포 비율**: 기존 `getRatio`로 충분하면 문서화만, 부족하면 명시 API 정리
3. **BMI 정상 범위 사용자 목록**: `18.5 < BMI < 23` 사용자 id(또는 레코드) 목록 반환 API
4. **전체 사용자 대비 BMI 범주별 비율**: 전체 인구 기준 UNDERWEIGHT/NORMAL/OVERWEIGHT/OBESITY %

각 신규 API에 `SHealthBMITest` 또는 분리 `*Test`에 `should_…_when_…` TC 추가.
fixture는 `src/test/resources/`에 소형 CSV.

**이번 Task에서 하지 말 것**
- 외부 라이브러리 추가(pom.xml 변경) — 사용자 명시 없으면 금지
- README·도메인과 무관한 대규모 스타일 변경
- 기존 17개 테스트 삭제·약화
- Report/Prompting/Git — 사용자 `/commit` 요청 전까지

## Format (출력 형식)

각 단계마다:

1. **작업 전**: 추가·변경할 클래스·메서드·테스트 목록(표)
2. **구현**: 최소 diff, 변경 이유 1줄 + 영향(BMI·보정·분류·통계 API) 나열
3. **검증**: `mvn test` 결과(전체 통과 필수)
4. **단계 완료**: Before/After 책임 표(클래스별), 다음 단계 안내

마지막 단계 완료 시:
- README Activity 4 항목별 체크리스트(한국어)
- `SHealthBMI.main`에서 신규 기능 **데모 출력**이 필요하면 최소 추가(20줄 이내 유지)

분석만 요청이 아니면 **반드시 실제 코드를 수정**하고 `mvn test`까지 실행합니다.
```

---

## 단계별 “짧은 PCTF” (한 번에 부담될 때)

Task만 바꿔 **3번** 요청하면 됩니다.

**1단계만**
```text
@Codebase @README.md @.cursorrules @java-refactoring.mdc

위 Activity 4 PCTF 중 **1단계만** 수행: UserRecord + List, CSV 로더 분리, SHealth public API 유지, mvn test 전부 통과. 2·3단계·신규 기능 금지.
```

**2단계만**
```text
1단계 완료 상태 가정. Activity 4 PCTF **2단계만**: WeightImputer, AgeDecadeStatistics 분리, getRatio 호환, mvn test. height 보정·목록·전체 비율 금지.
```

**3단계만**
```text
1·2단계 완료 가정. Activity 4 PCTF **3단계만**: height==0 보정, 정상 BMI 사용자 목록, 전체 비율 API + TC. mvn test.
```

---

## 프롬프트 설계 팁

1. **성공 기준을 끝에 고정**  
   예: “`mvn test` 17건 이상 전부 통과 + README 4 bullet 각각 최소 1 API + 1 TC”

2. **회귀 앵커**  
   “기존 fixture(`impute-weight.csv` 등) assertion 값 변경 없이 통과”라고 쓰면 임의 동작 변경을 줄일 수 있습니다.

3. **키 보정 TC 예시를 Context에 넣기** (선택)  
   `height==0`만 있는 20대 + 유효 키 2명 → 평균 키로 대체 후 BMI/비율 검증

4. **파일 저장**  
   실습 기록용으로 `Prompting/07_Activity4-SRP-기능개선-Prompting.md`에 위 블록을 저장해 두고, 채팅에는 **짧은 PCTF + @참조**만 붙여도 됩니다.

---

원하시면 위 내용을 `Prompting/07_Activity4-SRP-기능개선-Prompting.md` 파일로 프로젝트에 만들어 드리거나, **SRP만(신규 기능 제외)** 버전 PCTF도 따로 적어 드리겠습니다.

---

**User**

## Persona (역할)
당신은 Java 21·Maven·JUnit 5 실습용 **클린코드·SRP 리팩토링 전문가**입니다.
- README Activities **4**(기능 개선)를 수행합니다: SRP 책임 분리 + README에 명시된 신규 기능.
- Activity 2·3에서 만든 동작(BMI 계산·체중 보정·분류 경계·연령대 비율·예외 처리)을 **깨지 않게** 유지합니다.
- 루트 `.cursorrules` 우선: 매직 넘버 상수화, public Javadoc, 메서드 **최대 20줄**, 테스트명 `should_[결과]_when_[조건]`.
- 변경은 **작은 단계·최소 diff**로 하고, 각 단계마다 `mvn test`로 회귀를 확인합니다.
- 커밋·Report·Prompting·push는 **사용자가 `/commit` 또는 명시 요청할 때만** 수행합니다.

## Context (맥락)

**도메인 (README)**
| 항목 | 규칙 |
|------|------|
| BMI | 체중(kg) / (키(m))², 키 cm→m |
| 저체중 | BMI ≤ 18.5 |
| 정상 | 18.5 < BMI < 23 |
| 과체중 | 23 ≤ BMI < 25 |
| 비만 | BMI ≥ 25 |
| 체중 누락 | weight == 0 → **동일 연령대**(10년 단위, 20~29=20대 등) 평균 체중 |
| 키 누락 | height == 0 → **동일 연령대** 평균 키 (Activity 4 신규) |
| 연령대 통계 | 20·30·40·50·60·70대별 각 BMI 범주 **비율(%)** |

**현재 코드 (Activity 2·3 이후)**
- `BmiClassifier`, `BmiCategory` — BMI 계산·분류 (이미 분리됨, 유지·재사용)
- `SHealth` — CSV 로드·파싱, 체중 보정, BMI 일괄 계산, 연령대 비율 집계, `getRatio`/`getBmiRatio` 조회
- `SHealth` 내부: 고정 배열 `MAX_RECORDS=10000`, 병렬 배열(ages/weights/heights/bmis)
- `SHealthBMI` — 얇은 `main`, 연령대별 출력
- `SHealthBMITest` — 17건 통과 기준(fixture CSV 포함)

**리팩토링 목표 구조 (`java-refactoring.mdc` 방향)**
CsvUserLoader(또는 HealthRecordReader)
  → List<UserRecord>
WeightImputer (weight==0)
HeightImputer (height==0)
BmiClassifier (기존 유지)
AgeDecadeStatistics (연령대별 비율)
UserQueryService (정상 BMI 목록, 전체 비율)
SHealth — **파사드/오케스트레이터**만 (또는 동일 public API 유지)

**제약**
- `shealth.dat` 전체를 매번 통합 테스트에 쓰지 말고, 기존·신규 **소형 fixture** 우선
- Mockito는 I/O 경계 등 필요할 때만
- `e.printStackTrace()` 금지, 예외는 호출부로 전달
- God Class를 더 키우지 말 것 — 신규 기능은 새 클래스에

## Task (할 일)

다음 **3단계 순서**로 진행하세요. 한 번에 전부 하지 말고, **단계마다 `mvn test` 통과 후** 다음 단계로 넘어갑니다.

### 1단계 — 데이터 모델·로딩 분리 (SRP 기반)
- `UserRecord`(또는 동등 모델): id, age, weight, height (+ 필요 시 bmi, category)
- 고정 크기 배열 제거 → `List<UserRecord>` 등 컬렉션
- CSV 읽기·파싱을 `SHealth` 밖 클래스로 추출 (예: `CsvHealthRecordLoader`)
- `SHealth.calculateBmi(String)` **public 시그니처·의미 유지** (기존 테스트 깨지지 않게)
- 기존 `SHealthBMITest` **전부 통과** 유지

### 2단계 — 보정·통계 책임 분리
- 체중 보정 로직 → `WeightImputer`(또는 동등): `weight==0` → 동 연령대 평균, 유효 체중 0건 연령대는 **보정 스킵**(현재 동작 유지)
- 연령대별 BMI 비율 집계 → `AgeDecadeStatistics`(또는 동등)
- `BmiClassifier`는 순수 계산·분류만 담당 (변경 최소)
- `getRatio(int ageDecade, BmiCategory)`, `getBmiRatio(int, int legacyType)` **동작·반환값 호환**

### 3단계 — Activity 4 신규 기능 + 조회 API
README 38~43행 기준으로 구현하고 테스트 추가:

1. **키 보정**: `height == 0` → 동일 연령대 평균 키 (체중 보정과 대칭)
2. **특정 연령대 BMI 분포 비율**: 기존 `getRatio`로 충분하면 문서화만, 부족하면 명시 API 정리
3. **BMI 정상 범위 사용자 목록**: `18.5 < BMI < 23` 사용자 id(또는 레코드) 목록 반환 API
4. **전체 사용자 대비 BMI 범주별 비율**: 전체 인구 기준 UNDERWEIGHT/NORMAL/OVERWEIGHT/OBESITY %

각 신규 API에 `SHealthBMITest` 또는 분리 `*Test`에 `should_…_when_…` TC 추가.
fixture는 `src/test/resources/`에 소형 CSV.

**이번 Task에서 하지 말 것**
- 외부 라이브러리 추가(pom.xml 변경) — 사용자 명시 없으면 금지
- README·도메인과 무관한 대규모 스타일 변경
- 기존 17개 테스트 삭제·약화
- Report/Prompting/Git — 사용자 `/commit` 요청 전까지

## Format (출력 형식)

각 단계마다:

1. **작업 전**: 추가·변경할 클래스·메서드·테스트 목록(표)
2. **구현**: 최소 diff, 변경 이유 1줄 + 영향(BMI·보정·분류·통계 API) 나열
3. **검증**: `mvn test` 결과(전체 통과 필수)
4. **단계 완료**: Before/After 책임 표(클래스별), 다음 단계 안내

마지막 단계 완료 시:
- README Activity 4 항목별 체크리스트(한국어)
- `SHealthBMI.main`에서 신규 기능 **데모 출력**이 필요하면 최소 추가(20줄 이내 유지)

분석만 요청이 아니면 **반드시 실제 코드를 수정**하고 `mvn test`까지 실행합니다.

---

**Cursor**

Activity 4 PCTF를 3단계로 진행합니다. 현재 코드와 테스트를 확인한 뒤 1단계부터 구현합니다.


1단계부터 구현합니다. 필요한 클래스를 추가하고 `SHealth`를 파사드로 리팩터링합니다.


Activity 4 PCTF를 3단계 모두 반영했습니다. `mvn test` **20건 전부 통과**했습니다.

**변경 이유:** `SHealth` God Class를 SRP 단위로 분리하고 README Activity 4 신규 기능(키 보정·정상 사용자 목록·전체 비율)을 추가했습니다.  
**영향:** BMI·체중 보정·분류 경계·연령대 비율·예외 동작은 기존 17건 유지, 파이프라인에 키 보정 단계 추가, `getNormalBmiUserIds()`·`getOverallRatio()` API 추가.

---

## 1단계 — 데이터 모델·로딩 분리

| 구분 | 내용 |
|------|------|
| 신규 | `UserRecord`, `AgeDecade`, `CsvHealthRecordLoader` |
| 변경 | `SHealth` — 고정 배열 제거, `List<UserRecord>` + 로더 위임 |
| 테스트 | 기존 17건 회귀 유지 |

---

## 2단계 — 보정·통계 책임 분리

| 구분 | 내용 |
|------|------|
| 신규 | `WeightImputer`, `AgeDecadeStatistics` |
| 변경 | `SHealth` — `getRatio` / `getBmiRatio`는 `AgeDecadeStatistics`에 위임 |
| 테스트 | 보정·비율 관련 기존 TC 동일 값으로 통과 |

---

## 3단계 — 신규 기능 + TC

| 기능 | API | 테스트 |
|------|-----|--------|
| 키 보정 (`height==0`) | `HeightImputer` + 파이프라인 | `impute-height.csv`, `should_imputeDecadeAverageHeight_…` |
| 연령대 비율 | 기존 `getRatio` 유지 | 기존 TC로 충분 |
| 정상 BMI 사용자 목록 | `getNormalBmiUserIds()` | `normal-users.csv` → id `2, 4` |
| 전체 범주 비율 | `getOverallRatio(BmiCategory)` | `overall-ratio.csv` → 각 25% |

`SHealthBMI.main`에 정상 사용자 id·전체 비율 출력을 최소 추가했습니다.

---

## Before / After 책임

| 클래스 | Before | After |
|--------|--------|-------|
| `SHealth` | 로드·보정·BMI·통계·조회 전부 | 파사드(오케스트레이션 + public API) |
| `CsvHealthRecordLoader` | — | CSV 파싱·`UserRecord` 생성 |
| `WeightImputer` / `HeightImputer` | — | 연령대 평균 보정 |
| `AgeDecadeStatistics` | — | 연령대별 BMI 비율(%) |
| `UserQueryService` | — | 정상 id 목록·전체 비율 |
| `BmiClassifier` | BMI 계산·분류 | 변경 없음 |

---

## README Activity 4 체크리스트

| 항목 | 상태 |
|------|------|
| SRP 책임 분리 | ✅ |
| 특정 연령대 BMI 분포 비율 | ✅ (`getRatio`) |
| `height==0` 연령대 평균 키 보정 | ✅ |
| BMI 정상 범위 사용자 목록 | ✅ (`getNormalBmiUserIds`) |
| 전체 사용자 대비 범주별 비율 | ✅ (`getOverallRatio`) |

---

테스트: **20 tests, BUILD SUCCESS**

`/commit`으로 Report·Prompting·커밋까지 진행할까요?

---

**User**

그래


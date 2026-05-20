# Activity4 height 보정 TDD 검증
_Exported on 5/20/2026 at 10:43:38 GMT+9 from Cursor (unknown)_

---

**User**

Height가 0인 경우에 대한 평균치 보정 로직 추가 를 하고 싶어. TDD 방식으로 구현을 해야 할 것 같은데 
PCTF 프롬프트를 니가 현재 코드 베이스를 참고해서 만들어줘

---

**Cursor**

코드베이스와 기존 PCTF·보정 로직 패턴을 확인한 뒤 Height=0 평균 보정용 PCTF 프롬프트를 작성합니다.


코드베이스를 기준으로 정리하면, **`HeightImputer`와 통합 TC 1건(`should_imputeDecadeAverageHeight_when_heightIsZero`)은 이미 있습니다.** README 41행 목표를 **처음부터 TDD로** 달성하거나, **보정 전용 TC·단위 테스트를 보강**할 때 아래 PCTF를 쓰면 됩니다.

---

## PCTF란 (이 프로젝트 기준)

| 구분 | 역할 |
|------|------|
| **P**ersona | TDD·`WeightImputer` 대칭 패턴에 익숙한 Java 실습 담당 |
| **C**ontext | Activity 4, 파이프라인·도메인·현재 구현/갭 |
| **T**ask | `height==0` 연령대 평균 보정 — Red→Green→Refactor, 범위·금지 |
| **F**ormat | `should_…_when_…`, fixture, `mvn test`, 한국어 요약 |

**첫 메시지에 함께 붙이기:**

`@Codebase` `@README.md` `@.cursorrules` `@.cursor/rules/java-tests.mdc` `@src/main/java/com/bestreviewer/WeightImputer.java` `@src/main/java/com/bestreviewer/HeightImputer.java` `@src/main/java/com/bestreviewer/SHealth.java` `@src/test/java/com/bestreviewer/SHealthBMITest.java`

---

## 현재 상태 vs TDD로 보강할 것

| 항목 | 현재 | TDD로 보강할 것 |
|------|------|-----------------|
| 구현 | `HeightImputer` — `WeightImputer`와 동일 구조 | 없으면 Green 단계에서 신규 추가 |
| 파이프라인 | `load` → `weightImputer` → `heightImputer` → BMI → 통계 | 순서 변경 금지(체중 보정 후 키 보정) |
| 통합 TC | `impute-height.csv` + `getRatio` 2개 assertion | 연령대 격리·유효 키 0건 등 **체중 TC와 대칭** |
| 단위 TC | 없음 (`HeightImputerTest` 없음) | `List<UserRecord>` 직접 `impute` 후 `getHeight()` 검증 |

---

## 복사용 — Height `0` 평균 보정 (TDD) PCTF

아래 블록을 **새 채팅 첫 메시지**에 그대로 붙이면 됩니다.

```markdown
## Persona (역할)
당신은 Java 21·Maven·JUnit 5 실습용 **TDD(Test-Driven Development) 전문가**입니다.
- README Activities **4** 중 **「Height가 0인 경우에 대한 평균치 보정 로직 추가」**만 수행합니다.
- **반드시 Red → Green → Refactor 순서**를 따릅니다. (1) 실패하는 테스트 추가 → (2) 최소 구현으로 통과 → (3) `WeightImputer`와 중복만 정리, 동작 불변.
- 이번 Task에서 **수정·추가하지 않을 것**: 연령대 BMI 분포 전용 TC 대량 추가, `getNormalBmiUserIds`, `getOverallRatio`, `AgeDecadeStatistics` 집계 로직 변경, 커밋·Report·Prompting·push(사용자 `/commit` 또는 명시 요청 시만).
- 기존 TC(특히 `impute-weight.csv`, `impute-cross-decade.csv`, `impute-no-valid-weight.csv` 관련 assertion)는 **값 변경 없이** 통과해야 합니다. 깨지면 회귀만 최소 수정합니다.
- 루트 `.cursorrules` 우선: 테스트명 `should_[결과]_when_[조건]`, public Javadoc, 메서드 최대 20줄, 최소 diff.

## Context (맥락)

**도메인 (README·shealth-project.mdc)**
| 항목 | 규칙 |
|------|------|
| 키 누락 | `height == 0` → **동일 연령대**(10년 단위: 20~29→20, 30~39→30, …) **유효 키(height ≠ 0)** 의 산술 평균(cm)으로 대체 |
| 유효 키 없음 | 해당 연령대에 `height ≠ 0`인 사용자가 한 명도 없으면 → `height==0` 레코드는 **0 유지**(보정 스킵). 체중 보정(`WeightImputer` + `impute-no-valid-weight.csv`)과 동일 |
| 연령대 격리 | 20대 누락 키는 30대 평균을 쓰지 않음 (`AgeDecade.isInDecade`) |
| BMI | 보정 **후** `BmiClassifier.computeBmi(weight, height)` — 키 cm→m |
| 파이프라인 | `SHealth.calculateBmi`: `CsvHealthRecordLoader.load` → `WeightImputer.impute` → **`HeightImputer.impute`** → BMI 계산 → `AgeDecadeStatistics.aggregate` |

**참조 구현 (체중 보정 — 키 보정은 이와 대칭)**
- `WeightImputer`: 연령대 루프 `MIN_DECADE`~`MAX_DECADE`/`STEP`, `NO_DECADE_AVERAGE = -1`이면 스킵, `record.setWeight(average)`
- `HeightImputer`: 동일 패턴, `record.setHeight(average)` (이미 있을 수 있음 — 있으면 **검증 TDD**로 테스트만 먼저 보강)

**기존 fixture·TC (회귀 앵커)**
- `impute-height.csv` — 20대 3명, id=2만 `height=0`:
  ```csv
  id,age,weight,height
  1,25,70.0,170.0
  2,27,70.0,0.0
  3,28,70.0,180.0
  ```
  - 20대 유효 키 평균: (170+180)/2 = **175.0**
  - id=2 보정 후 BMI ≈ 22.86 → NORMAL; id=1 ≈ 24.22 → OVERWEIGHT; id=3 ≈ 21.60 → NORMAL
  - 20대 비율: OVERWEIGHT **33.33%**, NORMAL **66.67%** (`should_imputeDecadeAverageHeight_when_heightIsZero` — assertion 변경 금지)

**갭 (이번 TDD로 채울 것)**
| # | 체중 쪽(있음) | 키 쪽(추가 권장) |
|---|----------------|------------------|
| 1 | `should_imputeDecadeAverageWeight_…` | 기본 TC 있음 → 유지 |
| 2 | `should_imputeOnlySameDecade_…` (`impute-cross-decade.csv`) | **연령대별 키 평균 격리** TC + fixture |
| 3 | `should_keepZeroWeight_when_noValidWeightInDecade` | **유효 키 0건 시 height 0 유지** TC + fixture |
| 4 | (단위 테스트 없음) | `HeightImputerTest` — CSV 없이 `impute` 후 `getHeight()` |

**제약**
- fixture: `src/test/resources/`, 헤더 `id,age,weight,height`
- 통합: `SHealth` + `resourcePath("…")` 패턴 유지
- 단위: `new HeightImputer().impute(mutableList)` 후 레코드 필드 assert (Red 단계에서 프로덕션 없이 실패 가능)
- Mockito 불필요(순수 도메인)
- `HeightImputer`가 이미 통과 중이면 Phase 1에서 **신규 TC만** 추가해 Red를 만들고, Green은 “이미 구현됨”일 수 있음 → Refactor만 수행

## Task (할 일) — TDD 단계

### Phase 0 — 현황 확인 (코드 수정 없음)
- `HeightImputer` 존재 여부, `SHealth.calculateBmi`에서 `heightImputer.impute` 호출 순서 확인
- `mvn test` 전체 통과 여부 확인
- 이미 구현되어 있으면 **「검증·보강 TDD」**: 아래 Phase 1 TC 추가 → (필요 시) Green → Refactor

### Phase 1 — Red: 실패하는 테스트 추가
**프로덕션 수정 전에** 테스트·fixture를 먼저 추가합니다.

| # | 의도 | 메서드명 예시 | fixture |
|---|------|----------------|---------|
| 1 | 동 연령대 평균 키로 대체 후 BMI/비율 반영 | `should_imputeDecadeAverageHeight_when_heightIsZero` | `impute-height.csv` (있으면 스킵) |
| 2 | 20대·30대 혼재 시 키 보정은 각 연령대 평균만 사용 | `should_imputeOnlySameDecadeHeight_when_multipleDecadesPresent` | `impute-cross-decade-height.csv` (신규) |
| 3 | 연령대 내 유효 키 0명 → height 0 유지 → BMI 계산 불가/0에 가까운 동작 | `should_keepZeroHeight_when_noValidHeightInDecade` | `impute-no-valid-height.csv` (신규) |
| 4 | 단위: impute 후 누락 행만 평균으로 설정 | `should_setDecadeAverageHeight_when_heightIsZero` | 없음 (`HeightImputerTest`) |

**fixture 설계 힌트 (Phase 1에서 기대값을 손으로 계산 후 assert)**

`impute-cross-decade-height.csv` 예시 골격:
```csv
id,age,weight,height
1,25,70.0,160.0
2,27,70.0,0.0
3,28,70.0,180.0
4,38,70.0,170.0
```
- 20대 유효 키 평균 (160+180)/2 = 170 → id=2 보정
- 30대는 id=4만 유효 → 평균 170 (또는 30대에 height=0 추가 시 별도 계산)
- assertion은 `getRatio(20, …)`, `getRatio(30, …)`로 **연령대 격리** 검증

`impute-no-valid-height.csv` 예시 (체중 `impute-no-valid-weight.csv` 대칭):
```csv
id,age,weight,height
1,32,70.0,0.0
2,35,70.0,0.0
```
- 30대 유효 키 0명 → 보정 스킵 → height 0 유지 시 BMI/비율 기대값을 **사전 계산**해 assert (체중 TC와 동일 패턴)

**단위 TC 예시 (`HeightImputerTest`)**
```java
List<UserRecord> records = List.of(
    new UserRecord(1, 25, 70.0, 170.0),
    new UserRecord(2, 27, 70.0, 0.0),
    new UserRecord(3, 28, 70.0, 180.0));
new HeightImputer().impute(new ArrayList<>(records));
assertEquals(175.0, records.get(1).getHeight(), 0.01);
```

### Phase 2 — Green: 최소 구현
- `HeightImputer` 없으면 `WeightImputer`를 복사·치환해 생성 (`MISSING_HEIGHT`, `averageValidHeightInDecade`, `setHeight`)
- `SHealth.calculateBmi`에 `heightImputer.impute(records)`가 **체중 보정 다음·BMI 계산 전**인지 확인
- 신규 TC가 모두 통과할 때까지 **최소** 수정
- **하지 말 것**: `BmiClassifier` 경계 변경, 통계 집계 방식 변경

### Phase 3 — Refactor
- `WeightImputer`/`HeightImputer` 중복이 보이면 **공통 추상화는 이번에 하지 않음**(범위 초과). Javadoc·상수명·private 메서드 20줄 이하만 정리
- `mvn test` 전체 통과 확인

## Format (산출물)
1. **변경 요약(한국어 3~5줄)**: 무엇을 Red/Green/Refactor 했는지, 파이프라인 순서, 영향(키 보정·BMI·연령대 비율).
2. **변경 파일 목록**: 예) `HeightImputer.java`, `SHealth.java`, `SHealthBMITest.java`, `HeightImputerTest.java`, `src/test/resources/impute-*.csv`
3. **테스트 표**: 추가한 `should_…_when_…` 와 fixture·기대값(±0.01).
4. **성공 기준**: `mvn test` 전체 통과 + README 41행 bullet 충족(키 보정 API/로직 + TC 최소 1건 통합 + 권장 1건 단위).
5. diff는 최소 범위; 식별자·enum·fixture 이름은 **영문 유지**.

## 금지·주의
- `weight==0` 보정 로직·기존 fixture assertion **임의 변경 금지**
- 키 보정을 BMI 계산 **전**이 아닌 **후**에 넣지 말 것
- `shealth.dat` 전체 파일을 통합 테스트에 쓰지 말 것
- God Class `SHealth`에 보정 알고리즘을 다시 넣지 말 것 — `HeightImputer`에 둘 것
```

---

## 프롬프트 설계 팁

1. **이미 구현된 경우** — Phase 0에서 `HeightImputer`가 있으면 “Greenfield”가 아니라 **Phase 1의 2·3·4번 TC만 Red**로 추가하는 **검증 TDD**로 진행하라고 명시해 두었습니다.
2. **회귀 앵커** — `impute-height.csv`의 **33.33 / 66.67** assertion은 손계산과 일치하므로 PCTF에 넣어 두면 임의 변경을 줄일 수 있습니다.
3. **체중과 대칭** — `impute-cross-decade.csv`, `impute-no-valid-weight.csv`와 같은 시나리오를 키용 fixture로 짝 지으면 리뷰·실습 설명이 쉽습니다.
4. **파이프라인** — 체중 보정이 먼저인 이유: `weight==0`도 있으면 키 보정 전에 체중이 정해져야 BMI가 일관됩니다 (`SHealth` 28–31행).

---

원하시면 위 내용을 `Prompting/09_Activity4-height-보정-TDD-Prompting.md`로 프로젝트에 저장해 드리거나, **구현이 없는 상태를 가정한 Greenfield-only** 짧은 버전도 따로 적어 드리겠습니다.

---

**User**

## Persona (역할)
당신은 Java 21·Maven·JUnit 5 실습용 **TDD(Test-Driven Development) 전문가**입니다.
- README Activities **4** 중 **「Height가 0인 경우에 대한 평균치 보정 로직 추가」**만 수행합니다.
- **반드시 Red → Green → Refactor 순서**를 따릅니다. (1) 실패하는 테스트 추가 → (2) 최소 구현으로 통과 → (3) `WeightImputer`와 중복만 정리, 동작 불변.
- 이번 Task에서 **수정·추가하지 않을 것**: 연령대 BMI 분포 전용 TC 대량 추가, `getNormalBmiUserIds`, `getOverallRatio`, `AgeDecadeStatistics` 집계 로직 변경, 커밋·Report·Prompting·push(사용자 `/commit` 또는 명시 요청 시만).
- 기존 TC(특히 `impute-weight.csv`, `impute-cross-decade.csv`, `impute-no-valid-weight.csv` 관련 assertion)는 **값 변경 없이** 통과해야 합니다. 깨지면 회귀만 최소 수정합니다.
- 루트 `.cursorrules` 우선: 테스트명 `should_[결과]_when_[조건]`, public Javadoc, 메서드 최대 20줄, 최소 diff.

## Context (맥락)

**도메인 (README·shealth-project.mdc)**
| 항목 | 규칙 |
|------|------|
| 키 누락 | `height == 0` → **동일 연령대**(10년 단위: 20~29→20, 30~39→30, …) **유효 키(height ≠ 0)** 의 산술 평균(cm)으로 대체 |
| 유효 키 없음 | 해당 연령대에 `height ≠ 0`인 사용자가 한 명도 없으면 → `height==0` 레코드는 **0 유지**(보정 스킵). 체중 보정(`WeightImputer` + `impute-no-valid-weight.csv`)과 동일 |
| 연령대 격리 | 20대 누락 키는 30대 평균을 쓰지 않음 (`AgeDecade.isInDecade`) |
| BMI | 보정 **후** `BmiClassifier.computeBmi(weight, height)` — 키 cm→m |
| 파이프라인 | `SHealth.calculateBmi`: `CsvHealthRecordLoader.load` → `WeightImputer.impute` → **`HeightImputer.impute`** → BMI 계산 → `AgeDecadeStatistics.aggregate` |

**참조 구현 (체중 보정 — 키 보정은 이와 대칭)**
- `WeightImputer`: 연령대 루프 `MIN_DECADE`~`MAX_DECADE`/`STEP`, `NO_DECADE_AVERAGE = -1`이면 스킵, `record.setWeight(average)`
- `HeightImputer`: 동일 패턴, `record.setHeight(average)` (이미 있을 수 있음 — 있으면 **검증 TDD**로 테스트만 먼저 보강)

**기존 fixture·TC (회귀 앵커)**
- `impute-height.csv` — 20대 3명, id=2만 `height=0`:
  ```csv
  id,age,weight,height
  1,25,70.0,170.0
  2,27,70.0,0.0
  3,28,70.0,180.0

---

**Cursor**

PCTF에 따라 Height 보정 TDD를 진행합니다. 현황 확인 후 Red 단계(보강 TC·fixture)부터 추가합니다.


PCTF에 따라 검증 TDD로 보강 TC·fixture·단위 테스트를 추가합니다. `HeightImputer`는 이미 있으므로 Green은 통과할 것으로 예상됩니다.


PCTF에 따라 **검증 TDD**(Phase 1 Red → 기존 구현으로 Green)를 적용했습니다. `HeightImputer`와 파이프라인은 이미 있어 프로덕션 코드는 변경하지 않았습니다.

## 변경 요약

**이유:** README 41행(키 `0` → 동 연령대 평균)을 체중 보정과 **대칭**으로 TC·fixture로 고정합니다.  
**영향:** 키 보정·BMI·연령대 비율만 검증 범위가 넓어졌고, 기존 `impute-weight`/`impute-cross-decade`/`impute-no-valid-weight` assertion은 그대로입니다.

## 추가한 테스트

| 메서드 | fixture / 범위 | 기대 |
|--------|----------------|------|
| `should_imputeOnlySameDecadeHeight_when_multipleDecadesPresent` | `impute-cross-decade-height.csv` | 20대 33.33% OBESITY/OVERWEIGHT/NORMAL, 30대 100% OVERWEIGHT |
| `should_keepZeroHeight_when_noValidHeightInDecade` | `impute-no-valid-height.csv` | 30대 유효 키 없음 → height 0 유지 → 100% OBESITY |
| `should_setDecadeAverageHeight_when_heightIsZero` | `HeightImputerTest` 단위 | id=2 → 175.0 cm |
| `should_keepZeroHeight_when_noValidHeightInDecade` | `HeightImputerTest` 단위 | 30대 전원 height 0 유지 |

기존 `should_imputeDecadeAverageHeight_when_heightIsZero` + `impute-height.csv`는 **변경 없음**.

## TDD 단계 정리

| 단계 | 결과 |
|------|------|
| **Red** | 신규 TC 4건 + fixture 2개 추가 |
| **Green** | `HeightImputer`·`SHealth.calculateBmi` 기존 구현으로 즉시 통과 |
| **Refactor** | 중복 추상화 없음(범위 외), diff 최소 |

## 성공 기준

`mvn test` **전체 통과** (기존 + 신규 TC).

---

다음으로 `Prompting/09_Activity4-height-보정-TDD-Prompting.md` 저장이나 Report 작성이 필요하면 알려 주세요. `/commit`은 요청 시에만 진행합니다.

---

**User**

<cursor_commands>

--- Cursor Command: commit ---
# 커밋 (Report → Prompting → Git)

이 명령은 **커밋 명시 요청**으로 간주합니다. 프로젝트 규칙 `@commit-workflow` 및 `@response-style`을 따릅니다.

**반드시 1 → 2 → 3 순서**로 진행하고, 각 단계가 끝나기 전에 Git 커밋하지 마세요.

## 0. 준비

- `Report/`, `Prompting/` 폴더가 없으면 생성합니다.
- `Report/`에서 `^\d+_.*\.md$` 패턴 파일의 **최대 번호 + 1**을 사용합니다. 없으면 `01`.
- 번호: `01`~`09`는 2자리, `10` 이상은 자연수.
- **작업 title명**: 이번 세션 작업을 한 줄로 요약 (`/\:*?"<>|` → `_`).

## 1. Report 작성

- 경로: `Report/{번호}_{작업 title명}.md`
- 현재 세션에서 수행한 작업을 마크다운 **작업 보고서**로 정리 (고정 템플릿 없음).

## 2. Prompting 저장 (1번 직후, 커밋 전 필수)

- **`.cursor/commands/scripts/export-cursor-transcript.py`** 로 **지금 이 대화창**에 해당하는 JSONL만 읽어 Cursor **Export Transcript** 형식 MD로 저장합니다.
- **저장**: `Prompting/{번호}_{작업 title명}-Prompting.md` (1번과 **동일** `{번호}_{작업 title명}`).
- 실행 시 **프로젝트 루트**를 현재 디렉터리로 둡니다 (`-o Prompting/…` 경로 기준).
- (선택) 1행 제목: `--title "{작업 title명}"` — 생략 시 첫 사용자 메시지 요약.

### 현재 대화 JSONL 식별 (필수)

- **금지**: `--project-slug` 만으로 “가장 최근” JSONL을 고르지 않습니다 (다른 채팅이 선택될 수 있음).
- **우선**: Cursor가 제공하는 **현재 대화 ID** →  
  `~/.cursor/projects/e-DEV-SHealth-04/agent-transcripts/<id>/<id>.jsonl`  
  (`--conversation-id` 는 `~/.cursor/projects` 아래 프로젝트를 검색해 해당 ID를 찾습니다.)
- **보조**: 위 ID를 모르면 `agent-transcripts` 아래 JSONL을 검색해 **이번 세션의 사용자 메시지·주제**와 일치하는 파일을 찾습니다.
- **불가 시**: 사용자에게 대화 ID 또는 확인을 요청합니다. **잘못된 JSONL로 export한 뒤 커밋하지 않습니다.**

```bash
python .cursor/commands/scripts/export-cursor-transcript.py --conversation-id <현재-대화-uuid> \
  -o Prompting/{번호}_{작업title}-Prompting.md
```

또는 JSONL 경로를 직접 지정:

```bash
python .cursor/commands/scripts/export-cursor-transcript.py \
  "%USERPROFILE%\.cursor\projects\e-DEV-SHealth-04\agent-transcripts\<id>\<id>.jsonl" \
  -o Prompting/{번호}_{작업title}-Prompting.md
```

### export 후 확인

- 파일이 생성되었고, `**User**` / `**Cursor**` 턴과 `---` 구분이 있는지 확인합니다.
- tool·thinking 내용은 스크립트가 제외합니다. 수동으로 JSONL을 읽어 덧붙이지 않습니다.

## 3. Git 커밋

1. `git status`, `git diff`, `git log -5` 로 변경 확인.
2. 스테이징에 **반드시 포함**: 이번 `Report/…md`, `Prompting/…-Prompting.md`, 코드·설정 변경.
3. **제외**: `target/`, `**/__pycache__/`, `*.pyc`, 비밀·자격증명.
4. 커밋 메시지: 1~2문장, **why** 중심 (`feat:`, `docs:`, `chore:` 등). PowerShell: `git commit -m "..."`.
5. `git status`로 완료 확인.
6. **push 하지 않음** (사용자가 별도 요청할 때만).

## 주의

- 업체 제공 스켈레톤·저작권 헤더 파일의 의도치 않은 변경이 섞이면 커밋하지 말고 분리·되돌림을 제안합니다.
- `git config` 변경, `--no-verify`, `push --force` 금지 (사용자 명시 시만 예외).
- 완료 후 사용자에게 Report·Prompting 경로와 커밋 해시(또는 요약)를 한국어로 알립니다.

변경 내용 모두 커밋해줘
--- End Command ---
</cursor_commands>
<user_query>
/commit 변경 내용 모두 커밋해줘 
</user_query>


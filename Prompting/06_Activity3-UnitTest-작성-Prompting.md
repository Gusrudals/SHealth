# Activity 3 UnitTest 작성 PCTF 실행 및 커밋
_Exported on 5/20/2026 at 10:02:33 GMT+9 from Cursor (unknown)_

---

**User**

현재 코드베이스 및 
@README.md (33-37) 
의 내용을 참조해서 PCTF 방식의 프롬프트를 어떻게 하면 좋을지 알려줘

---

**Cursor**

README의 UnitTest 항목과 기존 PCTF 프롬프트 형식·코드베이스 상태를 확인합니다.


Activity 2 이후 코드와 `Prompting/04` 패턴을 기준으로, README Activity 3(33–37행)에 맞는 **PCTF 프롬프트 설계**를 정리했습니다.

## 현재 상태 vs README Activity 3

| README 항목 | 현재 커버리지 | 보강 포인트 |
|-------------|---------------|-------------|
| BMI 계산 | `should_computeExpectedBmi_when_weightAndHeightGiven` 1건 | cm→m 변환, 다른 체중·키 조합, 소수 `delta` |
| Age 평균치 보정 | `impute-weight.csv` + 비율 1건 | 동일 연령대만 보정, 다른 연령대 미보정, 평균값 직접 검증 |
| 분류 TC | 경계 18.5, 18.51, 23, 25 | 23 미만 정상, 25 미만 과체중 등 **구간 대표값** |
| 예외 TC | **없음** | 빈 파일, 컬럼 오류, 유효 체중 0건 연령대(평균 불가) |

`BmiClassifier`·`SHealth`·fixture는 이미 테스트 가능한 형태입니다. Activity 3 프롬프트는 **기능 추가(Activity 4)** 보다 **TC 보강·예외 명세**에 두는 것이 맞습니다.

---

## PCTF란 (이 프로젝트 기준)

`Prompting/04`와 동일합니다.

| 구분 | 역할 |
|------|------|
| **P**ersona | JUnit 5·도메인 경계에 익숙한 테스트 전문가 |
| **C**ontext | 리팩토링 후 코드, README 도메인, `.cursorrules` / `java-tests.mdc` |
| **T**ask | README 4항목별 TC 목록·금지 범위 |
| **F**ormat | `should_…_when_…`, fixture, `mvn test`, 최소 diff |

Cursor에서는 `@Codebase` `@README.md` `@.cursorrules` `@.cursor/rules/java-tests.mdc` 를 함께 붙이면 Context가 안정됩니다.

---

## 복사용 — Activity 3 전체 UnitTest PCTF

아래 블록을 그대로 새 채팅 첫 메시지에 붙이면 됩니다.

```markdown
## Persona (역할)
당신은 Java 21·Maven·JUnit 5 실습용 **단위 테스트 전문가**입니다.
- README Activities **3**(UnitTest 작성)만 수행합니다.
- Activity 4(키 보정, 정상 BMI 목록, 전체 비율 API, 대규모 SRP 분리)는 **구현하지 않습니다**.
- 기존 `BmiClassifier` / `SHealth` / `SHealthBMITest` 동작을 깨지 않게 하고, 실패하는 TC가 나오면 **도메인·README에 맞는 최소 수정**만 합니다.
- 루트 `.cursorrules` 우선: 테스트명 `should_[결과]_when_[조건]`, JUnit 5, fixture는 `src/test/resources/`.

## Context (맥락)
**도메인 (README)**
| 항목 | 규칙 |
|------|------|
| BMI | 체중(kg) / (키(m))², 키 cm→m |
| 저체중 | BMI ≤ 18.5 |
| 정상 | 18.5 < BMI < 23 |
| 과체중 | 23 ≤ BMI < 25 |
| 비만 | BMI ≥ 25 |
| 체중 누락 | weight == 0 → **동일 연령대**(10년 단위) 평균 체중 |

**현재 코드**
- `BmiClassifier.computeBmi`, `BmiClassifier.classify` — 순수 단위 테스트 대상
- `SHealth.calculateBmi` — CSV 로드 → `imputeZeroWeightsByAgeDecade` → BMI → 연령대 비율
- 기존 테스트 6건: BMI 1, 분류 경계 4, 보정 통합 1 (`impute-weight.csv`)
- `java-tests.mdc` 필수: BMI·보정·분류·**예외**(빈 파일, 잘못된 컬럼, 평균 불가 연령대)

**제약**
- `shealth.dat` 전체 통합은 선택(느리면 소형 fixture 우선)
- Mockito는 필요할 때만(I/O·순수 로직은 fixture/직접 호출 우선)
- 플레이스홀더 `fail()` 테스트 추가 금지

## Task (할 일)
README Activity 3 네 가지를 **모두** 커버하는 테스트를 추가·보강하세요.

### 1) BMI 계산 로직 TC
- `BmiClassifier.computeBmi` 단위 테스트
- 알려진 (weightKg, heightCm) → 기대 BMI (`assertEquals(expected, actual, 0.01)`)
- 최소 2~3조합 (기존 70kg/170cm 외 1건 이상)

### 2) Age(연령대) 평균치 보정 TC
- `src/test/resources/`에 작은 CSV fixture 추가 가능
- 검증 예:
  - 동일 연령대 `weight==0`만 해당 연령대 평균으로 대체
  - 다른 연령대 레코드는 영향 없음
  - 보정 후 BMI/비율이 기대와 일치 (기존 `impute-weight.csv` 패턴 확장)
- 가능하면 **평균값(예: (70+80)/2=75)** 을 간접이 아닌 BMI·비율로 검증

### 3) 정상/저체중/과체중/비만 분류 TC
- `BmiClassifier.classify` 경계·구간 대표값
- 필수 경계: 18.5, 23, 25 (기존 TC 유지)
- 추가: 22.99→NORMAL, 24.99→OVERWEIGHT, 25.01→OBESITY 등
- README와 불일치 시 **코드 수정 + TC** (특히 BMI=25 → OBESITY)

### 4) 예외상황 TC
- fixture 또는 `assertThrows`로 명시
- 최소 포함:
  - 존재하지 않는 파일 → `IOException`
  - 헤더만/빈 데이터 파일
  - 컬럼 수 부족·잘못된 숫자 형식 (`NumberFormatException` 등 실제 동작에 맞게)
  - 한 연령대에 유효 체중이 하나도 없는데 `weight==0`만 있는 경우 (평균 불가 — 기대 동작을 테스트명·assertion으로 문서화)
- 예외 TC 추가 후 실패하면, README·실습 목적에 맞게 **최소한의 방어 로직**만 `SHealth`에 추가

**이번 Task에서 하지 말 것**
- `height==0` 보정, 정상 BMI 사용자 목록, 전체 사용자 비율 API
- 대규모 구조 변경·God Class 재분해

## Format (출력 형식)
1. 작업 전: 추가할 테스트 메서드·fixture 목록(표)
2. 구현: 테스트 코드 + 필요 시 최소 프로덕션 수정
3. 검증: `mvn test` 결과
4. 마무리: README 4항목별 커버리지 체크리스트(한국어)

분석만 요청이 아니면 **반드시 테스트 코드를 작성**하고 실행까지 확인합니다.
```

---

## README 4항목별 “짧은 PCTF” (한 번에 부담될 때)

Task만 바꿔 4번 반복하면 됩니다.

**1) BMI만**
```text
Task: BmiClassifier.computeBmi 단위 TC 3건 추가. 기존 should_computeExpectedBmi 유지. Activity 4 금지.
Format: should_…_when_…, delta 0.01, mvn test.
```

**2) 보정만**
```text
Task: weight==0 연령대 평균 보정 TC. fixture CSV 신규 1~2개. 20대만 0인 케이스, 30대는 미보정 케이스 포함.
Context: @impute-weight.csv @SHealth.java
```

**3) 분류만**
```text
Task: BmiClassifier.classify 구간·경계 TC 보강. 18.5/23/25 및 구간 대표값. README 경계와 불일치 시 classify 수정.
```

**4) 예외만**
```text
Task: 빈 파일·잘못된 CSV·평균 불가 연령대 TC. assertThrows 명시. 실패 시 SHealth 최소 방어 로직만 추가.
Context: @java-tests.mdc 예외 항목
```

---

## 프롬프트 설계 팁 (이 코드베이스에 맞게)

1. **첫 메시지 조합**  
   `@Codebase` + 위 PCTF + `@src/test/java/com/bestreviewer/SHealthBMITest.java`  
   `src/test/**`를 열면 `java-tests.mdc`가 자동 적용됩니다.

2. **예외 TC는 “기대 동작”을 먼저 적기**  
   `averageValidWeightInDecade`는 유효 체중 0건이면 `0/0` → NaN이 될 수 있습니다. 프롬프트에 *“평균 불가 시 0 유지 / 스킵 / 예외” 중 README·실습 의도에 맞는 것을 테스트로 고정*이라고 쓰면 AI가 임의로 넘어가지 않습니다.

3. **통합 vs 단위 분리**  
   - BMI·분류 → `BmiClassifier` **단위** (빠름, 원인 분리)  
   - 보정·CSV·비율 → `SHealth` + **fixture** (README “Age 평균치”에 가깝음)

4. **Activity 2와의 연속**  
   `Prompting/04` 마지막에 “Activity 3: 예외 TC”가 이미 적혀 있으므로, Report/Prompting 파일명은 `06_Activity3-UnitTest-Prompting.md` 같은 식으로 이어가면 워크플로와 맞습니다.

5. **성공 기준을 프롬프트 끝에 고정**  
   예: “`mvn test` 전부 통과 + README 4 bullet 각각 최소 2 assertion” — 완료 판단이 명확해집니다.

---

원하시면 위 PCTF를 바탕으로 `Prompting/06_Activity3-UnitTest-Prompting.md` 초안 파일까지 만들어 드리거나, 예외 3케이스에 대한 **fixture CSV 예시 행**만 따로 적어 드릴 수 있습니다.

---

**User**

## Persona (역할)
당신은 Java 21·Maven·JUnit 5 실습용 **단위 테스트 전문가**입니다.
- README Activities **3**(UnitTest 작성)만 수행합니다.
- Activity 4(키 보정, 정상 BMI 목록, 전체 비율 API, 대규모 SRP 분리)는 **구현하지 않습니다**.
- 기존 `BmiClassifier` / `SHealth` / `SHealthBMITest` 동작을 깨지 않게 하고, 실패하는 TC가 나오면 **도메인·README에 맞는 최소 수정**만 합니다.
- 루트 `.cursorrules` 우선: 테스트명 `should_[결과]_when_[조건]`, JUnit 5, fixture는 `src/test/resources/`.

## Context (맥락)
**도메인 (README)**
| 항목 | 규칙 |
|------|------|
| BMI | 체중(kg) / (키(m))², 키 cm→m |
| 저체중 | BMI ≤ 18.5 |
| 정상 | 18.5 < BMI < 23 |
| 과체중 | 23 ≤ BMI < 25 |
| 비만 | BMI ≥ 25 |
| 체중 누락 | weight == 0 → **동일 연령대**(10년 단위) 평균 체중 |

**현재 코드**
- `BmiClassifier.computeBmi`, `BmiClassifier.classify` — 순수 단위 테스트 대상
- `SHealth.calculateBmi` — CSV 로드 → `imputeZeroWeightsByAgeDecade` → BMI → 연령대 비율
- 기존 테스트 6건: BMI 1, 분류 경계 4, 보정 통합 1 (`impute-weight.csv`)
- `java-tests.mdc` 필수: BMI·보정·분류·**예외**(빈 파일, 잘못된 컬럼, 평균 불가 연령대)

**제약**
- `shealth.dat` 전체 통합은 선택(느리면 소형 fixture 우선)
- Mockito는 필요할 때만(I/O·순수 로직은 fixture/직접 호출 우선)
- 플레이스홀더 `fail()` 테스트 추가 금지

## Task (할 일)
README Activity 3 네 가지를 **모두** 커버하는 테스트를 추가·보강하세요.

### 1) BMI 계산 로직 TC
- `BmiClassifier.computeBmi` 단위 테스트
- 알려진 (weightKg, heightCm) → 기대 BMI (`assertEquals(expected, actual, 0.01)`)
- 최소 2~3조합 (기존 70kg/170cm 외 1건 이상)

### 2) Age(연령대) 평균치 보정 TC
- `src/test/resources/`에 작은 CSV fixture 추가 가능
- 검증 예:
  - 동일 연령대 `weight==0`만 해당 연령대 평균으로 대체
  - 다른 연령대 레코드는 영향 없음
  - 보정 후 BMI/비율이 기대와 일치 (기존 `impute-weight.csv` 패턴 확장)
- 가능하면 **평균값(예: (70+80)/2=75)** 을 간접이 아닌 BMI·비율로 검증

### 3) 정상/저체중/과체중/비만 분류 TC
- `BmiClassifier.classify` 경계·구간 대표값
- 필수 경계: 18.5, 23, 25 (기존 TC 유지)
- 추가: 22.99→NORMAL, 24.99→OVERWEIGHT, 25.01→OBESITY 등
- README와 불일치 시 **코드 수정 + TC** (특히 BMI=25 → OBESITY)

### 4) 예외상황 TC
- fixture 또는 `assertThrows`로 명시
- 최소 포함:
  - 존재하지 않는 파일 → `IOException`
  - 헤더만/빈 데이터 파일
  - 컬럼 수 부족·잘못된 숫자 형식 (`NumberFormatException` 등 실제 동작에 맞게)
  - 한 연령대에 유효 체중이 하나도 없는데 `weight==0`만 있는 경우 (평균 불가 — 기대 동작을 테스트명·assertion으로 문서화)
- 예외 TC 추가 후 실패하면, README·실습 목적에 맞게 **최소한의 방어 로직**만 `SHealth`에 추가

**이번 Task에서 하지 말 것**
- `height==0` 보정, 정상 BMI 사용자 목록, 전체 사용자 비율 API
- 대규모 구조 변경·God Class 재분해

## Format (출력 형식)
1. 작업 전: 추가할 테스트 메서드·fixture 목록(표)
2. 구현: 테스트 코드 + 필요 시 최소 프로덕션 수정
3. 검증: `mvn test` 결과
4. 마무리: README 4항목별 커버리지 체크리스트(한국어)

분석만 요청이 아니면 **반드시 테스트 코드를 작성**하고 실행까지 확인합니다.

---

**Cursor**

Activity 3 PCTF에 따라 테스트·fixture를 추가하고, 필요 시 `SHealth`에 최소 방어 로직을 넣은 뒤 `mvn test`로 검증합니다.


작업 전 계획을 정리한 뒤 fixture·테스트·`SHealth` 최소 수정을 진행합니다.


테스트는 통과했습니다. 프로젝트 스타일에 맞게 들여쓰기를 4칸으로 맞춥니다.


Activity 3 PCTF를 반영해 테스트·fixture를 추가했고, 예외·보정 엣지 케이스를 위해 `SHealth`에 최소 방어 로직만 넣었습니다.

**변경 이유:** README Activity 3 네 가지(BMI·연령대 보정·분류·예외) 커버리지 충족.  
**영향:** BMI/분류 동작 동일, `weight==0` 보정 시 유효 체중 0건 연령대는 보정 스킵, CSV 컬럼 부족 시 `IOException`.

---

## 1. 작업 전 계획 (실행 내용)

| 구분 | 테스트 메서드 | fixture |
|------|---------------|---------|
| BMI | `should_computeExpectedBmi_when_another…`, `…_sampleData…` | — |
| 보정 | `should_imputeOnlySameDecade_…`, `should_keepZeroWeight_…` | `impute-cross-decade.csv`, `impute-no-valid-weight.csv` |
| 분류 | `should_classifyAsNormal_when_bmiIs22_99` 등 3건 | — |
| 예외 | `should_throwIOException_…` 등 4건 | `empty-data.csv`, `bad-columns.csv`, `bad-number.csv` |

---

## 2. 구현 요약

### 프로덕션 (`SHealth.java`)
- CSV **4컬럼 미만** → `IOException`
- 연령대에 **유효 체중 0건** → 평균 보정 스킵 (`NO_DECADE_AVERAGE`, NaN 방지)

### 테스트 (`SHealthBMITest.java`) — **6건 → 17건**
- BMI 3건, 분류 7건, 보정 3건, 예외 4건

### fixture (`src/test/resources/`)
- `impute-cross-decade.csv` — 20대만 0→(60+80)/2=70 보정, 30대(50kg) 독립
- `impute-no-valid-weight.csv` — 30대 전원 `weight==0`, 보정 불가 → BMI 0 → 저체중 100%
- `empty-data.csv`, `bad-columns.csv`, `bad-number.csv`

---

## 3. 검증

```
mvn test → BUILD SUCCESS (17 tests)
```

---

## 4. README Activity 3 커버리지

| README 항목 | 상태 | 근거 |
|-------------|------|------|
| BMI 계산 로직 TC | ✅ | `computeBmi` 3조합 (70/170, 80/180, 53.5/150.2) |
| Age 평균치 보정 TC | ✅ | 기존 `impute-weight.csv` + 연령대 분리·평균 불가 케이스 |
| 정상/저체중/과체중/비만 분류 TC | ✅ | 경계 18.5·23·25 + 구간 22.99·24.99·25.01 |
| 예외상황 TC | ✅ | 파일 없음, 헤더만, 컬럼 부족, 숫자 형식 오류 |

Activity 4(키 보정·정상 BMI 목록·전체 비율 API)는 구현하지 않았습니다.

`/commit`으로 Report·Prompting·커밋까지 진행할까요?

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


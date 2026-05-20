# Activity4 정상 BMI 목록 TDD 검증
_Exported on 5/20/2026 at 11:07:16 GMT+9 from Cursor (unknown)_

---

**User**

BMI 정상 범위 사용자 목록 조회 기능 추가 를 하고 싶어. TDD 방식으로 구현을 해야 할 것 같은데 
PCTF 프롬프트를 니가 현재 코드 베이스를 참고해서 만들어줘

---

**Cursor**

PCTF 프롬프트 작성을 위해 코드베이스와 기존 PCTF 예시를 확인합니다.


코드베이스를 기준으로 정리하면, **BMI 정상 범위 사용자 목록**은 `UserQueryService.findNormalBmiUserIds` → `SHealth.getNormalBmiUserIds()`로 **이미 구현**되어 있고, 통합 TC는 `normal-users.csv` 1건뿐입니다. README 42행을 **TDD로 명시적으로** 달성하려면 경계값·빈 목록·보정 후 분류·`UserQueryService` 단위 TC를 Red → Green → Refactor로 보강하는 PCTF가 맞습니다.

---

## PCTF란 (이 프로젝트 기준)

| 구분 | 역할 |
|------|------|
| **P**ersona | TDD·BMI 경계·SRP 구조에 익숙한 Java 실습 담당 |
| **C**ontext | Activity 4 일부, `UserQueryService`·파이프라인·기존 ~30건 테스트 |
| **T**ask | `18.5 < BMI < 23` 사용자 id 목록 — Red→Green→Refactor, 범위·금지 |
| **F**ormat | `should_…_when_…`, fixture, `mvn test`, 한국어 요약 |

**첫 메시지에 함께 붙이기:**  
`@Codebase` `@README.md` `@.cursorrules` `@.cursor/rules/java-tests.mdc` `@src/main/java/com/bestreviewer/UserQueryService.java` `@src/main/java/com/bestreviewer/BmiClassifier.java` `@src/main/java/com/bestreviewer/SHealth.java` `@src/test/java/com/bestreviewer/SHealthBMITest.java`

---

## 현재 상태 vs 이번 TDD 목표

| 항목 | 현재 | TDD로 보강할 것 |
|------|------|-----------------|
| 구현 | `UserQueryService.findNormalBmiUserIds`, `SHealth.getNormalBmiUserIds()` | 없으면 Green에서 최소 추가, 있으면 **검증 TDD** |
| 통합 TC | `should_returnNormalBmiUserIds_when_mixedCategoriesPresent` + `normal-users.csv` | 경계·빈 목록·보정 후 정상 포함 |
| 단위 TC | `UserQueryServiceTest` **없음** | CSV 없이 `List<UserRecord>` + `setBmi`로 Red 가능 |
| 경계 | `BmiClassifier` 분류 TC는 있음 | **목록 API**에서 18.5·23 **제외** 고정 |
| 출력 | `SHealthBMI`에 `normal BMI user ids` 출력 있음 | 이번 Task에서 `main` 대규모 변경 금지 |

**회귀 앵커 (`normal-users.csv`)**

```csv
id,age,weight,height
1,25,70.0,170.0
2,22,55.0,165.0
3,35,90.0,180.0
4,24,60.0,175.0
```

- id 1: BMI ≈ 24.22 → 과체중  
- id 2: ≈ 20.20 → **정상**  
- id 3: ≈ 27.78 → 비만  
- id 4: ≈ 19.59 → **정상**  
- 기대: `List.of(2, 4)` — **기존 assertion 변경 금지**

---

## 복사용 — BMI 정상 범위 사용자 목록 (TDD) PCTF

아래 블록을 **새 채팅 첫 메시지**에 그대로 붙이면 됩니다.

```markdown
## Persona (역할)
당신은 Java 21·Maven·JUnit 5 실습용 **TDD(Test-Driven Development) 전문가**입니다.
- README Activities **4** 중 **「BMI 정상 범위 사용자 목록 조회 기능 추가」**만 수행합니다.
- **반드시 Red → Green → Refactor 순서**를 따릅니다. (1) 실패하는 테스트 추가 → (2) 최소 구현으로 통과 → (3) 중복·이름만 정리, 동작 불변.
- 이번 Task에서 **수정·추가하지 않을 것**: 연령대 BMI 분포 전용 TC 대량 추가, `getOverallRatio`·`AgeDecadeStatistics` 집계 변경, `HeightImputer`/`WeightImputer` 로직 변경(보정 시나리오 TC는 **조회 결과만** 검증), 커밋·Report·Prompting·push(사용자 `/commit` 또는 명시 요청 시만).
- 기존 TC(특히 `should_returnNormalBmiUserIds_when_mixedCategoriesPresent`의 `List.of(2, 4)`)는 **값 변경 없이** 통과해야 합니다. 깨지면 회귀만 최소 수정합니다.
- 루트 `.cursorrules` 우선: 테스트명 `should_[결과]_when_[조건]`, public Javadoc, 메서드 최대 20줄, 최소 diff.

## Context (맥락)

**도메인 (README·shealth-project.mdc)**
| 항목 | 규칙 |
|------|------|
| BMI | 체중(kg) / (키(m))², 키 cm→m (`BmiClassifier.computeBmi`) |
| **정상(이번 기능)** | **18.5 < BMI < 23** (18.5 이하·23 이상은 목록에서 **제외**) |
| 분류와의 일치 | `BmiClassifier.classify`의 NORMAL과 동일 구간 (`UNDERWEIGHT_MAX=18.5`, `NORMAL_MAX=23`) |
| **이번 기능** | 로드·보정·BMI 계산 **후** 전체 사용자 중 정상 BMI 사용자 **id 목록** 반환 |
| 체중·키 보정 | `weight==0` / `height==0` → 동 연령대 평균. 보정으로 정상이 되는 경우는 **별도 통합 TC**로 검증 |

**현재 코드 (Activity 4 SRP 이후)**
- `UserQueryService.findNormalBmiUserIds(List<UserRecord>)` — `isNormalBmi`: `bmi > 18.5 && bmi < 23`
- `SHealth.getNormalBmiUserIds()` — `calculateBmi` 이후 `records`에 대해 위 서비스 위임
- `SHealth.calculateBmi`: `load` → `WeightImputer` → `HeightImputer` → BMI → `AgeDecadeStatistics.aggregate`
- `SHealthBMITest.should_returnNormalBmiUserIds_when_mixedCategoriesPresent` — `normal-users.csv` → id `2, 4`
- **갭**: 경계(18.5·23)·정상 0명·미로드·보정 후 정상 포함·`UserQueryService` **단위** TC 없음

**목록 API 규칙 (테스트로 고정할 것)**
- 반환 타입: `List<Integer>` (id만; 순서는 **CSV 로드 순** = `records` 순회 순)
- `calculateBmi` 호출 전 → **빈 리스트** (`records`가 비어 있음)
- 정상 사용자 0명 → 빈 리스트 (크기 0)
- BMI **정확히 18.5** 또는 **23.0** → 목록에 **포함하지 않음** (엄격 부등호)
- 경계 근처: 18.51·22.99 등은 포함 가능 (fixture 또는 `setBmi`로 고정)

**제약**
- fixture: `src/test/resources/`, 헤더 `id,age,weight,height`
- 통합 TC: `SHealth` + `resourcePath(...)` 패턴 유지
- 단위 TC: `UserQueryServiceTest` — `List<UserRecord>`에 `setBmi` 후 `findNormalBmiUserIds` (CSV·파이프라인 없이 Red 가능)
- Mockito 불필요(순수 도메인)

## Task (할 일) — TDD 단계

### Phase 0 — 현황 확인 (코드 수정 없음)
- `UserQueryService.findNormalBmiUserIds`, `SHealth.getNormalBmiUserIds` 존재 여부 확인
- 이미 구현되어 있으면 **「검증 TDD」**: 테스트만 먼저 추가 → 실패 시 최소 수정(Green) → Refactor
- `mvn test` 기준선(전체 통과 건수) 기록

### Phase 1 — Red: 실패하는 테스트 추가
프로덕션 수정 **전에** 아래 TC를 추가합니다. 구현이 있으면 대부분 Green이지만, **갭 TC는 Red 가능**합니다.

| # | 테스트 의도 | 클래스 | 메서드명 예시 |
|---|-------------|--------|----------------|
| 1 | 혼합 범주 → 정상 id만 | `SHealthBMITest` | `should_returnNormalBmiUserIds_when_mixedCategoriesPresent` (**기존 유지**) |
| 2 | BMI 18.5·23.0은 제외, 18.51·22.99는 포함 | `UserQueryServiceTest` | `should_excludeBoundaryBmis_when_at18_5And23` |
| 3 | 모두 저체중/과체중/비만 | `SHealthBMITest` | `should_returnEmptyList_when_noNormalUsers` |
| 4 | `calculateBmi` 전 조회 | `SHealthBMITest` | `should_returnEmptyList_when_dataNotLoaded` |
| 5 | (선택) `weight==0` 보정 후 정상 → id 포함 | `SHealthBMITest` | `should_includeUser_when_weightImputedToNormalBmi` |

**fixture 신규 예시**

`normal-boundary.csv` — 경계만 검증(단위 TC에서는 `setBmi`로 대체 가능):
```csv
id,age,weight,height
10,25,53.47,170.0
11,25,53.50,170.0
12,25,66.47,170.0
13,25,66.50,170.0
```
(각 행 BMI를 `BmiClassifier.computeBmi`로 계산해 18.5/23.0/정상 구간 여부를 확정한 뒤, **통합 TC** 기대 id 목록을 assert)

`normal-none.csv` — 4명 모두 정상 범위 밖:
```csv
id,age,weight,height
1,25,45.0,170.0
2,26,80.0,170.0
3,27,90.0,170.0
4,28,70.0,170.0
```
→ `getNormalBmiUserIds()` → `List.of()` (빈 리스트)

`normal-after-weight-impute.csv` — (선택) 20대 2명, id=2만 `weight=0`, 동대 평균 보정 후 NORMAL:
```csv
id,age,weight,height
1,25,60.0,175.0
2,25,0.0,175.0
```
(20대 평균 체중·BMI를 손계산 후 id=2 포함 여부 assert)

### Phase 2 — Green: 최소 구현
- `UserQueryService` / `SHealth.getNormalBmiUserIds`가 없을 때만 추가
- 이미 있으면 실패 TC에 맞게 **최소 수정**(경계 조건 버그 시 `isNormalBmi`만)
- `SHealth` 파이프라인 순서·다른 public API 시그니처 변경 금지

### Phase 3 — Refactor
- `isNormalBmi`와 `BmiClassifier.classify(record.getBmi()) == NORMAL` 중복이 있으면 **한쪽으로 통일** (동작 불변)
- `UserQueryService` 20줄 초과 시 private 메서드 분리만
- `mvn test` 전체 통과 보고

## Format (결과 형식)
1. **한국어** 요약: Red/Green/Refactor 각 단계에서 한 일
2. 추가·수정 파일 목록 (`UserQueryServiceTest.java`, fixture 이름)
3. `mvn test` 결과(통과 건수)
4. **완료 체크리스트**
   - [ ] 정상 구간 `18.5 < BMI < 23`만 id 반환
   - [ ] 경계 18.5·23 제외 TC
   - [ ] 빈 목록(미로드·정상 0명) TC
   - [ ] `UserQueryService` 단위 TC ≥ 1
   - [ ] `normal-users.csv` 회귀(`2, 4`) 유지
   - [ ] 연령대 분포·전체 비율·키 보정 TC 미변경(회귀만 허용)
```

---

## 단계별 “짧은 PCTF” (한 번에 부담될 때)

**1단계 — 단위(Red→Green)**  
> Activity 4 PCTF 중 **정상 BMI 목록 1단계만**: `UserQueryServiceTest` 신규. 경계 18.5·23 제외, 18.51·22.99 포함. `SHealth`·fixture·다른 API 금지. `mvn test`.

**2단계 — 통합**  
> 1단계 완료 가정. `SHealthBMITest`에 빈 목록·`normal-none.csv`·미로드 TC 추가. `should_returnNormalBmiUserIds_when_mixedCategoriesPresent` assertion 변경 금지.

**3단계 — 보정(선택)**  
> 2단계 완료 가정. `weight==0` 보정 후 정상 id 포함 통합 TC 1건 + fixture. Imputer 구현 변경 금지.

---

## 사용 팁

1. 새 채팅 → `@Codebase` + 위 **전체 PCTF** + `@UserQueryService.java` `@SHealthBMITest.java`
2. 구현이 이미 있으면 에이전트에게 **「검증 TDD만, 프로덕션 변경 없이 TC부터」**라고 한 줄 추가하면 08·09번과 같은 흐름이 됩니다.
3. `normal-users.csv`의 `2, 4`는 손계산과 일치하므로 PCTF에 넣어 두면 임의 변경을 줄일 수 있습니다.

---

원하시면 위 내용을 `Prompting/10_Activity4-정상-BMI-목록-TDD-Prompting.md`로 저장하거나, **처음부터 Green이 없는 순수 Red 시나리오**(구현을 일부러 지운 뒤 TDD)용 PCTF 변형도 만들어 드리겠습니다.

---

**User**

## Persona (역할)
당신은 Java 21·Maven·JUnit 5 실습용 **TDD(Test-Driven Development) 전문가**입니다.
- README Activities **4** 중 **「BMI 정상 범위 사용자 목록 조회 기능 추가」**만 수행합니다.
- **반드시 Red → Green → Refactor 순서**를 따릅니다. (1) 실패하는 테스트 추가 → (2) 최소 구현으로 통과 → (3) 중복·이름만 정리, 동작 불변.
- 이번 Task에서 **수정·추가하지 않을 것**: 연령대 BMI 분포 전용 TC 대량 추가, `getOverallRatio`·`AgeDecadeStatistics` 집계 변경, `HeightImputer`/`WeightImputer` 로직 변경(보정 시나리오 TC는 **조회 결과만** 검증), 커밋·Report·Prompting·push(사용자 `/commit` 또는 명시 요청 시만).
- 기존 TC(특히 `should_returnNormalBmiUserIds_when_mixedCategoriesPresent`의 `List.of(2, 4)`)는 **값 변경 없이** 통과해야 합니다. 깨지면 회귀만 최소 수정합니다.
- 루트 `.cursorrules` 우선: 테스트명 `should_[결과]_when_[조건]`, public Javadoc, 메서드 최대 20줄, 최소 diff.

## Context (맥락)

**도메인 (README·shealth-project.mdc)**
| 항목 | 규칙 |
|------|------|
| BMI | 체중(kg) / (키(m))², 키 cm→m (`BmiClassifier.computeBmi`) |
| **정상(이번 기능)** | **18.5 < BMI < 23** (18.5 이하·23 이상은 목록에서 **제외**) |
| 분류와의 일치 | `BmiClassifier.classify`의 NORMAL과 동일 구간 (`UNDERWEIGHT_MAX=18.5`, `NORMAL_MAX=23`) |
| **이번 기능** | 로드·보정·BMI 계산 **후** 전체 사용자 중 정상 BMI 사용자 **id 목록** 반환 |
| 체중·키 보정 | `weight==0` / `height==0` → 동 연령대 평균. 보정으로 정상이 되는 경우는 **별도 통합 TC**로 검증 |

**현재 코드 (Activity 4 SRP 이후)**
- `UserQueryService.findNormalBmiUserIds(List<UserRecord>)` — `isNormalBmi`: `bmi > 18.5 && bmi < 23`
- `SHealth.getNormalBmiUserIds()` — `calculateBmi` 이후 `records`에 대해 위 서비스 위임
- `SHealth.calculateBmi`: `load` → `WeightImputer` → `HeightImputer` → BMI → `AgeDecadeStatistics.aggregate`
- `SHealthBMITest.should_returnNormalBmiUserIds_when_mixedCategoriesPresent` — `normal-users.csv` → id `2, 4`
- **갭**: 경계(18.5·23)·정상 0명·미로드·보정 후 정상 포함·`UserQueryService` **단위** TC 없음

**목록 API 규칙 (테스트로 고정할 것)**
- 반환 타입: `List<Integer>` (id만; 순서는 **CSV 로드 순** = `records` 순회 순)
- `calculateBmi` 호출 전 → **빈 리스트** (`records`가 비어 있음)
- 정상 사용자 0명 → 빈 리스트 (크기 0)
- BMI **정확히 18.5** 또는 **23.0** → 목록에 **포함하지 않음** (엄격 부등호)
- 경계 근처: 18.51·22.99 등은 포함 가능 (fixture 또는 `setBmi`로 고정)

**제약**
- fixture: `src/test/resources/`, 헤더 `id,age,weight,height`
- 통합 TC: `SHealth` + `resourcePath(...)` 패턴 유지
- 단위 TC: `UserQueryServiceTest` — `List<UserRecord>`에 `setBmi` 후 `findNormalBmiUserIds` (CSV·파이프라인 없이 Red 가능)
- Mockito 불필요(순수 도메인)

## Task (할 일) — TDD 단계

### Phase 0 — 현황 확인 (코드 수정 없음)
- `UserQueryService.findNormalBmiUserIds`, `SHealth.getNormalBmiUserIds` 존재 여부 확인
- 이미 구현되어 있으면 **「검증 TDD」**: 테스트만 먼저 추가 → 실패 시 최소 수정(Green) → Refactor
- `mvn test` 기준선(전체 통과 건수) 기록

### Phase 1 — Red: 실패하는 테스트 추가
프로덕션 수정 **전에** 아래 TC를 추가합니다. 구현이 있으면 대부분 Green이지만, **갭 TC는 Red 가능**합니다.

| # | 테스트 의도 | 클래스 | 메서드명 예시 |
|---|-------------|--------|----------------|
| 1 | 혼합 범주 → 정상 id만 | `SHealthBMITest` | `should_returnNormalBmiUserIds_when_mixedCategoriesPresent` (**기존 유지**) |
| 2 | BMI 18.5·23.0은 제외, 18.51·22.99는 포함 | `UserQueryServiceTest` | `should_excludeBoundaryBmis_when_at18_5And23` |
| 3 | 모두 저체중/과체중/비만 | `SHealthBMITest` | `should_returnEmptyList_when_noNormalUsers` |
| 4 | `calculateBmi` 전 조회 | `SHealthBMITest` | `should_returnEmptyList_when_dataNotLoaded` |
| 5 | (선택) `weight==0` 보정 후 정상 → id 포함 | `SHealthBMITest` | `should_includeUser_when_weightImputedToNormalBmi` |

**fixture 신규 예시**

`normal-boundary.csv` — 경계만 검증(단위 TC에서는 `setBmi`로 대체 가능):
```csv
id,age,weight,height
10,25,53.47,170.0
11,25,53.50,170.0
12,25,66.47,170.0
13,25,66.50,170.0

---

**Cursor**

PCTF에 따라 검증 TDD로 진행합니다. Phase 0 확인 후 테스트·fixture를 추가합니다.


Phase 1: 테스트와 fixture 추가 중입니다.


**변경 이유:** README Activity 4 「BMI 정상 범위 사용자 목록」을 **검증 TDD**로 고정했습니다. 구현은 이미 있어 프로덕션은 `isNormalBmi`를 `BmiClassifier`와 일치시키는 Refactor만 했습니다.

**영향:** `getNormalBmiUserIds()` / `findNormalBmiUserIds()` 동작(18.5·23 제외, 로드 순 id) · 체중 보정 후 목록 · 기존 `normal-users.csv` 회귀(`2, 4`).

---

## TDD 요약

| 단계 | 내용 |
|------|------|
| **Phase 0** | `UserQueryService`·`SHealth.getNormalBmiUserIds()` 확인, 기준선 **29건** 통과 |
| **Phase 1 (Red→Green)** | 갭 TC 4건 + fixture 2개 추가 → 기존 구현으로 **전부 Green** |
| **Phase 3 (Refactor)** | `isNormalBmi` → `BmiClassifier.classify(bmi) == NORMAL` (경계 중복 제거, 동작 동일) |

---

## 추가·수정 파일

| 파일 | 내용 |
|------|------|
| `UserQueryServiceTest.java` | 경계 18.5·23 제외, 18.51·22.99 포함 |
| `SHealthBMITest.java` | 미로드 빈 목록, 정상 0명, 보정 후 정상 포함 |
| `normal-none.csv` | 4명 모두 정상 범위 밖 |
| `normal-after-weight-impute.csv` | `weight==0` → 동 연령대 평균 후 id 1·2 정상 |
| `UserQueryService.java` | `isNormalBmi` Refactor만 |

---

## `mvn test`

- **33건** 전부 통과 (29 → +4)
- `should_returnNormalBmiUserIds_when_mixedCategoriesPresent` → `List.of(2, 4)` **유지**

---

## 완료 체크리스트

- [x] 정상 구간 `18.5 < BMI < 23`만 id 반환  
- [x] 경계 18.5·23 제외 TC  
- [x] 빈 목록(미로드·정상 0명) TC  
- [x] `UserQueryService` 단위 TC ≥ 1  
- [x] `normal-users.csv` 회귀 유지  
- [x] 연령대 분포·전체 비율·Imputer 로직 미변경

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

변경 내용 모두 커밋해줘
--- End Command ---
</cursor_commands>
<user_query>
/commit 변경 내용 모두 커밋해줘
</user_query>


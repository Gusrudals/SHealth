# @Codebase BMI 코드 스멜 분석 및 docs 문서화

_Exported on 5/20/2026 at 15:30:00 GMT+9 from Cursor (unknown)_

**User**

@Codebase 이 BMI 프로젝트의 코드 스멜을 찾아줘:- Magic Number (18.5, 23, 25, 0 등 하드코딩)- Long Method (20줄 이상 메서드)- Duplicated Code (중복 로직)- God Class (너무 많은 책임을 가진 클래스)
심각도 순으로 표 형식으로 정리해줘.

위의 프롬프트를 현재 코드베이스에 맞춰서 PCTF 방식으로 바꿔줘봐

---

**Cursor**

코드베이스를 분석하고, `SHealth.java`·`SHealthBMI.java` 기준으로 코드 스멜 표와 PCTF 재작성 프롬프트를 제공했다.

**코드 스멜 분석표 (요약)**

| 심각도 | 스멜 | 위치 |
|--------|------|------|
| 높음 | God Class | `SHealth` 전체 |
| 높음 | Long Method | `calculateBmi` 약 99줄 |
| 높음 | Duplicated Code | `getBmiRatio` 24분기, 108~138행 연령대 할당 |
| 중간 | Magic Number / 중복 / Long Method | BMI 경계, type 코드, 연령대 루프 |
| 중간 | 도메인 불일치 | BMI=25 미분류 |
| 낮음 | `SHealthBMI.main` printf 반복 |

**PCTF 프롬프트** — Persona(리뷰어·README 도메인 준수), Context(Maven Java 21, shealth.dat, `.cursorrules` 20줄), Task(4종 스멜), Format(한국어·영문 식별자·심각도 표·분석만) 구조로 제시.

---

**User**

## Persona (역할)
당신은 Java 클린코드·리팩토링에 익숙한 코드 리뷰어입니다.
Samsung Health BMI 실습(SHealth)의 **의도된 코드 스멜**을 식별하되,
리팩토링 시 깨지면 안 되는 도메인 규칙(BMI 공식, 경계값, weight==0 보정)을 README와 일치시켜 설명합니다.
## Context (맥락)
(... PCTF 전문: 프로젝트 경로, 도메인, 품질 기준, Activity 1 분석만 ...)
## Task (할 일)
(... Magic Number, Long Method, Duplicated Code, God Class ...)
## Format (출력 형식)
(... 표 열 정의, 심각도 내림차순, 코드 수정 금지 ...)

---

**Cursor**

PCTF 프롬프트에 따라 `SHealth`·`SHealthBMI` 메인 소스를 재검토하고 심각도 내림차순 표를 제출했다.

- `SHealthBMITest`: `failedTest()` → `fail()`만 호출 — **미구현**
- 15항목 표: God Class, `calculateBmi` 99줄, `getBmiRatio` 24분기, 24필드 중복, Magic Number, BMI=25 도메인 불일치 등
- `split`은 20줄 이내로 이번 4종 주 대상 아님
- 분석만 수행, 코드·커밋·Report/Prompting 미생성

---

**User**

코드 스멜의 분석한 결과물을 docs 폴더에 파일로 잘 정리해서 만들어줘

---

**Cursor**

`docs/` 폴더에 분석 결과를 문서화했다.

| 파일 | 역할 |
|------|------|
| `docs/README.md` | 문서 인덱스 |
| `docs/code-smell-analysis.md` | 요약·종합표·유형별 상세·도메인 불일치·테스트 현황·Activity 2 순서·PCTF 부록 |

구성: 8개 섹션(요약, 범위·도메인, 종합표, 상세, BMI=25, 테스트, 리팩토링 순서, 부록). 코드 인용은 `SHealth.java` 행 번호 포함.

---

**User**

/commit 변경 내용 모두 커밋해줘

---

**Cursor**

커밋 워크플로(Report → Prompting → Git) 진행: `Report/03_…`, `Prompting/03_…` 작성 후 `docs/` 및 Report/Prompting 스테이징·커밋 (`target/` 제외).

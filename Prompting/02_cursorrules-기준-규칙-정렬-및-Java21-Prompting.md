# @.cursorrules.md 을 내가 만들었는데 rules 폴더의 rule 파일들과 비교

_Exported on 5/20/2026 at 14:30:00 GMT+9 from Cursor (unknown)_

**User**

@.cursorrules.md 을 내가 만들었는데 rules 폴더의 rule 파일들과 비교해서 기존 rules 파일들을 그대로 유지하는게 좋을지? 아니면 삭제해도 되는지? 고쳐서 사용하는게 나을지 판단해줘. 단 @.cursorrules.md 파일을 수정하지 마

---

**Cursor**

## 결론

**`.cursor/rules/*.mdc`는 그대로 두는 것이 맞고, 삭제하면 안 됩니다.**

`.cursorrules.md`는 **단독으로 Cursor에 자동 적용되는 규칙 소스가 아니며**, 내용도 이 프로젝트와 **일부 충돌**합니다. 지금 구조(`.mdc` + `AGENTS.md`)를 유지하고, `.cursorrules.md`에 있는 **일반 코딩 규칙만 골라** `.mdc` 쪽에 반영하는 방향이 가장 낫습니다.

## 역할 비교

| 구분 | `.cursorrules.md` | `.cursor/rules/*.mdc` |
|------|-------------------|------------------------|
| 적용 | `.cursorrules`가 아니라 **자동 로딩 약함** | `alwaysApply` / `globs`로 조건부·항상 적용 |
| 내용 | 범용 코딩 체크리스트 | SHealth 도메인·커밋·한국어 응답 등 |

## 충돌

- **Java 21** vs `shealth-project.mdc`·`pom.xml`의 **Java 8**
- **Javadoc·20줄** — `.mdc`에 없음
- 테스트명 `should_…` vs 한글 메서드명 허용

## 권장

- `.mdc` **삭제 비추천**, `.cursorrules.md` **참고용 보관** 또는 좋은 항목만 `.mdc`에 이식
- `.cursorrules.md`만으로 단일화 **비추천**

---

**User**

@.cursorrules.md 의 내용이 더 우선시되어야 해. 이점을 고려해서 다시 판단해줘

---

**Cursor**

## 재판단 (`.cursorrules.md` 최우선)

**`.mdc`는 삭제하지 말고**, 충돌 시 **`.cursorrules.md`가 이기도록** `.mdc`·`AGENTS.md`를 맞추는 것이 맞습니다.

**우선순위:** 1 `.cursorrules.md` → 2 `.mdc` → 3 `pom.xml` / `README`

파일명만 바꿔서는 “항상 이긴다”가 보장되지 않음 — **문서 계층을 바꿔야** 합니다.

| 항목 | `.cursorrules.md` 우선 시 |
|------|---------------------------|
| Java 21 | `shealth-project.mdc`·`pom` 정렬 필요 |
| Javadoc·20줄·`should_…` | `.mdc`에 반영 |
| BMI·커밋 | `.mdc`만 있음 → **유지** |

**시나리오:** `.mdc` 유지 + 상충 문장 수정 + `AGENTS.md` 최우선 선언이 **추천**.

---

**User**

@.cursorrules 파일명을 이렇게 바꾸면?

---

**Cursor**

**`.cursorrules`로 바꾼 것은 잘 한 선택** — Cursor가 인식하는 레거시 규칙 파일명입니다.

- **`.mdc` 삭제 불필요** — 둘 다 로드되는 구조가 일반적
- **상충은 여전히** (Java 21 vs 8, 테스트명) → `AGENTS.md`·`.mdc` 정렬 필요
- `AGENTS.md`는 아직 「`.mdc` 대신 `.cursorrules`」라 **어긋남**

파일명 변경 = 올바른 방향, **우선순위 완성 = `AGENTS.md` + `.mdc` 정렬**.

---

**User**

그래 @.cursorrules 을 기준으로 rules 파일이 다른 부분이 있다면 rules 파일의 내용을 수정해줘

---

**Cursor**

`.cursorrules`와 맞추어 수정했습니다.

| 파일 | 변경 |
|------|------|
| `shealth-project.mdc` | Java 21, Mockito, `.cursorrules` 최우선 |
| `java-refactoring.mdc` | Javadoc·20줄·매직 넘버 섹션 |
| `java-tests.mdc` | `should_[결과]_when_[조건]`만, 한글명 제거 |
| `AGENTS.md` | 우선순위·`.cursorrules` 표 추가 |

`commit-workflow.mdc` / `response-style.mdc`는 변경 없음. `pom.xml`은 아직 Java 8.

---

**User**

pom.xml 도 수정해줘

---

**Cursor**

`pom.xml`: `maven.compiler.release` 21, `UTF-8`. `mvn test` 컴파일 OK. `SHealthBMITest.failedTest`의 `fail()` 1건은 기존 플레이스홀더로 pom과 무관.

이제 `.cursorrules` · `.mdc` · `pom.xml` Java 버전 일치.

---

**User**

@AGENTS.md 파을은 왜 만든거지? @.cursorrules , rules 폴더의 mdc 파일들의 관계에 대해 설명해줘

---

**Cursor**

`AGENTS.md`는 커서룰·커밋 워크플로 구성(Report 01) 때 만든 **에이전트용 목차·안내서** — 규칙 본문이 아님.

| | `.cursorrules` | `.mdc` | `AGENTS.md` |
|--|----------------|--------|-------------|
| 역할 | 공통 코딩 헌법 | 도메인·경로·커밋 | 맵·우선순위 |
| 삭제 시 | 공통 규칙 약화 | 도메인·커밋 상실 | 맵만 사라짐 |

**관계:** `.cursorrules` 1순위, `.mdc` 보완, `AGENTS.md`는 둘을 연결하는 안내판.

---

**User**

@.cursorrules 의 내용을 @AGENTS.md 에 다 옮기고 @.cursorrules 파일을 삭제하고 @AGENTS.md 파일 내에서 rule 폴더의 파일 활용에 대한 내용을 명시하면 되는 것 아니야?

---

**Cursor**

**AGENTS.md에만 옮기면 규칙 강도가 약해짐** — Cursor는 `.cursorrules`/`.mdc`와 `AGENTS.md`를 다르게 취급.

**권장:** 코딩 규칙 → `alwaysApply` `.mdc`(예: `coding-standards.mdc`) 후 `.cursorrules` 삭제 가능. `AGENTS.md` = 맵·mdc 활용법.

`.mdc` 삭제·대체는 안 됨 (BMI·커밋).

---

**User**

그럼 반대로 @AGENTS.md 의 내용을 @.cursorrules 에 옮기면?

---

**Cursor**

**AGENTS → `.cursorrules`는 AGENTS만 흡수하는 것보다 유리**하지만:

- 맵·표도 매 턴 토큰 증가
- 「1순위: `.cursorrules`」는 **문구 수정** 필요 (자기 참조)
- **`.mdc`는 그대로 필요**

**장기 권장:** `.cursorrules`(코딩) + `AGENTS.md`(맵) 분리. 합쳐도 되나 규칙 본문은 `.mdc`에 두는 편이 Cursor스러움.

---

**User**

/commit 변경 내용을 모두 커밋해줘

---

**Cursor**

Report → Prompting → Git 순서로 커밋 진행.

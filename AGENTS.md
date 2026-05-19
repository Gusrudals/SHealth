# SHealth BMI — Agent 가이드

## 규칙 우선순위

1. **`.cursorrules`** (루트) — 기술 스택·코딩·테스트 네이밍 (**최우선**)
2. **`.cursor/rules/*.mdc`** — SHealth 도메인·경로별 Java/테스트·커밋·응답 (1번과 충돌 시 `.cursorrules` 우선)
3. **`pom.xml` / `README`** — 빌드·실습 사실

## 규칙 파일

| 규칙 | 적용 |
|------|------|
| `.cursorrules` | 항상 — Java 21, JUnit 5 + Mockito, Javadoc, 20줄, `should_…_when_…` |
| `shealth-project.mdc` | 항상 — 도메인·실습 목표 |
| `java-refactoring.mdc` | `src/main/java/**` |
| `java-tests.mdc` | `src/test/**` |
| `commit-workflow.mdc` | **커밋/commit** 요청 시 — Report → Prompting → Git |
| `.cursor/commands/commit.md` | 채팅에서 **`/commit`** 실행 시 위와 동일 절차 |
| `response-style.mdc` | 항상 — 한국어 응답·최소 diff |

**커밋**: 명시 요청 시에만 `commit-workflow.mdc`의 1→2→3 순서를 반드시 수행합니다.

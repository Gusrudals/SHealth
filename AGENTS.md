# SHealth BMI — Agent 가이드

이 프로젝트는 [`.cursor/rules/`](.cursor/rules/) 의 `.mdc` 규칙을 따릅니다. (레거시 `.cursorrules` 대신 사용)

| 규칙 | 적용 |
|------|------|
| `shealth-project.mdc` | 항상 — 도메인·실습 목표 |
| `java-refactoring.mdc` | `src/main/java/**` |
| `java-tests.mdc` | `src/test/**` |
| `commit-workflow.mdc` | **커밋/commit** 요청 시 — Report → Prompting → Git |
| `.cursor/commands/commit.md` | 채팅에서 **`/commit`** 실행 시 위와 동일 절차 |
| `response-style.mdc` | 항상 — 한국어 응답·최소 diff |

**커밋**: 명시 요청 시에만 `commit-workflow.mdc`의 1→2→3 순서를 반드시 수행합니다.

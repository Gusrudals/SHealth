# 작업 보고서: `.cursorrules` 기준 규칙 정렬 및 Java 21

**일자**: 2026-05-20  
**브랜치**: refactoring  
**작업 번호**: 02

## 요약

루트 `.cursorrules`를 최우선 코딩·스택 규칙으로 두고, `.cursor/rules/*.mdc`·`AGENTS.md`·`pom.xml`을 그에 맞게 정렬했다. `.cursorrules` 파일명 정리(`.cursorrules.md` → `.cursorrules`)와 규칙·`AGENTS.md` 관계에 대한 설계 논의를 포함한다.

## 수행 내용

### 1. 규칙 정렬 (`.cursorrules` 최우선)

| 파일 | 변경 |
|------|------|
| `shealth-project.mdc` | Java 8 → **21**, Mockito 명시, `.cursorrules` 최우선 참조 |
| `java-refactoring.mdc` | Javadoc·20줄·매직 넘버 섹션, `.cursorrules` 우선 |
| `java-tests.mdc` | `should_[결과]_when_[조건]`만 허용, Mockito, 예시 메서드명 변경 |
| `AGENTS.md` | 규칙 우선순위(1 `.cursorrules` → 2 `.mdc` → 3 pom/README) 및 파일 표 |

### 2. 빌드

- `pom.xml`: `maven.compiler.release` **21**, `UTF-8` 인코딩

### 3. 신규·추가

- 루트 **`.cursorrules`** (기술 스택·코딩 규칙 10줄) — Git 추적 대상으로 추가

### 4. 세션 내 논의 (코드 미반영)

- `.cursorrules` vs `AGENTS.md` 통합·삭제 방안 검토 → **현 구조 유지** 권장 (규칙 본문은 `.cursorrules`/`.mdc`, `AGENTS.md`는 맵)
- `AGENTS.md` 내용을 `.cursorrules`로만 옮기는 방안 → 가능하나 토큰·역할 혼합 트레이드오프 설명

## 검증

- `mvn test`: 컴파일·실행 OK (기존 `SHealthBMITest.failedTest` 플레이스홀더 `fail()` 1건 실패는 변경 전과 동일)

## 변경 파일 (커밋 대상)

- `.cursorrules` (신규)
- `.cursor/rules/shealth-project.mdc`, `java-refactoring.mdc`, `java-tests.mdc`
- `AGENTS.md`, `pom.xml`
- `Report/02_…md`, `Prompting/02_…-Prompting.md`

## 제외

- `target/` (빌드 산출물)

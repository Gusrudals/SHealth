# 작업 보고서: Activity 5 docs 회고·발표자료 및 스멜 문서

**일자**: 2026-05-20  
**브랜치**: feature  
**작업 번호**: 12

## 요약

README Activity 5(회고·발표)에 맞춰 `docs/`에 리팩토링 이후 스멜 분석·개발 순서 비교 문서를 신규 추가하고, 종합 발표용 HTML 슬라이드(12장)를 작성했다. 기존 `code-smell-analysis.md`(리팩토링 전)는 수정하지 않았다.

## 수행 내용

### 1. 코드 스멜 분석 (리팩토링 이후) — 신규

| 파일 | 역할 |
|------|------|
| `docs/code-smell-analysis-after-refactoring.md` | SRP 분리 후 11클래스 기준 스멜 표·Before/After·잔여 Duplicated Code |

- 높음 스멜(God Class, Long Method, BMI=25) 해소·중간 Duplicated Code(Imputer, `toPercentRatios`) 정리

### 2. 개발 순서 영향 — 신규

| 파일 | 역할 |
|------|------|
| `docs/refactoring-order-and-impact.md` | 실제 순서(2→3→4) vs 기능 선행 가정 비교·Activity 5 회고 포인트 |

### 3. Activity 5 발표 HTML — 신규

| 파일 | 역할 |
|------|------|
| `docs/presentation/activity5-retrospective.html` | 12슬라이드(목표 달성·Before/After·AI·TC·클린코드)·키보드/버튼 탐색 |

### 4. 문서 인덱스

| 파일 | 변경 |
|------|------|
| `docs/README.md` | 위 3종 + 발표 HTML 링크 추가 |

## 검증

- `code-smell-analysis.md` — git 원본 유지(리팩토링 전 분석)
- HTML: 브라우저 로컬 오픈·슬라이드 네비게이션 수동 확인
- `mvn test` — 기존 42건 통과(문서-only, 소스 미변경)

## 변경 파일 (커밋 대상)

- `docs/code-smell-analysis-after-refactoring.md` (신규)
- `docs/refactoring-order-and-impact.md` (신규)
- `docs/presentation/activity5-retrospective.html` (신규)
- `docs/README.md`
- `Report/12_Activity5-docs-회고-발표자료-및-스멜문서.md`
- `Prompting/12_Activity5-docs-회고-발표자료-및-스멜문서-Prompting.md`

## 제외

- `target/`, `__pycache__/`, `*.pyc`

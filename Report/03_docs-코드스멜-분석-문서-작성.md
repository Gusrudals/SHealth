# 작업 보고서: docs 코드 스멜 분석 문서 작성

**일자**: 2026-05-20  
**브랜치**: refactoring  
**작업 번호**: 03

## 요약

README Activity 1(코드 스멜 분석) 결과를 `docs/`에 정리했다. 대화에서 PCTF 프롬프트로 `SHealth`·`SHealthBMI`를 검토한 내용을 보고서 형태로 문서화했으며, 리팩토링·테스트 연계 섹션을 포함했다.

## 수행 내용

### 1. 코드 스멜 분석 (세션)

| 스멜 | 핵심 발견 |
|------|-----------|
| God Class | `SHealth`에 I/O·보정·BMI·통계·조회 집중 |
| Long Method | `calculateBmi` 약 99줄, `getBmiRatio` 약 51줄 |
| Duplicated Code | `getBmiRatio` 24분기, 연령대별 필드·할당 6회 반복 |
| Magic Number | BMI 18.5/23/25, type 100~400, 배열 10000 등 |
| 도메인 | BMI=25 미분류 (README ≥25 비만과 불일치) |

### 2. PCTF 프롬프트

- Persona / Context / Task / Format 구조로 Activity 1용 재사용 프롬프트 작성·실행
- 분석만 수행(코드 수정 없음) 원칙 반영

### 3. 문서 추가 (`docs/`)

| 파일 | 역할 |
|------|------|
| `docs/README.md` | 문서 인덱스·관련 링크 |
| `docs/code-smell-analysis.md` | 종합표·유형별 상세·도메인 불일치·Activity 2~3 연계 |

## 검증

- 소스 코드 변경 없음 (`SHealth.java` 등 미수정)
- `mvn test` 미실행 (문서-only 변경)

## 변경 파일 (커밋 대상)

- `docs/README.md`
- `docs/code-smell-analysis.md`
- `Report/03_docs-코드스멜-분석-문서-작성.md`
- `Prompting/03_docs-코드스멜-분석-문서-작성-Prompting.md`

## 제외

- `target/` (빌드 산출물)

## 다음 단계 (권장)

- Activity 2: 상수·enum, `calculateBmi` 분해, God Class 분리
- Activity 3: BMI 경계(특히 25) TC, `SHealthBMITest` 플레이스홀더 제거

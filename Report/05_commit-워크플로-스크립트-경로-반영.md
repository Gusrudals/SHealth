# Report: commit 워크플로 스크립트 경로 반영

## 작업 요약

커밋 명령(`/commit`) 및 `commit-workflow.mdc`에서 Prompting 저장 방식을 수동 Export Transcript 작성에서 **스크립트 기반 export**로 되돌리고, 스크립트 실제 위치에 맞게 경로를 정리했다.

## 변경 내용

| 파일 | 내용 |
|------|------|
| `.cursor/commands/commit.md` | `export-cursor-transcript.py` 사용 안내, JSONL 식별 규칙, 실행 예시 |
| `.cursor/rules/commit-workflow.mdc` | `commit.md`와 동일한 2단계(Prompting) 절차 |
| `.cursor/commands/scripts/export-cursor-transcript.py` | transcript JSONL → Export Transcript MD 변환 (신규·이전 `scripts/`에서 이동) |

## 경로 정리

- **이전 문서**: `scripts/export-cursor-transcript.py` (루트) + fallback
- **현재 표준**: `.cursor/commands/scripts/export-cursor-transcript.py`
- 실행 시 프로젝트 루트를 cwd로 두고 `-o Prompting/…` 지정

## JSONL 식별 규칙 (문서화)

- `--project-slug`만으로 최근 파일 선택 금지
- `--conversation-id` 우선 (현재 대화 UUID)
- 실패 시 사용자 확인 후 진행, 잘못된 export 후 커밋 금지

## 테스트

- `python .cursor/commands/scripts/export-cursor-transcript.py --help` 정상
- `--conversation-id 0cd57115-…` 로 Prompting export 후 턴 형식 확인
- Report·Prompting·규칙 파일 스테이징 후 커밋

## 영향

- BMI·연령대·분류 등 **애플리케이션 코드 동작 변경 없음**
- 커밋 시 Prompting 자동 생성 절차만 변경

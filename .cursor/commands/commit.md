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

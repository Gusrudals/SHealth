# commit-워크플로-스크립트-경로-반영
_Exported on 5/20/2026 at 09:44:12 GMT+9 from Cursor (unknown)_

---

**User**

@.cursor/commands/commit.md 의 내용을 보면 script 파일 사용 금지로 명시되어 있잖아. 이를 다시 script 사용으로 변경하고 싶어. 
하기는 기존에 script 를 사용해서 프롬프트 파일을 생성하던 가이드야 이를 참조해서 @.cursor/commands/commit.md 의 내용을 갱신해줘

### 2. 현재 대화 Prompting 저장 (1번 직후, 커밋 전 필수)

- **`scripts/export-cursor-transcript.py`** 로 **지금 이 대화창**에 해당하는 JSONL만 읽어 Export Transcript 형식 MD로 저장합니다.
- **저장**: `Prompting/{번호}_{작업 title명}-Prompting.md` (1번과 동일 `{번호}_{작업 title명}`).

**현재 대화 JSONL 식별 (필수)**

- **금지**: `--project-slug` 만으로 “가장 최근” JSONL을 고르지 않습니다 (다른 채팅이 선택될 수 있음).
- **우선**: Cursor가 제공하는 **현재 대화 ID** →  
  `~/.cursor/projects/e-DEV-GildedRose-04/agent-transcripts/<id>/<id>.jsonl`
- **보조**: 위 ID를 모르면 `agent-transcripts` 아래 JSONL을 검색해 **이번 세션의 사용자 메시지·주제**와 일치하는 파일을 찾습니다.
- **불가 시**: 사용자에게 대화 ID 또는 확인을 요청합니다. 잘못된 JSONL로 export 후 커밋하지 않습니다.

```bash
python scripts/export-cursor-transcript.py --conversation-id <현재-대화-uuid> \
  -o Prompting/{번호}_{작업title}-Prompting.md

---

**Cursor**

commit.md와 관련 규칙·스크립트를 확인한 뒤, 스크립트 기반 Prompting 저장 가이드로 갱신합니다.


`commit.md`를 스크립트 기반으로 갱신하고, `/commit`과 동일 절차인 `commit-workflow.mdc`도 맞춥니다.


`commit.md`와 동일 워크플로를 쓰는 `commit-workflow.mdc`를 스크립트 기반 Prompting 저장으로 맞춰 두었습니다.

### 변경 요약
**이유**: 커밋 시 Prompting을 수동 Export Transcript 작성 대신 `export-cursor-transcript.py`로 생성하도록 되돌림.  
**영향**: `/commit`·「커밋해줘」 시 2단계에서 JSONL → MD 자동 변환, 잘못된 대화 선택 방지 규칙 적용.

### `.cursor/commands/commit.md` (2단계)

| 이전 | 이후 |
|------|------|
| `scripts/`·JSONL 직접 읽기·스크립트 **금지**, 수동 작성 | `scripts/export-cursor-transcript.py`로 export |
| 수동 형식 규칙 나열 | `--conversation-id` 우선, JSONL 경로 직접 지정 예시 |
| — | `--project-slug`만으로 최근 파일 선택 **금지** |
| — | export 후 `User`/`Cursor` 턴 확인, 실패 시 커밋 중단 |

프로젝트 slug는 이 저장소 기준 **`e-DEV-SHealth-04`** 로 명시했습니다. 스크립트가 루트 `scripts/`에 없으면 `.cursor/commands/scripts/export-cursor-transcript.py` 를 쓰도록 안내했습니다.

### `commit-workflow.mdc`
`commit.md`와 같은 2단계 내용으로 동기화했습니다 (`/commit`과 「커밋해줘」 동작 일치).

### 실행 예시

```bash
python scripts/export-cursor-transcript.py --conversation-id <현재-대화-uuid> \
  -o Prompting/{번호}_{작업title}-Prompting.md
```

현재 대화 ID는 Cursor가 제공하는 UUID(에이전트 transcript 컨텍스트의 id)를 사용하면 됩니다.

원하시면 `scripts/export-cursor-transcript.py`를 `.cursor/commands/scripts/`와 동기화하거나, 스크립트 기본 `--project-slug`를 `e-DEV-SHealth-04`로 바꾸는 작업도 이어서 할 수 있습니다.

---

**User**

파일 경로가 @.cursor/commands/scripts/ 폴더로 변경되었어 실제 파일 경로 확인해서 이부분도 반영해줘

---

**Cursor**

실제 스크립트 경로를 확인한 뒤 `commit.md`와 `commit-workflow.mdc`를 반영하겠습니다.


확인 결과 스크립트는 `.cursor/commands/scripts/export-cursor-transcript.py`에 있습니다. `commit.md`와 `commit-workflow.mdc`의 경로를 이에 맞춥니다.


**변경 이유**: transcript export 스크립트가 `.cursor/commands/scripts/`로 옮겨졌으므로 커밋 워크플로 문서의 경로·실행 예시를 실제 위치에 맞춤.

**확인한 실제 경로**
- `.cursor/commands/scripts/export-cursor-transcript.py` (존재, `--help` 실행 확인)
- 루트 `scripts/export-cursor-transcript.py`는 glob에 남아 있을 수 있으나, 워크플로 기준 경로는 **`.cursor/commands/scripts/`** 만 사용

**반영 파일**
- `.cursor/commands/commit.md` — 스크립트 경로·bash 예시 2곳
- `.cursor/rules/commit-workflow.mdc` — 동일

**실행 예시** (프로젝트 루트에서):

```bash
python .cursor/commands/scripts/export-cursor-transcript.py --conversation-id <현재-대화-uuid> \
  -o Prompting/{번호}_{작업title}-Prompting.md
```

참고: 스크립트 기본 `--project-slug`는 아직 `e-DEV-GildedRose-04`입니다. `--conversation-id`를 쓰면 프로젝트 slug와 무관하게 동작합니다. 기본값도 `e-DEV-SHealth-04`로 바꾸길 원하시면 말씀해 주세요.

---

**User**

수정된 커맨드 테스트 해줘


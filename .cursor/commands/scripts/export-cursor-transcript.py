#!/usr/bin/env python3
"""
Export Cursor agent-transcript JSONL to Markdown matching Cursor's
"Export Transcript" format.

Differences addressed vs naive JSONL→MD conversion:
  - Merge consecutive assistant JSONL rows into one **Cursor** block per user turn
  - Omit tool_use entries entirely
  - Strip [REDACTED] and <user_query> wrappers
  - Use **User** / **Cursor** labels (not numbered ## headings)
  - Cursor-style header: # title + _Exported on ... from Cursor (version)_
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from dataclasses import dataclass
from datetime import datetime, timezone, timedelta
from pathlib import Path
from typing import Iterable, Iterator, Literal


Role = Literal["user", "assistant"]


@dataclass
class Turn:
    role: Role
    parts: list[str]


USER_QUERY_RE = re.compile(
    r"^<user_query>\s*([\s\S]*?)\s*</user_query>\s*$", re.IGNORECASE
)
REDACTED_TRAILING_RE = re.compile(r"\n*\[REDACTED\]\s*$", re.IGNORECASE)


def strip_redacted(text: str) -> str:
    """Remove transcript privacy placeholders without touching literal [REDACTED] in prose."""
    if text.strip().upper() == "[REDACTED]":
        return ""
    cleaned = REDACTED_TRAILING_RE.sub("", text)
    cleaned = re.sub(r"\n{3,}", "\n\n", cleaned)
    return cleaned.strip()


def extract_user_text(raw: str) -> str:
    raw = raw.strip()
    match = USER_QUERY_RE.match(raw)
    if match:
        return match.group(1)
    return raw


def extract_text_parts(content: list[dict]) -> list[str]:
    """Pull visible text from a message content array; ignore tool_use."""
    parts: list[str] = []
    for item in content:
        if item.get("type") != "text":
            continue
        text = item.get("text")
        if not isinstance(text, str):
            continue
        cleaned = strip_redacted(text)
        if cleaned:
            parts.append(cleaned)
    return parts


def parse_jsonl(path: Path) -> Iterator[tuple[Role, list[str]]]:
    with path.open(encoding="utf-8") as f:
        for line_no, line in enumerate(f, start=1):
            line = line.strip()
            if not line:
                continue
            try:
                row = json.loads(line)
            except json.JSONDecodeError as exc:
                raise ValueError(f"{path}:{line_no}: invalid JSON") from exc

            role = row.get("role")
            if role not in ("user", "assistant"):
                continue

            message = row.get("message") or {}
            content = message.get("content") or []
            if not isinstance(content, list):
                continue

            parts = extract_text_parts(content)
            if role == "user":
                parts = [extract_user_text(p) for p in parts]
                parts = [p for p in parts if p]

            yield role, parts


def group_turns(rows: Iterable[tuple[Role, list[str]]]) -> list[Turn]:
    """Alternate user turns with merged assistant segments (one **Cursor** block each)."""
    turns: list[Turn] = []
    assistant_buffer: list[str] = []

    for role, parts in rows:
        if not parts:
            continue
        if role == "user":
            if assistant_buffer:
                turns.append(Turn(role="assistant", parts=assistant_buffer))
                assistant_buffer = []
            turns.append(Turn(role="user", parts=list(parts)))
        else:
            assistant_buffer.extend(parts)

    if assistant_buffer:
        turns.append(Turn(role="assistant", parts=assistant_buffer))

    return turns


def format_export_timestamp(
    when: datetime | None, tz_offset_hours: int
) -> str:
    if when is None:
        when = datetime.now(timezone.utc)
    elif when.tzinfo is None:
        when = when.replace(tzinfo=timezone.utc)

    tz = timezone(timedelta(hours=tz_offset_hours))
    local = when.astimezone(tz)
    sign = "+" if tz_offset_hours >= 0 else ""
    # Cursor uses M/D/YYYY without zero-padding (e.g. 5/19/2026)
    date_part = f"{local.month}/{local.day}/{local.year}"
    time_part = local.strftime("%H:%M:%S")
    return f"{date_part} at {time_part} GMT{sign}{tz_offset_hours}"


def join_assistant_parts(parts: list[str]) -> str:
    """Join assistant segments the way Cursor Export Transcript does."""
    if not parts:
        return ""
    merged = parts[0]
    for part in parts[1:]:
        # Cursor puts a blank line between each visible segment (status, interim, answer).
        merged = f"{merged}\n\n\n{part}"
    return merged


def render_markdown(
    turns: list[Turn],
    title: str,
    exported_at: datetime | None,
    cursor_version: str,
    tz_offset_hours: int,
) -> str:
    lines: list[str] = [
        f"# {title}",
        f"_Exported on {format_export_timestamp(exported_at, tz_offset_hours)} "
        f"from Cursor ({cursor_version})_",
        "",
        "---",
        "",
    ]

    for index, turn in enumerate(turns):
        label = "**User**" if turn.role == "user" else "**Cursor**"
        body = (
            "\n\n".join(turn.parts)
            if turn.role == "user"
            else join_assistant_parts(turn.parts)
        )

        lines.extend([label, "", body, ""])

        if index < len(turns) - 1:
            lines.extend(["---", ""])

    return "\n".join(lines) + "\n"


def default_title_from_turns(turns: list[Turn]) -> str:
    for turn in turns:
        if turn.role == "user" and turn.parts:
            first = turn.parts[0].replace("\n", " ").strip()
            if len(first) > 80:
                return first[:77] + "..."
            return first
    return "Cursor chat transcript"


def find_transcript_by_conversation_id(conversation_id: str) -> Path | None:
    projects_root = Path.home() / ".cursor" / "projects"
    if not projects_root.is_dir():
        return None
    for project_dir in projects_root.iterdir():
        if not project_dir.is_dir():
            continue
        candidate = (
            project_dir
            / "agent-transcripts"
            / conversation_id
            / f"{conversation_id}.jsonl"
        )
        if candidate.is_file():
            return candidate
    return None


def resolve_transcript_path(
    jsonl: Path | None,
    project_slug: str | None,
    conversation_id: str | None,
) -> Path:
    if jsonl is not None:
        path = jsonl.expanduser().resolve()
        if not path.is_file():
            raise FileNotFoundError(f"Transcript not found: {path}")
        return path

    if conversation_id:
        found = find_transcript_by_conversation_id(conversation_id)
        if found:
            return found
        raise FileNotFoundError(
            f"No transcript for conversation id '{conversation_id}' under "
            f"{Path.home() / '.cursor' / 'projects'}"
        )

    if project_slug:
        transcripts_dir = (
            Path.home()
            / ".cursor"
            / "projects"
            / project_slug
            / "agent-transcripts"
        )
        if not transcripts_dir.is_dir():
            raise FileNotFoundError(
                f"agent-transcripts directory not found: {transcripts_dir}"
            )
        candidates = sorted(
            transcripts_dir.glob("*/*.jsonl"),
            key=lambda p: p.stat().st_mtime,
            reverse=True,
        )
        if not candidates:
            raise FileNotFoundError(f"No .jsonl transcripts under {transcripts_dir}")
        return candidates[0]

    raise FileNotFoundError(
        "Provide a jsonl path, --conversation-id, or --project-slug for auto-discovery."
    )


def build_arg_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Export Cursor agent-transcript JSONL to Export Transcript markdown."
    )
    parser.add_argument(
        "jsonl",
        nargs="?",
        type=Path,
        help="Path to .jsonl transcript (default: latest in ~/.cursor/projects/...)",
    )
    parser.add_argument(
        "-o",
        "--output",
        type=Path,
        help="Output .md path (default: stdout)",
    )
    parser.add_argument(
        "--title",
        help="Document title (default: first user message)",
    )
    parser.add_argument(
        "--conversation-id",
        help="Transcript UUID when jsonl path is omitted",
    )
    parser.add_argument(
        "--project-slug",
        default="e-DEV-GildedRose-04",
        help="Cursor projects folder name under ~/.cursor/projects/ "
        "(default: e-DEV-GildedRose-04)",
    )
    parser.add_argument(
        "--cursor-version",
        default="unknown",
        help="Cursor version string for export header (e.g. 3.4.20)",
    )
    parser.add_argument(
        "--exported-at",
        help="ISO-8601 timestamp for header (default: now)",
    )
    parser.add_argument(
        "--tz-offset",
        type=int,
        default=9,
        help="Timezone offset hours for header, e.g. 9 for GMT+9 (default: 9)",
    )
    return parser


def main(argv: list[str] | None = None) -> int:
    parser = build_arg_parser()
    args = parser.parse_args(argv)

    try:
        jsonl_path = resolve_transcript_path(
            args.jsonl, args.project_slug, args.conversation_id
        )
    except FileNotFoundError as exc:
        print(exc, file=sys.stderr)
        return 1

    rows = list(parse_jsonl(jsonl_path))
    turns = group_turns(rows)
    if not turns:
        print("No exportable content in transcript.", file=sys.stderr)
        return 1

    title = args.title or default_title_from_turns(turns)
    exported_at = (
        datetime.fromisoformat(args.exported_at.replace("Z", "+00:00"))
        if args.exported_at
        else None
    )

    markdown = render_markdown(
        turns=turns,
        title=title,
        exported_at=exported_at,
        cursor_version=args.cursor_version,
        tz_offset_hours=args.tz_offset,
    )

    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(markdown, encoding="utf-8", newline="\n")
        print(f"Wrote {args.output.resolve()}", file=sys.stderr)
    else:
        sys.stdout.write(markdown)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

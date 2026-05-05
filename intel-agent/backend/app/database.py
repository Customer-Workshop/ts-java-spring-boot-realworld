"""SQLite persistence for evidence packs."""

from __future__ import annotations

import json
import logging
import sqlite3
from datetime import datetime
from pathlib import Path
from typing import Optional

from .models import EvidencePack

logger = logging.getLogger(__name__)

DB_PATH = Path(__file__).parent.parent / "intel_agent.db"


def _get_conn() -> sqlite3.Connection:
    conn = sqlite3.connect(str(DB_PATH))
    conn.row_factory = sqlite3.Row
    return conn


def init_db() -> None:
    conn = _get_conn()
    try:
        conn.execute("""
            CREATE TABLE IF NOT EXISTS evidence_packs (
                id TEXT PRIMARY KEY,
                status TEXT NOT NULL,
                data TEXT NOT NULL,
                created_at TEXT NOT NULL,
                completed_at TEXT
            )
        """)
        conn.commit()
    finally:
        conn.close()
    logger.info("Database initialized at %s", DB_PATH)


def save_pack(pack: EvidencePack) -> None:
    conn = _get_conn()
    try:
        data = pack.model_dump_json()
        conn.execute(
            """
            INSERT OR REPLACE INTO evidence_packs (id, status, data, created_at, completed_at)
            VALUES (?, ?, ?, ?, ?)
            """,
            (
                pack.id,
                pack.status.value,
                data,
                pack.created_at.isoformat(),
                pack.completed_at.isoformat() if pack.completed_at else None,
            ),
        )
        conn.commit()
    finally:
        conn.close()


def load_pack(pack_id: str) -> Optional[EvidencePack]:
    conn = _get_conn()
    try:
        row = conn.execute(
            "SELECT data FROM evidence_packs WHERE id = ?", (pack_id,)
        ).fetchone()
    finally:
        conn.close()
    if row:
        return EvidencePack.model_validate_json(row["data"])
    return None


def list_packs(limit: int = 50) -> list[dict]:
    conn = _get_conn()
    try:
        rows = conn.execute(
            "SELECT id, status, created_at, completed_at FROM evidence_packs "
            "ORDER BY created_at DESC LIMIT ?",
            (limit,),
        ).fetchall()
    finally:
        conn.close()
    return [dict(r) for r in rows]


def delete_pack(pack_id: str) -> bool:
    conn = _get_conn()
    try:
        cursor = conn.execute("DELETE FROM evidence_packs WHERE id = ?", (pack_id,))
        conn.commit()
    finally:
        conn.close()
    return cursor.rowcount > 0

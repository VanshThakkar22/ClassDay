import os
import time
from datetime import datetime, timedelta, timezone

import pymysql

CITY = "Saharanpur"


def connect():
    return pymysql.connect(
        host=os.environ.get("DB_HOST", "mysql"),
        user=os.environ.get("DB_USER", "openbell"),
        password=os.environ.get("DB_PASSWORD", "openbell"),
        database=os.environ.get("DB_NAME", "openbell"),
        charset="utf8mb4",
        autocommit=True,
    )


def wait_for_rows():
    for _ in range(60):
        try:
            with connect() as db:
                with db.cursor() as cursor:
                    cursor.execute("SELECT COUNT(*) FROM city_news WHERE sample = 1")
                    (count,) = cursor.fetchone()
                    if count and count > 0:
                        return
        except pymysql.MySQLError:
            pass
        time.sleep(3)
    raise SystemExit("City news never appeared. The Java seed may not have finished.")


def freshen():
    now = datetime.now(timezone.utc).replace(tzinfo=None)
    with connect() as db:
        with db.cursor() as cursor:
            cursor.execute("SELECT id FROM city_news WHERE sample = 1 ORDER BY id")
            rows = cursor.fetchall()
            for index, (row_id,) in enumerate(rows):
                published = now - timedelta(hours=index * 4 + 1)
                cursor.execute(
                    "UPDATE city_news SET published_at = %s WHERE id = %s",
                    (published, row_id),
                )
    print(f"Kept {len(rows)} sample {CITY} items inside the last 24 hours.", flush=True)


def main():
    wait_for_rows()
    while True:
        freshen()
        time.sleep(60 * 60)


if __name__ == "__main__":
    main()

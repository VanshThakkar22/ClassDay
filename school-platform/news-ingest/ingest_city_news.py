"""Post a Saharanpur headline into Classday.

The newspaper source is not connected yet. This script sends a headline,
the source name, a link, and a one-line summary. It does not copy an article.
Swap `sample_items` later for a licensed RSS or wire feed.
"""

import os
from datetime import datetime, timedelta, timezone

import requests

API = os.environ.get("CLASSDAY_API", "http://localhost:8080")
KEY = os.environ.get("CLASSDAY_INGEST_KEY", "classday-ingest-dev")


def post(title: str, source: str, url: str, summary: str, published: datetime) -> None:
    response = requests.post(
        f"{API}/api/city-news/ingest",
        headers={"X-Ingest-Key": KEY},
        json={
            "city": "Saharanpur",
            "title": title,
            "sourceName": source,
            "sourceUrl": url,
            "summary": summary,
            "publishedAt": published.astimezone(timezone.utc).isoformat().replace("+00:00", "Z"),
        },
        timeout=20,
    )
    response.raise_for_status()
    print(response.status_code, response.json()["title"])


def main() -> None:
    now = datetime.now(timezone.utc)
    post(
        "Sample: inter-school athletics entries close Friday",
        "Demo wire",
        "https://example.com/saharanpur-athletics",
        "Placeholder headline for the last-24-hours city board.",
        now - timedelta(hours=1),
    )


if __name__ == "__main__":
    main()

"""Saharanpur headlines from the last 24 hours.

GET /news returns title, source, link, and a one-line note. It does not copy
the article. POST /push sends those headlines into the ClassDay API.

    python city_api.py
    python city_api.py --push
    python city_api.py --watch
"""

import json
import os
import time
import urllib.error
import urllib.parse
import urllib.request
import xml.etree.ElementTree as ET
from datetime import datetime, timedelta, timezone
from email.utils import parsedate_to_datetime
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

CITY = "Saharanpur"
WINDOW = timedelta(hours=24)
FEED = (
    "https://news.google.com/rss/search?q="
    + urllib.parse.quote("Saharanpur when:1d")
    + "&hl=en-IN&gl=IN&ceid=IN:en"
)
API = os.environ.get("CLASSDAY_API", "http://localhost:8080")
KEY = os.environ.get("CLASSDAY_INGEST_KEY", "classday-ingest-dev")
PORT = int(os.environ.get("CITY_API_PORT", "8090"))
EVERY = int(os.environ.get("CITY_REFRESH_MINUTES", "15")) * 60
KEYWORDS = ("saharanpur", "सहारनपुर")
LOCAL_FEEDS = [
    ("Hindustan Times", "https://www.hindustantimes.com/feeds/rss/cities/lucknow-news/rssfeed.xml"),
    ("Times of India", "https://timesofindia.indiatimes.com/rssfeedstopstories.cms"),
    ("NDTV", "https://feeds.feedburner.com/ndtvnews-top-stories"),
    ("Amar Ujala", "https://www.amarujala.com/rss/uttar-pradesh.xml"),
]
NATIONAL_FEED = ("Hindustan Times", "https://www.hindustantimes.com/feeds/rss/india-news/rssfeed.xml")
TOTAL = 10
LOCAL_MAX = 8
NATIONAL_SLOTS = 2


def read_items(url: str, publisher: str | None, keep) -> list[dict]:
    request = urllib.request.Request(url, headers={"User-Agent": "ClassDay/0.1"})
    with urllib.request.urlopen(request, timeout=20) as response:
        root = ET.fromstring(response.read())
    cutoff = datetime.now(timezone.utc) - WINDOW
    items = []
    for node in root.iter("item"):
        title = (node.findtext("title") or "").strip()
        link = (node.findtext("link") or "").strip()
        note = node.findtext("description") or ""
        source_node = node.find("source")
        source = (source_node.text or "").strip() if source_node is not None else ""
        source = source or publisher or "News"
        published = parsedate_to_datetime((node.findtext("pubDate") or "").strip())
        if published.tzinfo is None:
            published = published.replace(tzinfo=timezone.utc)
        published = published.astimezone(timezone.utc)
        if not title or published < cutoff or not keep(title, note):
            continue
        if title.endswith(" - " + source):
            title = title[: -(len(source) + 3)].strip()
        items.append(
            {
                "city": CITY,
                "title": title,
                "sourceName": source,
                "scope": "City",
                "sourceUrl": link,
                "summary": f"Reported by {source}. The story stays on their site.",
                "publishedAt": published.isoformat().replace("+00:00", "Z"),
            }
        )
    return items


def gather(url: str, publisher: str | None, keep) -> list[dict]:
    try:
        return read_items(url, publisher, keep)
    except (OSError, ET.ParseError, ValueError, TypeError) as error:
        print(f"skipped {url}: {error}", flush=True)
        return []


def about_saharanpur(title: str, note: str) -> bool:
    text = f"{title} {note}".lower()
    return any(word in text for word in KEYWORDS)


def newest(item: dict) -> str:
    return item["publishedAt"]


def headlines() -> list[dict]:
    local = gather(FEED, None, lambda title, note: True)
    for name, url in LOCAL_FEEDS:
        local += gather(url, name, about_saharanpur)
    nationals = [
        {**item, "scope": "National"}
        for item in sorted(gather(NATIONAL_FEED[1], NATIONAL_FEED[0], lambda title, note: True), key=newest, reverse=True)
    ]
    taken = {item["title"] for item in nationals}
    unique = {}
    for item in sorted(local, key=newest, reverse=True):
        if item["title"] not in taken:
            unique.setdefault(item["title"], item)
    picked = nationals[:NATIONAL_SLOTS] + list(unique.values())[:LOCAL_MAX]
    for item in nationals[NATIONAL_SLOTS:]:
        if len(picked) >= TOTAL:
            break
        picked.append(item)
    return sorted(picked, key=newest, reverse=True)


def push(items: list[dict]) -> list[str]:
    sent = []
    for item in items:
        body = json.dumps(item).encode()
        request = urllib.request.Request(
            f"{API}/api/city-news/ingest",
            data=body,
            headers={"Content-Type": "application/json", "X-Ingest-Key": KEY},
            method="POST",
        )
        try:
            with urllib.request.urlopen(request, timeout=20) as response:
                saved = json.loads(response.read().decode())
        except urllib.error.HTTPError as error:
            if error.code == 400:
                continue
            raise
        sent.append(saved.get("title", item["title"]))
    return sent


class Handler(BaseHTTPRequestHandler):
    def _send(self, status: int, payload: dict) -> None:
        body = json.dumps(payload).encode()
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self) -> None:
        if self.path.split("?", 1)[0] == "/health":
            self._send(200, {"status": "ok", "city": CITY})
            return
        if self.path.split("?", 1)[0] != "/news":
            self._send(404, {"error": "Not found."})
            return
        try:
            items = headlines()
        except (urllib.error.URLError, ET.ParseError, ValueError) as error:
            self._send(502, {"error": f"The Saharanpur feed did not answer. {error}"})
            return
        self._send(200, {"city": CITY, "hours": 24, "items": items})

    def do_POST(self) -> None:
        if self.path.split("?", 1)[0] != "/push":
            self._send(404, {"error": "Not found."})
            return
        if self.headers.get("X-Ingest-Key") != KEY:
            self._send(403, {"error": "The news ingest key does not match."})
            return
        try:
            sent = push(headlines())
        except (urllib.error.URLError, ET.ParseError, ValueError) as error:
            self._send(502, {"error": str(error)})
            return
        self._send(200, {"sent": len(sent), "titles": sent})

    def log_message(self, format: str, *args) -> None:
        print(f"{self.address_string()} {format % args}")


def main() -> None:
    import sys

    if "--watch" in sys.argv:
        while True:
            try:
                print(f"{datetime.now():%H:%M} sent {len(push(headlines()))}", flush=True)
            except (urllib.error.URLError, ET.ParseError, ValueError) as error:
                print(f"{datetime.now():%H:%M} skipped: {error}", flush=True)
            time.sleep(EVERY)
    if "--push" in sys.argv:
        titles = push(headlines())
        print(f"Sent {len(titles)}")
        for title in titles:
            print(title)
        return
    server = ThreadingHTTPServer(("127.0.0.1", PORT), Handler)
    print(f"Saharanpur news API on http://127.0.0.1:{PORT}")
    server.serve_forever()


if __name__ == "__main__":
    main()

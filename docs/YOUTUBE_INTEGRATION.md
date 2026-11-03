# YouTube integration

Vynora uses YouTube Data API v3 `videos.list` to read the regional chart:
- `chart=mostPopular`
- `regionCode=BD` (region selector is planned)
- `videoCategoryId=10` (Music)
- `part=snippet,statistics,contentDetails`

Since July 21, 2025, YouTube's `mostPopular` chart reflects Trending Music, Movies and Gaming charts. The Music category filter is used for this screen. This is not a universal newest-release feed and does not guarantee every new song appears first.

YouTube Data API returns metadata, not raw audio. Do not scrape, extract, download, proxy, or background-play YouTube audio. Open selected videos using official YouTube app/site or a compliant official embedded player. Preserve YouTube attribution and follow its API terms.

The `YOUTUBE_API_KEY` dart-define avoids source-control leaks but does not make a mobile key secret. APK contents can be inspected. Restrict the key to YouTube Data API and the Android app identity; a backend proxy is recommended for stronger key isolation and quota management.

Handle quota exhaustion, invalid region, unavailable videos, timeouts and empty responses. Never fabricate rankings or view counts.

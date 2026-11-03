class TrendingVideo {
  const TrendingVideo({
    required this.id,
    required this.title,
    required this.channelTitle,
    required this.thumbnailUrl,
    required this.publishedAt,
    required this.viewCount,
    required this.categoryId,
  });

  final String id;
  final String title;
  final String channelTitle;
  final String thumbnailUrl;
  final DateTime? publishedAt;
  final int? viewCount;
  final String categoryId;

  /// YouTube Music category ID returned by the Data API.
  bool get isMusic => categoryId == '10';

  String get youtubeUrl => 'https://www.youtube.com/watch?v=$id';

  factory TrendingVideo.fromJson(Map<String, dynamic> json) {
    final snippet = json['snippet'] as Map<String, dynamic>? ?? const {};
    final stats = json['statistics'] as Map<String, dynamic>? ?? const {};
    final thumbs = snippet['thumbnails'] as Map<String, dynamic>? ?? const {};
    final thumb =
        (thumbs['maxres'] ??
                thumbs['standard'] ??
                thumbs['high'] ??
                thumbs['medium'] ??
                thumbs['default'])
            as Map<String, dynamic>? ??
        const {};

    return TrendingVideo(
      id: json['id']?.toString() ?? '',
      title: snippet['title']?.toString() ?? 'Untitled',
      channelTitle: snippet['channelTitle']?.toString() ?? 'Unknown artist',
      thumbnailUrl: thumb['url']?.toString() ?? '',
      publishedAt: DateTime.tryParse(snippet['publishedAt']?.toString() ?? ''),
      viewCount: int.tryParse(stats['viewCount']?.toString() ?? ''),
      categoryId: snippet['categoryId']?.toString() ?? '',
    );
  }
}

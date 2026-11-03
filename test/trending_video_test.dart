import 'package:flutter_test/flutter_test.dart';
import 'package:vynora/features/discover/domain/trending_video.dart';

void main() {
  group('TrendingVideo', () {
    test('parses chart metadata and canonical YouTube URL', () {
      final video = TrendingVideo.fromJson({
        'id': 'abcdefghijk',
        'snippet': {
          'title': 'Sample track',
          'channelTitle': 'Sample Artist',
          'publishedAt': '2026-11-01T12:00:00Z',
          'thumbnails': {'high': {'url': 'https://i.ytimg.com/sample.jpg'}},
        },
        'statistics': {'viewCount': '12345'},
      });
      expect(video.title, 'Sample track');
      expect(video.channelTitle, 'Sample Artist');
      expect(video.viewCount, 12345);
      expect(video.youtubeUrl, 'https://www.youtube.com/watch?v=abcdefghijk');
    });

    test('handles missing optional metadata', () {
      final video = TrendingVideo.fromJson({'id': 'abcdefghijk'});
      expect(video.title, 'Untitled');
      expect(video.channelTitle, 'Unknown artist');
      expect(video.viewCount, isNull);
      expect(video.publishedAt, isNull);
    });
  });
}

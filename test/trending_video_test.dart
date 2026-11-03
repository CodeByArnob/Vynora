import 'package:flutter_test/flutter_test.dart';
import 'package:vynora/features/discover/domain/trending_video.dart';

void main() {
  group('TrendingVideo', () {
    test('parses music metadata, artwork and canonical YouTube URL', () {
      final video = TrendingVideo.fromJson({
        'id': 'abcdefghijk',
        'snippet': {
          'title': 'Sample track',
          'channelTitle': 'Sample Artist',
          'categoryId': '10',
          'publishedAt': '2026-11-01T12:00:00Z',
          'thumbnails': {
            'high': {'url': 'https://i.ytimg.com/sample.jpg'},
          },
        },
        'statistics': {'viewCount': '12345'},
      });

      expect(video.title, 'Sample track');
      expect(video.channelTitle, 'Sample Artist');
      expect(video.viewCount, 12345);
      expect(video.isMusic, isTrue);
      expect(video.thumbnailUrl, 'https://i.ytimg.com/sample.jpg');
      expect(video.youtubeUrl, 'https://www.youtube.com/watch?v=abcdefghijk');
    });

    test('does not classify missing category as music', () {
      final video = TrendingVideo.fromJson({'id': 'abcdefghijk'});

      expect(video.title, 'Untitled');
      expect(video.channelTitle, 'Unknown artist');
      expect(video.viewCount, isNull);
      expect(video.publishedAt, isNull);
      expect(video.isMusic, isFalse);
      expect(video.thumbnailUrl, isEmpty);
    });
  });
}

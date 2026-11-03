import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:vynora/features/discover/data/youtube_trending_service.dart';

void main() {
  group('YouTubeTrendingService', () {
    test('requires an API key', () async {
      final service = YouTubeTrendingService(
        client: MockClient((_) async => http.Response('{}', 200)),
        apiKey: '',
      );

      await expectLater(
        service.fetchTrendingMusic(),
        throwsA(isA<YouTubeApiException>()),
      );
      service.close();
    });

    test('returns only music-category entries with artwork', () async {
      Uri? requestedUri;
      final client = MockClient((request) async {
        requestedUri = request.url;
        return http.Response(
          jsonEncode({
            'items': [
              {
                'id': 'music1234567',
                'snippet': {
                  'title': 'Sample track',
                  'channelTitle': 'Sample artist',
                  'categoryId': '10',
                  'thumbnails': {
                    'high': {'url': 'https://example.com/cover.jpg'},
                  },
                },
                'statistics': {'viewCount': '2500'},
              },
              {
                'id': 'other123456',
                'snippet': {
                  'title': 'Not music',
                  'categoryId': '20',
                  'thumbnails': {
                    'high': {'url': 'https://example.com/other.jpg'},
                  },
                },
              },
              {
                'id': 'noimage12345',
                'snippet': {
                  'title': 'Music without artwork',
                  'categoryId': '10',
                },
              },
            ],
          }),
          200,
        );
      });
      final service = YouTubeTrendingService(
        client: client,
        apiKey: 'test-key',
      );

      final tracks = await service.fetchTrendingMusic(maxResults: 100);

      expect(requestedUri?.host, 'www.googleapis.com');
      expect(requestedUri?.path, '/youtube/v3/videos');
      expect(requestedUri?.queryParameters['chart'], 'mostPopular');
      expect(requestedUri?.queryParameters['regionCode'], 'BD');
      expect(requestedUri?.queryParameters['videoCategoryId'], '10');
      expect(requestedUri?.queryParameters['part'], 'snippet,statistics');
      expect(requestedUri?.queryParameters['maxResults'], '50');
      expect(tracks, hasLength(1));
      expect(tracks.single.title, 'Sample track');
      expect(tracks.single.viewCount, 2500);
      expect(tracks.single.isMusic, isTrue);
      expect(tracks.single.thumbnailUrl, 'https://example.com/cover.jpg');
      service.close();
    });

    test('rejects an invalid region code before making a request', () async {
      var requestCount = 0;
      final service = YouTubeTrendingService(
        client: MockClient((_) async {
          requestCount++;
          return http.Response('{}', 200);
        }),
        apiKey: 'test-key',
      );

      await expectLater(
        service.fetchTrendingMusic(regionCode: 'Bangladesh'),
        throwsA(isA<YouTubeApiException>()),
      );
      expect(requestCount, 0);
      service.close();
    });

    test('surfaces the API error message for non-200 responses', () async {
      final service = YouTubeTrendingService(
        client: MockClient(
          (_) async => http.Response(
            jsonEncode({
              'error': {'message': 'The request is not authorized.'},
            }),
            403,
          ),
        ),
        apiKey: 'test-key',
      );

      await expectLater(
        service.fetchTrendingMusic(),
        throwsA(
          isA<YouTubeApiException>().having(
            (error) => error.message,
            'message',
            'The request is not authorized.',
          ),
        ),
      );
      service.close();
    });
  });
}

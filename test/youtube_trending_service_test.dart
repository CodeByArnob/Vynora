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

    test('requests the Bangladesh music chart and parses results', () async {
      Uri? requestedUri;
      final client = MockClient((request) async {
        requestedUri = request.url;
        return http.Response(
          jsonEncode({
            'items': [
              {
                'id': 'abcdefghijk',
                'snippet': {
                  'title': 'Sample track',
                  'channelTitle': 'Sample artist',
                  'thumbnails': {
                    'high': {'url': 'https://example.com/cover.jpg'},
                  },
                },
                'statistics': {'viewCount': '2500'},
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

      final videos = await service.fetchTrendingMusic(maxResults: 100);

      expect(requestedUri?.host, 'www.googleapis.com');
      expect(requestedUri?.path, '/youtube/v3/videos');
      expect(requestedUri?.queryParameters['chart'], 'mostPopular');
      expect(requestedUri?.queryParameters['regionCode'], 'BD');
      expect(requestedUri?.queryParameters['videoCategoryId'], '10');
      expect(requestedUri?.queryParameters['maxResults'], '50');
      expect(videos, hasLength(1));
      expect(videos.single.title, 'Sample track');
      expect(videos.single.viewCount, 2500);
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

import 'dart:convert';
import 'package:http/http.dart' as http;
import '../domain/trending_video.dart';

class YouTubeApiException implements Exception {
  const YouTubeApiException(this.message);
  final String message;
  @override
  String toString() => message;
}

/// Retrieves YouTube chart metadata only; never extracts media streams.
class YouTubeTrendingService {
  YouTubeTrendingService({http.Client? client, String? apiKey})
      : _client = client ?? http.Client(),
        _apiKey = apiKey ?? const String.fromEnvironment('YOUTUBE_API_KEY');

  final http.Client _client;
  final String _apiKey;

  Future<List<TrendingVideo>> fetchTrendingMusic({
    String regionCode = 'BD',
    int maxResults = 25,
  }) async {
    if (_apiKey.isEmpty) {
      throw const YouTubeApiException(
        'Configure YOUTUBE_API_KEY at build time to load YouTube charts.',
      );
    }
    final uri = Uri.https('www.googleapis.com', '/youtube/v3/videos', {
      'part': 'snippet,statistics,contentDetails',
      'chart': 'mostPopular',
      'regionCode': regionCode.toUpperCase(),
      'videoCategoryId': '10',
      'maxResults': maxResults.clamp(1, 50).toString(),
      'key': _apiKey,
    });
    final response = await _client.get(uri).timeout(
      const Duration(seconds: 15),
    );
    if (response.statusCode != 200) {
      final body = jsonDecode(response.body);
      final message = body is Map<String, dynamic>
          ? body['error']?['message']?.toString()
          : null;
      throw YouTubeApiException(
        message ?? 'YouTube API error: HTTP ${response.statusCode}',
      );
    }
    final body = jsonDecode(response.body) as Map<String, dynamic>;
    final items = body['items'] as List<dynamic>? ?? const [];
    return items
        .whereType<Map<String, dynamic>>()
        .map(TrendingVideo.fromJson)
        .where((video) => video.id.isNotEmpty)
        .toList(growable: false);
  }

  void close() => _client.close();
}

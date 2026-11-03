import 'dart:convert';

import 'package:http/http.dart' as http;

import '../domain/trending_video.dart';

class YouTubeApiException implements Exception {
  const YouTubeApiException(this.message);

  final String message;

  @override
  String toString() => message;
}

/// Retrieves YouTube Music-category metadata and artwork only.
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

    final normalizedRegion = regionCode.trim().toUpperCase();
    if (!RegExp(r'^[A-Z]{2}$').hasMatch(normalizedRegion)) {
      throw const YouTubeApiException(
        'Region code must be a valid two-letter country code.',
      );
    }

    final uri = Uri.https('www.googleapis.com', '/youtube/v3/videos', {
      'part': 'snippet,statistics',
      'chart': 'mostPopular',
      'regionCode': normalizedRegion,
      'videoCategoryId': '10',
      'maxResults': maxResults.clamp(1, 50).toString(),
      'key': _apiKey,
    });

    final response = await _client
        .get(uri)
        .timeout(const Duration(seconds: 15));

    if (response.statusCode != 200) {
      String? message;
      try {
        final body = jsonDecode(response.body);
        if (body is Map<String, dynamic>) {
          final error = body['error'];
          if (error is Map<String, dynamic>) {
            message = error['message']?.toString();
          }
        }
      } on FormatException {
        // Fall back to the HTTP status when the API response is not JSON.
      }

      throw YouTubeApiException(
        message ?? 'YouTube API error: HTTP ${response.statusCode}',
      );
    }

    final decoded = jsonDecode(response.body);
    if (decoded is! Map<String, dynamic>) {
      throw const YouTubeApiException(
        'YouTube API returned an unexpected response format.',
      );
    }

    final rawItems = decoded['items'];
    if (rawItems is! List) {
      return const [];
    }

    return rawItems
        .whereType<Map<String, dynamic>>()
        .map(TrendingVideo.fromJson)
        .where((track) =>
            track.id.isNotEmpty &&
            track.isMusic &&
            track.thumbnailUrl.isNotEmpty)
        .toList(growable: false);
  }

  void close() => _client.close();
}

import 'dart:convert';
import 'package:http/http.dart' as http;
import '../domain/audius_trending_track.dart';

class AudiusApiException implements Exception {
  const AudiusApiException(this.message);
  final String message;
  @override
  String toString() => message;
}

class AudiusTrendingService {
  AudiusTrendingService({http.Client? client, String? apiKey})
      : _client = client ?? http.Client(),
        _apiKey = apiKey ?? const String.fromEnvironment('AUDIUS_API_KEY');

  final http.Client _client;
  final String _apiKey;

  Future<List<AudiusTrendingTrack>> fetchTrendingMusic({
    String time = 'week',
    int maxResults = 25,
  }) async {
    final queryParams = <String, String>{
      'app_name': 'Vynora',
      'time': time,
      'limit': maxResults.clamp(1, 50).toString(),
    };
    if (_apiKey.isNotEmpty) {
      queryParams['api_key'] = _apiKey;
    }

    final uri = Uri.https('api.audius.co', '/v1/tracks/trending', queryParams);

    final response = await _client.get(
      uri,
      headers: const {'Accept': 'application/json'},
    );

    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw AudiusApiException(
        'Audius request failed with status ${response.statusCode}',
      );
    }

    final payload = jsonDecode(response.body) as Map<String, dynamic>;
    final items = payload['data'] as List<dynamic>? ?? const [];

    return items
        .whereType<Map<String, dynamic>>()
        .map(AudiusTrendingTrack.fromJson)
        .toList();
  }

  void close() {
    _client.close();
  }
}

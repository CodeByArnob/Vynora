class AudiusTrendingTrack {
  const AudiusTrendingTrack({
    required this.id,
    required this.title,
    required this.artist,
    required this.thumbnailUrl,
    required this.durationSeconds,
    required this.genre,
  });

  final String id;
  final String title;
  final String artist;
  final String thumbnailUrl;
  final int durationSeconds;
  final String genre;

  String get streamUrl => 'https://api.audius.co/v1/tracks/$id/stream?app_name=Vynora';

  factory AudiusTrendingTrack.fromJson(Map<String, dynamic> json) {
    final user = json['user'] as Map<String, dynamic>? ?? const {};
    final artwork = json['artwork'] as Map<String, dynamic>? ?? const {};
    final artUrl = artwork['480x480']?.toString() ??
        artwork['150x150']?.toString() ??
        artwork['1000x1000']?.toString() ??
        '';

    return AudiusTrendingTrack(
      id: json['id']?.toString() ?? '',
      title: json['title']?.toString() ?? 'Untitled',
      artist: user['name']?.toString() ?? 'Unknown artist',
      thumbnailUrl: artUrl,
      durationSeconds: (json['duration'] as num?)?.toInt() ?? 0,
      genre: json['genre']?.toString() ?? 'Music',
    );
  }
}

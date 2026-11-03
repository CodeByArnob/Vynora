import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../data/audius_trending_service.dart';
import '../domain/audius_trending_track.dart';

final trendingServiceProvider = Provider<AudiusTrendingService>((ref) {
  final service = AudiusTrendingService();
  ref.onDispose(service.close);
  return service;
});

final trendingMusicProvider = FutureProvider<List<AudiusTrendingTrack>>(
  (ref) => ref.watch(trendingServiceProvider).fetchTrendingMusic(),
);

class DiscoverScreen extends ConsumerWidget {
  const DiscoverScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final trending = ref.watch(trendingMusicProvider);
    final colors = Theme.of(context).colorScheme;

    return Scaffold(
      appBar: AppBar(
        title: const Text(
          'vynora',
          style: TextStyle(fontWeight: FontWeight.w900, letterSpacing: -1.1),
        ),
      ),
      body: RefreshIndicator(
        onRefresh: () => ref.refresh(trendingMusicProvider.future),
        child: ListView(
          padding: const EdgeInsets.fromLTRB(20, 8, 20, 32),
          children: [
            Text(
              'GOOD MUSIC,\nGOOD MOOD.',
              style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                fontWeight: FontWeight.w900,
                height: 1.04,
                letterSpacing: -1.2,
              ),
            ),
            const SizedBox(height: 10),
            Text(
              'Discover decentralized music streaming with Audius.',
              style: TextStyle(color: colors.onSurfaceVariant),
            ),
            const SizedBox(height: 24),
            Row(
              children: [
                Expanded(
                  child: Text(
                    'Trending on Audius',
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                ),
                const Chip(
                  avatar: Icon(Icons.public, size: 16),
                  label: Text('Global'),
                ),
              ],
            ),
            const SizedBox(height: 10),
            trending.when(
              loading:
                  () => const Padding(
                    padding: EdgeInsets.all(36),
                    child: Center(child: CircularProgressIndicator()),
                  ),
              error:
                  (error, _) => Card(
                    child: Padding(
                      padding: const EdgeInsets.all(20),
                      child: Column(
                        children: [
                          const Icon(Icons.cloud_off_rounded, size: 36),
                          const SizedBox(height: 8),
                          const Text('Could not load trending music'),
                          const SizedBox(height: 6),
                          Text(
                            error.toString(),
                            textAlign: TextAlign.center,
                            style: Theme.of(context).textTheme.bodySmall,
                          ),
                          const SizedBox(height: 12),
                          FilledButton.tonal(
                            onPressed:
                                () => ref.invalidate(trendingMusicProvider),
                            child: const Text('Try again'),
                          ),
                        ],
                      ),
                    ),
                  ),
              data:
                  (items) =>
                      items.isEmpty
                          ? const Card(
                            child: Padding(
                              padding: EdgeInsets.all(24),
                              child: Text(
                                'No chart results available at this time.',
                              ),
                            ),
                          )
                          : Column(
                            children: [
                              for (var i = 0; i < items.length; i++)
                                _TrendingTile(rank: i + 1, track: items[i]),
                            ],
                          ),
            ),
            const SizedBox(height: 16),
            Text(
              'Audius provides online music streaming via its decentralized API.',
              style: Theme.of(
                context,
              ).textTheme.bodySmall?.copyWith(color: colors.onSurfaceVariant),
            ),
          ],
        ),
      ),
    );
  }
}

class _TrendingTile extends StatelessWidget {
  const _TrendingTile({required this.rank, required this.track});

  final int rank;
  final AudiusTrendingTrack track;

  @override
  Widget build(BuildContext context) => ListTile(
    contentPadding: const EdgeInsets.symmetric(vertical: 5),
    leading: SizedBox(
      width: 66,
      height: 66,
      child: ClipRRect(
        borderRadius: BorderRadius.circular(12),
        child:
            track.thumbnailUrl.isEmpty
                ? const ColoredBox(
                  color: Colors.black12,
                  child: Icon(Icons.music_note),
                )
                : CachedNetworkImage(
                  imageUrl: track.thumbnailUrl,
                  fit: BoxFit.cover,
                  errorWidget:
                      (_, __, ___) => const ColoredBox(
                        color: Colors.black12,
                        child: Icon(Icons.music_note),
                      ),
                  placeholder:
                      (_, __) => const ColoredBox(
                        color: Colors.black12,
                        child: Center(
                          child: CircularProgressIndicator(strokeWidth: 2),
                        ),
                      ),
                ),
      ),
    ),
    title: Text(
      track.title,
      maxLines: 2,
      overflow: TextOverflow.ellipsis,
      style: const TextStyle(fontWeight: FontWeight.w700),
    ),
    subtitle: Text(
      '#$rank · ${track.artist} · ${track.genre}',
      maxLines: 1,
      overflow: TextOverflow.ellipsis,
    ),
    trailing: const Icon(
      Icons.play_circle_outline_rounded,
      color: Color(0xFF1DB954),
    ),
  );
}

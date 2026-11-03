import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:url_launcher/url_launcher.dart';
import '../data/youtube_trending_service.dart';
import '../domain/trending_video.dart';

final trendingServiceProvider = Provider<YouTubeTrendingService>((ref) {
  final service = YouTubeTrendingService();
  ref.onDispose(service.close);
  return service;
});
final trendingMusicProvider = FutureProvider<List<TrendingVideo>>(
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
        title: const Text('vynora', style: TextStyle(
          fontWeight: FontWeight.w900, letterSpacing: -1.1,
        )),
        actions: [
          IconButton(onPressed: () {}, icon: const Icon(Icons.search_rounded)),
          IconButton(onPressed: () {}, icon: const Icon(Icons.tune_rounded)),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () => ref.refresh(trendingMusicProvider.future),
        child: ListView(
          padding: const EdgeInsets.fromLTRB(20, 8, 20, 32),
          children: [
            Text('GOOD MUSIC,\nGOOD MOOD.',
              style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                fontWeight: FontWeight.w900, height: 1.04, letterSpacing: -1.2,
              )),
            const SizedBox(height: 10),
            Text('Discover what listeners are playing right now.',
              style: TextStyle(color: colors.onSurfaceVariant)),
            const SizedBox(height: 24),
            Row(children: [
              Expanded(child: Text('Trending music',
                style: Theme.of(context).textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.w800))),
              const Chip(avatar: Icon(Icons.public, size: 16),
                label: Text('Bangladesh')),
            ]),
            const SizedBox(height: 10),
            trending.when(
              loading: () => const Padding(
                padding: EdgeInsets.all(36),
                child: Center(child: CircularProgressIndicator())),
              error: (error, _) => Card(child: Padding(
                padding: const EdgeInsets.all(20),
                child: Column(children: [
                  const Icon(Icons.cloud_off_rounded, size: 36),
                  const SizedBox(height: 8),
                  const Text('Could not load trending music'),
                  const SizedBox(height: 6),
                  Text(error.toString(), textAlign: TextAlign.center,
                    style: Theme.of(context).textTheme.bodySmall),
                  const SizedBox(height: 12),
                  FilledButton.tonal(
                    onPressed: () => ref.invalidate(trendingMusicProvider),
                    child: const Text('Try again')),
                ]))),
              data: (items) => items.isEmpty
                ? const Card(child: Padding(
                    padding: EdgeInsets.all(24),
                    child: Text('No chart results available for this region.')))
                : Column(children: [
                    for (var i = 0; i < items.length; i++)
                      _TrendingTile(rank: i + 1, video: items[i]),
                  ]),
            ),
            const SizedBox(height: 24),
            Text('Your library', style: Theme.of(context).textTheme.titleLarge
              ?.copyWith(fontWeight: FontWeight.w800)),
            const Card(child: ListTile(
              leading: CircleAvatar(child: Icon(Icons.library_music_rounded)),
              title: Text('On-device music'),
              subtitle: Text('Your songs, albums and playlists will appear here.'),
              trailing: Icon(Icons.chevron_right_rounded),
            )),
            const SizedBox(height: 12),
            Text('YouTube provides chart metadata and thumbnails. Playback opens '
              'in YouTube; Vynora does not extract YouTube audio.',
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                color: colors.onSurfaceVariant)),
          ],
        ),
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: 0,
        destinations: const [
          NavigationDestination(icon: Icon(Icons.explore_outlined),
            selectedIcon: Icon(Icons.explore), label: 'Discover'),
          NavigationDestination(icon: Icon(Icons.library_music_outlined),
            label: 'Library'),
          NavigationDestination(icon: Icon(Icons.favorite_border_rounded),
            label: 'Liked'),
        ],
        onDestinationSelected: (_) {},
      ),
    );
  }
}

class _TrendingTile extends StatelessWidget {
  const _TrendingTile({required this.rank, required this.video});
  final int rank;
  final TrendingVideo video;

  @override
  Widget build(BuildContext context) => ListTile(
    contentPadding: const EdgeInsets.symmetric(vertical: 5),
    leading: SizedBox(width: 104, height: 66,
      child: ClipRRect(borderRadius: BorderRadius.circular(12),
        child: video.thumbnailUrl.isEmpty
          ? const ColoredBox(color: Colors.black12,
              child: Icon(Icons.music_note))
          : CachedNetworkImage(imageUrl: video.thumbnailUrl, fit: BoxFit.cover,
              errorWidget: (_, __, ___) => const ColoredBox(
                color: Colors.black12, child: Icon(Icons.music_note)),
              placeholder: (_, __) => const ColoredBox(
                color: Colors.black12,
                child: Center(child: CircularProgressIndicator(strokeWidth: 2))),
            ))),
    title: Text(video.title, maxLines: 2, overflow: TextOverflow.ellipsis,
      style: const TextStyle(fontWeight: FontWeight.w700)),
    subtitle: Text('#$rank · ${video.channelTitle}',
      maxLines: 1, overflow: TextOverflow.ellipsis),
    trailing: IconButton(
      tooltip: 'Open on YouTube',
      icon: const Icon(Icons.play_circle_outline_rounded),
      onPressed: () async => launchUrl(Uri.parse(video.youtubeUrl),
        mode: LaunchMode.externalApplication),
    ),
  );
}

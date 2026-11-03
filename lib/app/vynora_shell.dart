import 'package:audio_service/audio_service.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:just_audio/just_audio.dart';

import '../features/discover/presentation/discover_screen.dart';
import '../features/library/presentation/library_screen.dart';
import '../features/player/data/audio_player_provider.dart';

class VynoraShell extends ConsumerStatefulWidget {
  const VynoraShell({super.key});

  @override
  ConsumerState<VynoraShell> createState() => _VynoraShellState();
}

class _VynoraShellState extends ConsumerState<VynoraShell> {
  int _selectedIndex = 0;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: IndexedStack(
        index: _selectedIndex,
        children: const [DiscoverScreen(), LibraryScreen()],
      ),
      bottomNavigationBar: SafeArea(
        top: false,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const _MiniPlayer(),
            NavigationBar(
              selectedIndex: _selectedIndex,
              destinations: const [
                NavigationDestination(
                  icon: Icon(Icons.explore_outlined),
                  selectedIcon: Icon(Icons.explore),
                  label: 'Discover',
                ),
                NavigationDestination(
                  icon: Icon(Icons.library_music_outlined),
                  selectedIcon: Icon(Icons.library_music),
                  label: 'Library',
                ),
              ],
              onDestinationSelected: (index) {
                setState(() => _selectedIndex = index);
              },
            ),
          ],
        ),
      ),
    );
  }
}

class _MiniPlayer extends ConsumerWidget {
  const _MiniPlayer();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final player = ref.watch(audioPlayerProvider);

    return StreamBuilder<int?>(
      stream: player.currentIndexStream,
      builder: (context, indexSnapshot) {
        final index = indexSnapshot.data;
        final sequence = player.sequence;
        if (index == null || index < 0 || index >= sequence.length) {
          return const SizedBox.shrink();
        }

        final tag = sequence[index].tag;
        final item = tag is MediaItem ? tag : null;
        final colors = Theme.of(context).colorScheme;

        return Material(
          color: colors.surfaceContainerLow,
          child: Padding(
            padding: const EdgeInsets.fromLTRB(16, 8, 12, 8),
            child: Row(
              children: [
                Container(
                  width: 42,
                  height: 42,
                  decoration: BoxDecoration(
                    color: colors.secondaryContainer,
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: Icon(
                    Icons.graphic_eq_rounded,
                    color: colors.onSecondaryContainer,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(
                        item?.title ?? 'Now playing',
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(fontWeight: FontWeight.w700),
                      ),
                      Text(
                        'On-device music',
                        style: Theme.of(context).textTheme.bodySmall?.copyWith(
                          color: colors.onSurfaceVariant,
                        ),
                      ),
                    ],
                  ),
                ),
                StreamBuilder<PlayerState>(
                  stream: player.playerStateStream,
                  builder: (context, snapshot) {
                    final isPlaying = snapshot.data?.playing ?? player.playing;
                    return IconButton(
                      tooltip: isPlaying ? 'Pause' : 'Play',
                      onPressed:
                          isPlaying ? player.pause : () => player.play(),
                      icon: Icon(
                        isPlaying
                            ? Icons.pause_rounded
                            : Icons.play_arrow_rounded,
                      ),
                    );
                  },
                ),
                IconButton(
                  tooltip: 'Next track',
                  onPressed: player.hasNext ? player.seekToNext : null,
                  icon: const Icon(Icons.skip_next_rounded),
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}

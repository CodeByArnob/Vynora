import 'dart:async';

import 'package:audio_service/audio_service.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:just_audio/just_audio.dart';

import '../../player/data/audio_player_provider.dart';
import '../data/local_music_providers.dart';
import '../domain/local_track.dart';

class LibraryScreen extends ConsumerStatefulWidget {
  const LibraryScreen({super.key});

  @override
  ConsumerState<LibraryScreen> createState() => _LibraryScreenState();
}

class _LibraryScreenState extends ConsumerState<LibraryScreen> {
  bool _isImporting = false;

  Future<void> _importTracks() async {
    if (_isImporting) return;
    setState(() => _isImporting = true);

    try {
      final tracks = await ref.read(localMusicRepositoryProvider).importTracks();
      ref.invalidate(localTracksProvider);
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            tracks.isEmpty
                ? 'No supported audio files found.'
                : 'Music library updated: ${tracks.length} tracks.',
          ),
        ),
      );
    } catch (error) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Could not import music: $error')),
      );
    } finally {
      if (mounted) setState(() => _isImporting = false);
    }
  }

  Future<void> _playTrack(List<LocalTrack> tracks, int index) async {
    final player = ref.read(audioPlayerProvider);
    final sources =
        tracks
            .map(
              (track) => AudioSource.file(
                track.path,
                tag: MediaItem(
                  id: track.path,
                  title: track.title,
                  album: 'On-device music',
                ),
              ),
            )
            .toList(growable: false);

    try {
      await player.setAudioSources(
        sources,
        initialIndex: index,
        initialPosition: Duration.zero,
      );
      unawaited(player.play());
    } catch (error) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Could not play this track: $error')),
      );
    }
  }

  Future<void> _deleteTrack(LocalTrack track) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder:
          (dialogContext) => AlertDialog(
            title: const Text('Remove imported track?'),
            content: Text(
              '“${track.title}” will be deleted from Vynora storage. '
              'The original file is not changed.',
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(dialogContext, false),
                child: const Text('Cancel'),
              ),
              FilledButton.tonal(
                onPressed: () => Navigator.pop(dialogContext, true),
                child: const Text('Remove'),
              ),
            ],
          ),
    );

    if (confirmed != true || !mounted) return;

    try {
      final player = ref.read(audioPlayerProvider);
      final sequence = player.sequence;
      final index = player.currentIndex;
      if (index != null && index < sequence.length) {
        final currentTag = sequence[index].tag;
        if (currentTag is MediaItem && currentTag.id == track.path) {
          await player.stop();
        }
      }

      await ref.read(localMusicRepositoryProvider).deleteTrack(track);
      ref.invalidate(localTracksProvider);
    } catch (error) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Could not remove this track: $error')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final tracks = ref.watch(localTracksProvider);
    final colors = Theme.of(context).colorScheme;

    return Scaffold(
      appBar: AppBar(
        title: const Text(
          'Your library',
          style: TextStyle(fontWeight: FontWeight.w800),
        ),
        actions: [
          IconButton(
            tooltip: 'Refresh library',
            onPressed: () => ref.invalidate(localTracksProvider),
            icon: const Icon(Icons.refresh_rounded),
          ),
          Padding(
            padding: const EdgeInsets.only(right: 8),
            child: FilledButton.icon(
              onPressed: _isImporting ? null : _importTracks,
              icon:
                  _isImporting
                      ? const SizedBox(
                        width: 16,
                        height: 16,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      )
                      : const Icon(Icons.add_rounded),
              label: const Text('Add music'),
            ),
          ),
        ],
      ),
      body: tracks.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error:
            (error, _) => _LibraryMessage(
              icon: Icons.error_outline_rounded,
              title: 'Library unavailable',
              message: error.toString(),
              actionLabel: 'Try again',
              onAction: () => ref.invalidate(localTracksProvider),
            ),
        data:
            (items) =>
                items.isEmpty
                    ? _LibraryMessage(
                      icon: Icons.library_music_outlined,
                      title: 'Your music starts here',
                      message:
                          'Import audio files from your device. Vynora keeps '
                          'a private copy so your library stays available.',
                      actionLabel: 'Import music',
                      onAction: _importTracks,
                    )
                    : RefreshIndicator(
                      onRefresh: () async {
                        ref.invalidate(localTracksProvider);
                        await ref.read(localTracksProvider.future);
                      },
                      child: ListView.separated(
                        padding: const EdgeInsets.fromLTRB(16, 12, 16, 28),
                        itemCount: items.length,
                        separatorBuilder:
                            (_, _) => const SizedBox(height: 6),
                        itemBuilder: (context, index) {
                          final track = items[index];
                          return Card(
                            child: ListTile(
                              leading: Container(
                                width: 48,
                                height: 48,
                                decoration: BoxDecoration(
                                  color: colors.secondaryContainer,
                                  borderRadius: BorderRadius.circular(12),
                                ),
                                child: Icon(
                                  Icons.music_note_rounded,
                                  color: colors.onSecondaryContainer,
                                ),
                              ),
                              title: Text(
                                track.title,
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                                style: const TextStyle(
                                  fontWeight: FontWeight.w700,
                                ),
                              ),
                              subtitle: Text(
                                track.fileType.isEmpty
                                    ? 'On-device audio'
                                    : '${track.fileType} · On-device audio',
                              ),
                              onTap: () => _playTrack(items, index),
                              trailing: IconButton(
                                tooltip: 'Remove imported track',
                                onPressed: () => _deleteTrack(track),
                                icon: const Icon(Icons.delete_outline_rounded),
                              ),
                            ),
                          );
                        },
                      ),
                    ),
      ),
    );
  }
}

class _LibraryMessage extends StatelessWidget {
  const _LibraryMessage({
    required this.icon,
    required this.title,
    required this.message,
    required this.actionLabel,
    required this.onAction,
  });

  final IconData icon;
  final String title;
  final String message;
  final String actionLabel;
  final VoidCallback onAction;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return Center(
      child: SingleChildScrollView(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(icon, size: 64, color: colors.primary),
            const SizedBox(height: 18),
            Text(
              title,
              textAlign: TextAlign.center,
              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.w800,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              message,
              textAlign: TextAlign.center,
              style: TextStyle(color: colors.onSurfaceVariant),
            ),
            const SizedBox(height: 22),
            FilledButton.icon(
              onPressed: onAction,
              icon: const Icon(Icons.library_add_rounded),
              label: Text(actionLabel),
            ),
          ],
        ),
      ),
    );
  }
}

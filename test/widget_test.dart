import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:vynora/features/discover/domain/trending_video.dart';
import 'package:vynora/features/discover/presentation/discover_screen.dart';
import 'package:vynora/features/library/data/local_music_providers.dart';
import 'package:vynora/features/library/domain/local_track.dart';
import 'package:vynora/features/library/presentation/library_screen.dart';

void main() {
  testWidgets('library presents an import action when it is empty', (
    tester,
  ) async {
    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          localTracksProvider.overrideWith((ref) async => <LocalTrack>[]),
        ],
        child: const MaterialApp(home: LibraryScreen()),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('Your music starts here'), findsOneWidget);
    expect(find.text('Import music'), findsOneWidget);
  });

  testWidgets('discover explains when there are no chart results', (
    tester,
  ) async {
    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          trendingMusicProvider.overrideWith((ref) async => <TrendingVideo>[]),
        ],
        child: const MaterialApp(home: DiscoverScreen()),
      ),
    );
    await tester.pumpAndSettle();

    expect(
      find.text('No chart results available for this region.'),
      findsOneWidget,
    );
  });
}

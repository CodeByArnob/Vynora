import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'local_music_repository.dart';
import '../domain/local_track.dart';

final localMusicRepositoryProvider = Provider<LocalMusicRepository>(
  (ref) => LocalMusicRepository(),
);

final localTracksProvider = FutureProvider<List<LocalTrack>>(
  (ref) => ref.watch(localMusicRepositoryProvider).getTracks(),
);

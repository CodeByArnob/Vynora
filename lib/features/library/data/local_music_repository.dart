import 'dart:io';

import 'package:file_picker/file_picker.dart';
import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';

import '../domain/local_track.dart';

class LocalMusicRepository {
  static const _supportedExtensions = <String>{
    '.mp3',
    '.m4a',
    '.aac',
    '.flac',
    '.wav',
    '.ogg',
    '.opus',
  };

  Future<Directory> _musicDirectory() async {
    final appDirectory = await getApplicationSupportDirectory();
    final musicDirectory = Directory(p.join(appDirectory.path, 'music'));
    await musicDirectory.create(recursive: true);
    return musicDirectory;
  }

  Future<List<LocalTrack>> getTracks() async {
    final directory = await _musicDirectory();
    final tracks = <LocalTrack>[];

    await for (final entity in directory.list(followLinks: false)) {
      if (entity is! File) continue;
      if (!_supportedExtensions.contains(p.extension(entity.path).toLowerCase())) {
        continue;
      }
      tracks.add(LocalTrack(path: entity.path));
    }

    tracks.sort(
      (a, b) => a.title.toLowerCase().compareTo(b.title.toLowerCase()),
    );
    return tracks;
  }

  Future<List<LocalTrack>> importTracks() async {
    final selection = await FilePicker.platform.pickFiles(
      type: FileType.audio,
      allowMultiple: true,
      withData: false,
    );
    if (selection == null || selection.files.isEmpty) {
      return getTracks();
    }

    final directory = await _musicDirectory();
    var copied = 0;
    for (final selected in selection.files) {
      final sourcePath = selected.path;
      final extension = p.extension(selected.name).toLowerCase();
      if (sourcePath == null || !_supportedExtensions.contains(extension)) {
        continue;
      }

      final uniqueName =
          '${DateTime.now().microsecondsSinceEpoch}_${copied++}$extension';
      await File(sourcePath).copy(p.join(directory.path, uniqueName));
    }

    return getTracks();
  }

  Future<void> deleteTrack(LocalTrack track) async {
    final directory = await _musicDirectory();
    final root = p.normalize(directory.path);
    final target = p.normalize(track.path);

    if (!p.isWithin(root, target)) {
      throw FileSystemException('Refusing to delete a file outside Vynora music storage.');
    }

    final file = File(target);
    if (await file.exists()) {
      await file.delete();
    }
  }
}

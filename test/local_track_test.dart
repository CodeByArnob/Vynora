import 'package:flutter_test/flutter_test.dart';
import 'package:vynora/features/library/domain/local_track.dart';

void main() {
  group('LocalTrack', () {
    test('uses a readable title derived from the imported filename', () {
      const track = LocalTrack(path: '/private/music/Some_Song-Remix.mp3');

      expect(track.id, track.path);
      expect(track.title, 'Some Song Remix');
      expect(track.fileType, 'MP3');
    });

    test('preserves the file extension in uppercase', () {
      const track = LocalTrack(path: '/private/music/ambient.flac');

      expect(track.fileType, 'FLAC');
    });
  });
}

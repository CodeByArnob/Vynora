import 'package:path/path.dart' as p;

class LocalTrack {
  const LocalTrack({required this.path});

  final String path;

  String get id => path;

  String get title {
    final name = p.basenameWithoutExtension(path);
    return name.replaceAll(RegExp(r'[_-]+'), ' ').trim();
  }

  String get fileType => p.extension(path).replaceFirst('.', '').toUpperCase();
}

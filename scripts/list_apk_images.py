from pathlib import Path
import struct
import imghdr

def get_image_size(path):
    """Return (width, height) for PNG/JPEG/WebP without external deps."""
    kind = imghdr.what(str(path))
    with open(path, 'rb') as f:
        data = f.read(32)
    if kind == 'png':
        # IHDR is at offset 16, width/height are 4 bytes each
        if data[:8] == b'\x89PNG\r\n\x1a\n':
            w, h = struct.unpack('>II', data[16:24])
            return w, h
    elif kind in ('jpeg', 'jpg'):
        # simplistic; not needed for this pass
        return None
    elif kind == 'webp':
        # VP8/VP8L chunk at offset 12
        chunk = data[12:16]
        if chunk == b'VP8 ':
            w, h = struct.unpack('<HH', data[26:30])
            return w & 0x3fff, h & 0x3fff
        elif chunk == b'VP8L':
            b = struct.unpack('<I', data[21:25])[0]
            w = (b & 0x3fff) + 1
            h = ((b >> 14) & 0x3fff) + 1
            return w, h
    return None

root = Path('c:/Users/bf410/goalday-local/apk_extract_ref/res')
files = []
for p in root.rglob('*'):
    if p.is_file() and p.suffix.lower() in ('.png','.jpg','.jpeg','.webp','.9.png'):
        size = get_image_size(p)
        if size:
            files.append((p.relative_to(root), size, p.stat().st_size))

files.sort(key=lambda x: x[2], reverse=True)
for rel, size, sz in files[:120]:
    print(f'{size[0]}x{size[1]} {sz:>8} {rel}')

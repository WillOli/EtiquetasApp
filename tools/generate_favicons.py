"""Generate high-contrast white-V PNG/GIF favicons using only Python's standard library."""

import math
import struct
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
IMAGE_DIR = ROOT / "backend/src/main/resources/web/img"
BACKGROUND = (128, 0, 0)
POLYGON = ((2.5, 2.5), (16.2, 2.5), (23.5, 9.5), (30.8, 2.5), (44.5, 2.5), (23.5, 46.1))
SUPERSAMPLE = 4


def inside_polygon(x, y, polygon):
    inside = False
    previous = polygon[-1]
    for current in polygon:
        x1, y1 = previous
        x2, y2 = current
        if (y1 > y) != (y2 > y) and x < (x2 - x1) * (y - y1) / (y2 - y1) + x1:
            inside = not inside
        previous = current
    return inside


def frame_pixels(size, angle=0.0, offset_x=0.0):
    pixels = []
    center = size / 2
    scale = size / 48
    cosine, sine = math.cos(angle), math.sin(angle)
    for y in range(size):
        row = []
        for x in range(size):
            covered = 0
            for sy in range(SUPERSAMPLE):
                for sx in range(SUPERSAMPLE):
                    px = (x + (sx + 0.5) / SUPERSAMPLE - center - offset_x) / scale + 24
                    py = (y + (sy + 0.5) / SUPERSAMPLE - center) / scale + 24
                    px, py = px - 24, py - 24
                    px, py = px * cosine + py * sine + 24, -px * sine + py * cosine + 24
                    if inside_polygon(px, py, POLYGON):
                        covered += 1
            row.append(round(covered * 255 / (SUPERSAMPLE * SUPERSAMPLE)))
        pixels.append(row)
    return pixels


def write_png(path, size=48):
    rows = []
    coverage = frame_pixels(size)
    for y, row in enumerate(coverage):
        raw = bytearray([0])
        for x, mark in enumerate(row):
            # Rounded burgundy tile with the original V silhouette rendered in white.
            edge = min(x + 0.5, y + 0.5, size - x - 0.5, size - y - 0.5)
            tile = 255 if edge >= 6 else round(max(0, edge / 6) * 255)
            tint = mark / 255
            raw.extend((round(BACKGROUND[0] * (1 - tint) + 255 * tint),
                        round(BACKGROUND[1] * (1 - tint) + 255 * tint),
                        round(BACKGROUND[2] * (1 - tint) + 255 * tint), tile))
        rows.append(raw)
    payload = b"".join(rows)

    def chunk(kind, data):
        return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data))

    png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">2I5B", size, size, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(payload, 9)) + chunk(b"IEND", b"")
    path.write_bytes(png)


def lzw_data(indices):
    # Clear before each literal: simple, valid fixed-width GIF LZW with no dictionary growth.
    codes = []
    for index in indices:
        codes.extend((256, index))
    codes.append(257)
    output = bytearray()
    accumulator = bit_count = 0
    for code in codes:
        accumulator |= code << bit_count
        bit_count += 9
        while bit_count >= 8:
            output.append(accumulator & 255)
            accumulator >>= 8
            bit_count -= 8
    if bit_count:
        output.append(accumulator & 255)
    blocks = bytearray()
    for start in range(0, len(output), 255):
        block = output[start:start + 255]
        blocks.append(len(block))
        blocks.extend(block)
    blocks.append(0)
    return bytes(blocks)


def write_gif(path, size=32):
    palette = bytearray()
    for index in range(256):
        mix = index / 255
        palette.extend(round(BACKGROUND[channel] * (1 - mix) + 255 * mix) for channel in range(3))
    gif = bytearray(b"GIF89a")
    gif.extend(struct.pack("<2HBBB", size, size, 0xF7, 0, 0))
    gif.extend(palette)
    gif.extend(b"!\xFF\x0BNETSCAPE2.0\x03\x01\x00\x00\x00")
    for frame in range(24):
        phase = 2 * math.pi * frame / 24
        pixels = frame_pixels(size, math.sin(phase) * 0.09, math.sin(phase) * 1.2)
        gif.extend(b"!\xF9\x04\x00\x08\x00\x00\x00")
        gif.extend(b",\x00\x00\x00\x00" + struct.pack("<2H", size, size) + b"\x00")
        gif.append(8)
        gif.extend(lzw_data([value for row in pixels for value in row]))
    gif.append(0x3B)
    path.write_bytes(gif)


if __name__ == "__main__":
    write_png(IMAGE_DIR / "Logo_V_Branco.png")
    write_gif(IMAGE_DIR / "Logo_V_Branco_Animado.gif")

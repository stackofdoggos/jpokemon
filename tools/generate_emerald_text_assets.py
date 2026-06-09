#!/usr/bin/env python3
"""Generate the Emerald text UI assets consumed by com.pokemon.gui.text.emerald.

Inputs (tools/emerald_src/, fetched from pret/pokeemerald):
  latin_normal.png  256x512 indexed; 16x16 px cells, 16 per row; glyph id = row*16+col.
                    Pixel indices: 0=transparent, 1=fg, 2=shadow, 3=white (kept opaque).
  fonts.c           contains gFontNormalLatinGlyphWidths (advance width per glyph id).
  message_box.png   56x16 indexed; 7x2 tiles of 8x8 used by WindowFunc_DrawDialogueFrame.
  down_arrow.png    8x48 indexed; blitted as an 8x16 strip at y offset {0,1,2,1}.

Outputs (src/main/resources/art/ui/emerald/):
  font_normal.png         256x256 RGBA, 16x16 grid of 16x16 cells, marker colors:
                          fg=#000000, shadow=#808080, white=#FFFFFF, rest transparent.
  font_normal_widths.txt  256 lines, advance width for glyph id = line number.
  dialogue_frame.png      240x48 RGBA strip placed at native y=112 on the 240x160 field.
  down_arrow.png          8x48 RGBA with transparency.
"""

import re
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "tools" / "emerald_src"
OUT = ROOT / "src" / "main" / "resources" / "art" / "ui" / "emerald"

FG_MARKER = (0, 0, 0, 255)
SHADOW_MARKER = (128, 128, 128, 255)
WHITE = (255, 255, 255, 255)
TRANSPARENT = (0, 0, 0, 0)


def index0_to_alpha(indexed):
    """RGBA copy of a palette-mode image with palette index 0 transparent."""
    rgba = indexed.convert("RGBA")
    mask = Image.new("L", indexed.size)
    mask.putdata([0 if p == 0 else 255 for p in indexed.getdata()])
    rgba.putalpha(mask)
    return rgba


def generate_font_sheet():
    src = Image.open(SRC / "latin_normal.png")
    out = Image.new("RGBA", (256, 256), TRANSPARENT)
    px_in = src.load()
    px_out = out.load()
    for y in range(256):  # first 256 glyph ids only (16 rows of 16x16 cells)
        for x in range(256):
            idx = px_in[x, y]
            if idx == 1:
                px_out[x, y] = FG_MARKER
            elif idx == 2:
                px_out[x, y] = SHADOW_MARKER
            elif idx == 3:
                px_out[x, y] = WHITE
    out.save(OUT / "font_normal.png")
    print("font_normal.png written")


def generate_widths():
    text = (SRC / "fonts.c").read_text()
    m = re.search(r"gFontNormalLatinGlyphWidths\[\] = \{(.*?)\};", text, re.S)
    widths = [int(t) for t in re.findall(r"\d+", m.group(1))][:256]
    assert len(widths) == 256, len(widths)
    (OUT / "font_normal_widths.txt").write_text("\n".join(map(str, widths)) + "\n")
    print("font_normal_widths.txt written")


def generate_dialogue_frame():
    """Replicates WindowFunc_DrawDialogueFrame (pokeemerald src/menu.c) for the
    standard field text box: tilemapLeft=2, tilemapTop=15, width=27, height=4.
    Output strip covers tile rows 14..19 (native y 112..160), full 240 width.
    """
    box = index0_to_alpha(Image.open(SRC / "message_box.png"))

    def tile(n, v_flip=False):
        t = box.crop(((n % 7) * 8, (n // 7) * 8, (n % 7) * 8 + 8, (n // 7) * 8 + 8))
        return t.transpose(Image.FLIP_TOP_BOTTOM) if v_flip else t

    left, top, w, h = 2, 15, 27, 4
    y0 = top - 1  # first tile row of the frame (14)
    out = Image.new("RGBA", (240, (h + 2) * 8), TRANSPARENT)

    def put(n, tx, ty, rw=1, rh=1, v_flip=False):
        t = tile(n, v_flip)
        for dy in range(rh):
            for dx in range(rw):
                out.paste(t, ((tx + dx) * 8, (ty + dy - y0) * 8))

    # Top border row
    put(1, left - 2, top - 1)
    put(3, left - 1, top - 1)
    put(4, left, top - 1, rw=w - 1)
    put(5, left + w - 1, top - 1)
    put(6, left + w, top - 1)
    # Body (5 rows: window rows plus the row the bottom border overwrites)
    put(7, left - 2, top, rh=5)
    put(9, left - 1, top, rw=w + 1, rh=5)
    put(10, left + w, top, rh=5)
    # Bottom border row (v-flipped top tiles)
    put(1, left - 2, top + h, v_flip=True)
    put(3, left - 1, top + h, v_flip=True)
    put(4, left, top + h, rw=w - 1, v_flip=True)
    put(5, left + w - 1, top + h, v_flip=True)
    put(6, left + w, top + h, v_flip=True)

    out.save(OUT / "dialogue_frame.png")
    print("dialogue_frame.png written", out.size)


def generate_down_arrow():
    rgba = index0_to_alpha(Image.open(SRC / "down_arrow.png"))
    rgba.save(OUT / "down_arrow.png")
    print("down_arrow.png written", rgba.size)


if __name__ == "__main__":
    OUT.mkdir(parents=True, exist_ok=True)
    generate_font_sheet()
    generate_widths()
    generate_dialogue_frame()
    generate_down_arrow()

# Design decisions & trade-offs

This document summarizes the main design choices, trade-offs, and potential improvements.

## 1) Data model

### Screen
- The screen is a fixed-size array of `Line` with length = `height`.
- Each `Line` has a fixed-size array of `Cell` with length = `width`.

### Cell
A `Cell` stores:
- `codePoint` (Unicode code point, `Cell.EMPTY` for empty)
- `TextAttributes` (fg/bg + style flags)

This design provides:
- O(1) random access by (row, col)
- predictable memory usage
- simple, testable operations

## 2) Scrollback

Scrollback is implemented as a bounded ring buffer of `Line` snapshots:
- when a line scrolls off the top, it is copied (`deepCopy`) and appended to scrollback
- when capacity is exceeded, the oldest entries are overwritten (ring behavior)

Trade-offs:
- `deepCopy` increases copying cost during scrolling but keeps scrollback immutable from the public API
- ring buffer guarantees bounded memory and O(1) insert

## 3) Cursor semantics

- Cursor is always clamped to the screen bounds: `0..width-1` and `0..height-1`
- Moving beyond the bottom during insert/write operations triggers scroll-up behavior where applicable

## 4) Editing operations

### write(text)
- Overwrites cells starting at the current cursor position.
- Cursor moves forward as text is written.
- For plain (1-cell) characters, writing stops at the end of the line.
- Wide characters may wrap to the next line if there is insufficient space.

Trade-off:
- This task focuses on the buffer itself, not on full terminal control sequences (CR/LF, tabs, etc.).

### insert(text) with wrapping
- Insert shifts existing content to the right.
- Overflow from the last column is carried forward into the next write position.
- Carry can wrap to the next line.
- If the cursor moves beyond the bottom, the screen scrolls up and the top line is pushed to scrollback.

The insert algorithm is implemented iteratively (not recursively) to avoid recursion depth issues.

### fillLine(row, char/empty)
- Fills a whole screen row with the provided code point (or empty).
- Uses current attributes for all filled cells.

## 5) Wide characters (bonus)

Some characters (emoji/CJK) are treated as width=2.
Representation:
- the first cell stores the character code point
- the second cell stores a special marker `Cell.CONTINUATION`

Rendering (`Line.toPlainString`) prints continuation cells as spaces.

Trade-offs:
- Width detection uses a simple heuristic (Unicode blocks + emoji ranges), not a full wcwidth/grapheme implementation.
- Combining marks and grapheme clusters are not handled.
- Overwriting part of a wide character is not normalized (a full terminal would apply additional rules).

## 6) Resize (bonus)

`resize(newWidth, newHeight)` strategy:
- Width increase: pad on the right with empty cells
- Width decrease: truncate right side
- Height increase: add empty lines at the bottom
- Height decrease: removed top lines are appended to scrollback (in order)
- Cursor is clamped to new bounds

Trade-offs:
- Shrinking width deterministically discards content on the right.
- Height shrinking moves removed lines to scrollback, which preserves history but changes the scrollback size.

## 7) Testing strategy

The project uses unit tests to document expected behavior:
- cursor clamping and movement boundaries
- write/insert behaviors including wrapping and scrolling
- scrollback capacity behavior
- clear operations
- resize behavior
- wide character behavior using continuation cells

Tests are intended to serve as both verification and documentation.

## 8) Possible improvements

If this were extended toward a more complete terminal buffer:
- Proper wcwidth implementation + grapheme cluster support
- Normalization rules for overwriting wide-character halves
- Additional terminal semantics: CR/LF handling, tabs, scrolling regions, erase-in-line/erase-in-display
- Bulk operations for performance (range clears, fast scrolling)
- More explicit line-level metadata (wrap flags, line attributes)

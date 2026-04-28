# Test Summary

All tests are implemented using NUnit and designed to run in Unity EditMode or a standard NUnit runner. They adhere to the requirement of "Pure C#" testing where possible.

## 1. MatchFinderTests
- `Test_NoMatches`: Verifies clean board detection.
- `Test_HorizontalMatch`: Verifies detecting 3-in-a-row horizontal.
- `Test_VerticalMatch`: Verifies detecting 3-in-a-row vertical.
- `Test_L_ShapeMatch`: Verifies detecting L-shapes (5 tiles).

## 2. SwapServiceTests
- `Test_Swap_ValidMatch`: Verifies a swap that results in a match is committed and moves decremented.
- `Test_Swap_InvalidMatch_Reverts`: Verifies a swap with no match is reverted and no moves lost.
- `Test_Swap_NonAdjacent`: Verifies non-neighbors cannot swap.

## 3. CascadeTests
- `Test_Resolve_SimpleMatch_ClearsAndRefills`:
    - Simulates a full game loop step.
    - Matches tiles -> Clears -> Gravity Falls -> Refills.
    - Verifies event stream sequence (`TilesMatched` -> `TilesCleared` -> `TilesFell` -> `TilesSpawned`).

## 4. ShuffleServiceTests
- `Test_Shuffle_PreservesTiles`: Verifies tile counts (conservation of mass) after shuffle.
- `Test_HasValidMoves`: Verifies detection of valid moves vs deadlocks.

## 5. LevelTests (Loader & Validator)
- `LevelValidatorTests`: Verifies rules (dimensions matching layout, positive moves, etc.).
- `LevelLoaderTests`: Verifies JSON parsing and validation pipeline.

## Known Limitations / Phase 2 Backlog
- **Visuals**: Animations are instant/teleport. Tweening (DOTween or Coroutines) needed for polish.
- **Input**: Uses legacy `Input.GetMouseButton`.
- **Level Objectives**: `objectives` string list is parsed but logic to check them (e.g. "Clear 10 Red") is not implemented in `BoardModel` yet.

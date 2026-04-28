# Delivery Manifest

## Foundation & Core (Pure C#)
- `Assets/Scripts/Board/Model/GridPos.cs`
- `Assets/Scripts/Board/Model/TileType.cs`
- `Assets/Scripts/Board/Model/RandomProvider.cs`
- `Assets/Scripts/Board/Model/BoardEvent.cs`
- `Assets/Scripts/Board/Model/BoardModel.cs`

## Services (Pure C#)
- `Assets/Scripts/Board/Services/MatchFinder.cs`
- `Assets/Scripts/Board/Services/SwapService.cs`
- `Assets/Scripts/Board/Services/ResolveService.cs`
- `Assets/Scripts/Board/Services/ShuffleService.cs`

## Level System (Pure C#)
- `Assets/Scripts/Level/LevelData.cs`
- `Assets/Scripts/Level/LevelValidator.cs`
- `Assets/Scripts/Level/LevelLoader.cs`
- `Assets/Resources/Levels/level_001.json` through `level_010.json`

## Unity Integration (MonoBehaviour)
- `Assets/Scripts/Unity/BoardController.cs`
- `Assets/Scripts/Unity/InputManager.cs`
- `Assets/Scripts/Unity/UIController.cs`
- `Assets/Scripts/Unity/GameManager.cs`

## Tests (NUnit)
- `Assets/Scripts/Tests/MatchFinderTests.cs`
- `Assets/Scripts/Tests/SwapServiceTests.cs`
- `Assets/Scripts/Tests/CascadeTests.cs`
- `Assets/Scripts/Tests/ShuffleServiceTests.cs`
- `Assets/Scripts/Tests/LevelTests.cs`

## Documentation
- `README.md`
- `delivery_manifest.md`
- `test_summary.md`

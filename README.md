# Island Match: Yard Vibes - Wiring Guide

## Overview
This project delivers a Pure C# Domain Layer for a Match-3 game, integrated with Unity scripts. Since no `.unity` or `.prefab` files are provided, follow this guide to wire the scene.

## 1. Project Settings
- **2D Mode**: Ensure project is set to 2D.
- **Layers**: Default is fine.
- **Input**: Use `Input.GetMouseButtonDown` (Legacy Input Manager used in `InputManager.cs`). If using New Input System, `InputManager` needs update. **Assumed: Legacy Input Manager**.

## 2. Prefab Creation
### `TilePrefab`
1. Create a **Square Sprite** GameObject.
2. Name it `TilePrefab`.
3. Ensure it has a **Sprite Renderer**.
4. Create a **Prefab** from this GameObject and delete it from the scene.

## 3. Scene Hierarchy & Components
Create current Scene Hierachy exactly as below:

### 1. `Main Camera`
- **Transform**: Position (0, 0, -10), Size ~5 (Orthographic).
- **Tag**: `MainCamera`.

### 2. `BoardRoot` (Empty GameObject)
- **Transform**: Position (0, 0, 0).
- **Component**: `BoardController` (Script)
    - `Tile Prefab`: Drag `TilePrefab` here.
    - `Board Root`: Drag `BoardRoot` (Self) here.
    - `Tile Size`: `1`
    - `Swap Duration`: `0.2`

### 3. `InputSystem` (Empty GameObject)
- **Component**: `InputManager` (Script)
    - `Game Camera`: Drag `Main Camera` here.
    - `Board Visuals`: Drag `BoardRoot` here.

### 4. `UI_Canvas` (Canvas - Screen Space Overlay)
- **Component**: `UIController` (Script)
    - `Score Text`: Drag child `Text_Score`.
    - `Moves Text`: Drag child `Text_Moves`.
- **Children**:
    - `Text_Score` (Text element): Place top-left. Text: "Score: 0".
    - `Text_Moves` (Text element): Place top-right. Text: "Moves: 10".

### 5. `GameManager` (Empty GameObject)
- **Component**: `GameManager` (Script)
    - `Board Controller`: Drag `BoardRoot` here.
    - `Input Manager`: Drag `InputSystem` here.
    - `Ui Controller`: Drag `UI_Canvas` here.
    - `Level Id`: `level_001` (or change to load others).
    - `Level Json File`: (Optional) Drag a .json file from `Assets/Resources/Levels/` here to override ID properly.

## 4. Sanity Check
1. Press Play.
2. You should see a grid of tiles centered on screen.
3. Score should read 0. Moves should read (e.g. 10).
4. Click a tile to select (Console logs "Selected ...").
5. Click an adjacent tile to Swap.
    - If match -> Tiles disappear, new ones fall. Score increases. Moves decrease.
    - If no match -> Tiles swap and swap back (instant in this prototype visual).
6. Verify no `NullReferenceException` in Console.

## 5. Script Location
- **Domain Logic**: `Assets/Scripts/Board/...` & `Assets/Scripts/Level/...`
- **Unity Scripts**: `Assets/Scripts/Unity/...`
- **Tests**: `Assets/Scripts/Tests/...`

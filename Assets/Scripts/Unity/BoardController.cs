using System.Collections.Generic;
using UnityEngine;
using IslandMatch.Board.Model;
using IslandMatch.Board.Services;

public class BoardController : MonoBehaviour
{
    [Header("References")]
    public GameObject tilePrefab;
    public Transform boardRoot;
    
    [Header("Settings")]
    public float tileSize = 1.0f;
    public float swapDuration = 0.2f;

    private BoardModel _model;
    private Dictionary<GridPos, GameObject> _visualTiles = new Dictionary<GridPos, GameObject>();

    public void Initialize(BoardModel model)
    {
        _model = model;
        _model.OnEvent += HandleBoardEvent;
        RebuildBoard();
    }

    private void OnDestroy()
    {
        if (_model != null) _model.OnEvent -= HandleBoardEvent;
    }

    private void RebuildBoard()
    {
        // Clear existing
        foreach (Transform child in boardRoot)
        {
            Destroy(child.gameObject);
        }
        _visualTiles.Clear();

        // Build new
        for (int x = 0; x < _model.Width; x++)
        {
            for (int y = 0; y < _model.Height; y++)
            {
                GridPos pos = new GridPos(y, x);
                TileType type = _model.GetTile(pos);
                if (type != TileType.None)
                {
                    CreateVisualTile(pos, type);
                }
            }
        }
        
        // Center the board
        float totalWidth = _model.Width * tileSize;
        float totalHeight = _model.Height * tileSize;
        boardRoot.position = new Vector3(-totalWidth / 2f + tileSize/2f, -totalHeight / 2f + tileSize/2f, 0);
    }

    private void CreateVisualTile(GridPos pos, TileType type)
    {
        GameObject go = Instantiate(tilePrefab, boardRoot);
        go.name = $"Tile_{pos.row}_{pos.col}";
        go.transform.localPosition = new Vector3(pos.col * tileSize, pos.row * tileSize, 0);
        
        // Set Color (Assuming simple SpriteRenderer coloring for prototype)
        SpriteRenderer sr = go.GetComponent<SpriteRenderer>();
        if (sr != null)
        {
            sr.color = GetColorForType(type);
        }
        
        _visualTiles[pos] = go;
    }

    private Color GetColorForType(TileType type)
    {
        switch (type)
        {
            case TileType.Red: return Color.red;
            case TileType.Blue: return Color.blue;
            case TileType.Green: return Color.green;
            case TileType.Yellow: return Color.yellow;
            case TileType.Purple: return new Color(0.5f, 0, 0.5f); // Purple
            default: return Color.white;
        }
    }

    private void HandleBoardEvent(BoardEvent evt)
    {
        switch (evt)
        {
            case SwapCommitted swap:
                AnimateSwap(swap.From, swap.To);
                break;
            case SwapReverted swap:
                AnimateSwapRevert(swap.From, swap.To); // Could be same animation
                break;
            case TilesMatched matched:
                // Highlight matches?
                break;
            case TilesCleared cleared:
                foreach (var pos in cleared.Tiles)
                {
                    if (_visualTiles.TryGetValue(pos, out GameObject go))
                    {
                        Destroy(go);
                        _visualTiles.Remove(pos);
                    }
                }
                break;
            case TilesFell fell:
                foreach (var move in fell.Moves)
                {
                    if (_visualTiles.TryGetValue(move.From, out GameObject go))
                    {
                        _visualTiles.Remove(move.From);
                        _visualTiles[move.To] = go;
                        go.name = $"Tile_{move.To.row}_{move.To.col}";
                        // Simple teleport for now, tweening would be better but keeping it simple for wiring
                         go.transform.localPosition = new Vector3(move.To.col * tileSize, move.To.row * tileSize, 0);
                    }
                }
                break;
            case TilesSpawned spawned:
                foreach (var spawn in spawned.Spawns)
                {
                    CreateVisualTile(spawn.Pos, spawn.Type);
                }
                break;
             case BoardShuffled shuffled:
                 RebuildBoard();
                 break;
        }
    }

    private void AnimateSwap(GridPos a, GridPos b)
    {
        // For prototype, just swap positions instantly
        if (_visualTiles.TryGetValue(a, out GameObject goA) && _visualTiles.TryGetValue(b, out GameObject goB))
        {
            Vector3 posA = goA.transform.localPosition;
            Vector3 posB = goB.transform.localPosition;
            goA.transform.localPosition = posB;
            goB.transform.localPosition = posA;

            _visualTiles[a] = goB;
            _visualTiles[b] = goA;
            
            goA.name = $"Tile_{b.row}_{b.col}";
            goB.name = $"Tile_{a.row}_{a.col}";
        }
    }
    
    private void AnimateSwapRevert(GridPos a, GridPos b)
    {
        // Same as swap logic for visual
        AnimateSwap(a, b); 
        // Note: Real implementation would animate A->B then B->A. 
        // Here logic does Revert internally so we just sync to final state? 
        // Wait, SwapService reverts the data, so we should sync visual to match data.
        // Actually SwapReverted means the swap FAILED, so we likely animated them swapping, found no match, and need to swap back.
        // But since we don't hold state during calculation frames, we can just ensure they stay put?
        // Since my `AnimateSwap` is instant, `SwapCommitted` makes them swap.
        // If `SwapService` tries swap -> checks -> reverts, the BoardModel "flickers".
        // But `SwapService` is synchronous. 
        // Is `TrySwap` instant? Yes.
        // So `SwapCommitted` is emitted. 
        // `SwapReverted` is emitted.
        // In `SwapService`, if invalid, we Revert.
        // So we emit Reverted.
        // The View sees "Reverted". It probably shouldn't have animated anything yet?
        // Or maybe it should animate a "shake"?
        // Since I'm not doing complex coroutines, I'll ignore `SwapReverted` visuals for now or just log it.
        Debug.Log($"Swap Reverted: {a} <-> {b}");
    }
}

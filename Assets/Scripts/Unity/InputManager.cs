using UnityEngine;
using IslandMatch.Board.Model;
using IslandMatch.Board.Services;

public class InputManager : MonoBehaviour
{
    public Camera gameCamera;
    public BoardController boardVisuals;
    
    // Dependencies injected via GameManager
    private SwapService _swapService; 
    private BoardModel _board;
    private float _tileSize;

    private GridPos _selectedPos = GridPos.Invalid;

    public void Initialize(SwapService swapService, BoardModel board, float tileSize)
    {
        _swapService = swapService;
        _board = board;
        _tileSize = tileSize;
    }

    void Update()
    {
        if (_swapService == null) return;

        if (Input.GetMouseButtonDown(0))
        {
            HandleClick();
        }
    }

    private void HandleClick()
    {
        Vector3 worldPos = gameCamera.ScreenToWorldPoint(Input.mousePosition);
        // Convert to Grid Space
        // Assuming BoardRoot is centered, we need to defer to BoardController logic or similar coordinate mapping.
        // Simple mapping: Inverse of BoardController placement.
        
        // BoardController centers tiles. Top-Left or Bottom-Left?
        // My BoardController: `x * tileSize` relative to `boardRoot`.
        // And `boardRoot` is at negative half extents.
        // So: localPos = worldPos - boardRoot.position
        // col = localPos.x / tileSize
        // row = localPos.y / tileSize
        
        if (boardVisuals == null) return;
        
        Vector3 localPos = worldPos - boardVisuals.boardRoot.position;
        int col = Mathf.RoundToInt(localPos.x / _tileSize); // Tiles are at 0, 1, 2...
        int row = Mathf.RoundToInt(localPos.y / _tileSize);
        
        GridPos clickPos = new GridPos(row, col);
        
        if (_board.IsValid(clickPos))
        {
            if (_selectedPos == GridPos.Invalid)
            {
                // Select
                _selectedPos = clickPos;
                Debug.Log($"Selected {clickPos}");
            }
            else
            {
                // Try Swap
                if (_selectedPos != clickPos)
                {
                    Debug.Log($"Trying Swap {_selectedPos} -> {clickPos}");
                    bool success = _swapService.TrySwap(_selectedPos, clickPos);
                    if (success)
                    {
                        // Deselect
                        _selectedPos = GridPos.Invalid;
                    }
                    else
                    {
                        // If adjacent but failed, deselect.
                        // If not adjacent, maybe select new?
                        _selectedPos = GridPos.Invalid; 
                    }
                }
                else
                {
                    // Deselect
                    _selectedPos = GridPos.Invalid;
                }
            }
        }
    }
}

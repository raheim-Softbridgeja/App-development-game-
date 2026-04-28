using System;
using IslandMatch.Board.Model;

namespace IslandMatch.Board.Services
{
    public class SwapService
    {
        private readonly BoardModel _board;

        public SwapService(BoardModel board)
        {
            _board = board;
        }

        public bool TrySwap(GridPos a, GridPos b)
        {
            // 1. Validate basic rules
            if (!_board.IsValid(a) || !_board.IsValid(b)) return false;
            if (!IsAdjacent(a, b)) return false;

            // 2. Perform Swap
            TileType typeA = _board.GetTile(a);
            TileType typeB = _board.GetTile(b);

            // Optimization: Don't swap same types
            if (typeA == typeB) return false;

            _board.SetTile(a, typeB);
            _board.SetTile(b, typeA);

            // 3. Check Matches
            var matches = MatchFinder.FindMatches(_board);

            if (matches.Count > 0)
            {
                // Valid Swap
                _board.Emit(new SwapCommitted(a, b));
                _board.DecrementMoves();
                return true;
            }
            else
            {
                // Invalid Swap -> Revert
                _board.SetTile(a, typeA);
                _board.SetTile(b, typeB);
                _board.Emit(new SwapReverted(a, b));
                return false;
            }
        }

        private bool IsAdjacent(GridPos a, GridPos b)
        {
            int dRow = Math.Abs(a.row - b.row);
            int dCol = Math.Abs(a.col - b.col);
            return (dRow == 1 && dCol == 0) || (dRow == 0 && dCol == 1);
        }
    }
}

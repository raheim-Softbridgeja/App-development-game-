using System.Collections.Generic;
using System.Linq;
using IslandMatch.Board.Model;

namespace IslandMatch.Board.Services
{
    public class ShuffleService
    {
        private readonly BoardModel _board;
        private readonly IRandomProvider _random;

        public ShuffleService(BoardModel board, IRandomProvider random)
        {
            _board = board;
            _random = random;
        }

        public void Shuffle()
        {
            var tiles = new List<TileType>();
            var positions = new List<GridPos>();
            int width = _board.Width;
            int height = _board.Height;

            // Collect
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
                    GridPos pos = new GridPos(y, x);
                    TileType t = _board.GetTile(pos);
                    if (t != TileType.None) // Should be all filled usually
                    {
                        tiles.Add(t);
                        positions.Add(pos);
                    }
                }
            }

            // Shuffle (Fisher-Yates)
            int n = tiles.Count;
            while (n > 1)
            {
                n--;
                int k = _random.NextInt(0, n + 1);
                TileType value = tiles[k];
                tiles[k] = tiles[n];
                tiles[n] = value;
            }

            // Reassign
            for (int i = 0; i < tiles.Count; i++)
            {
                _board.SetTile(positions[i], tiles[i]);
            }

            _board.Emit(new BoardShuffled());
        }
        
        // Check if any valid moves exist
        public bool HasValidMoves()
        {
            // Simple check: try virtual swaps
            int width = _board.Width;
            int height = _board.Height;
            
            // Horizontal swaps
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width - 1; x++)
                {
                    if (TryVirtualSwap(new GridPos(y, x), new GridPos(y, x + 1))) return true;
                }
            }
            
            // Vertical swaps
            for (int x = 0; x < width; x++)
            {
                for (int y = 0; y < height - 1; y++)
                {
                    if (TryVirtualSwap(new GridPos(y, x), new GridPos(y + 1, x))) return true;
                }
            }
            
            return false;
        }

        private bool TryVirtualSwap(GridPos a, GridPos b)
        {
            TileType tA = _board.GetTile(a);
            TileType tB = _board.GetTile(b);
            if (tA == tB) return false; // Optimization

            // Swap
            _board.SetTile(a, tB);
            _board.SetTile(b, tA);

            bool hasMatch = MatchFinder.FindMatches(_board).Count > 0;

            // Revert
            _board.SetTile(a, tA);
            _board.SetTile(b, tB);

            return hasMatch;
        }
    }
}

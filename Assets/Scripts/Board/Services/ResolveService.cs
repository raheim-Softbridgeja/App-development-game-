using System.Collections.Generic;
using System.Linq;
using IslandMatch.Board.Model;

namespace IslandMatch.Board.Services
{
    public class ResolveService
    {
        private readonly BoardModel _board;
        private readonly IRandomProvider _random;

        public ResolveService(BoardModel board, IRandomProvider random)
        {
            _board = board;
            _random = random;
        }

        public void ResolveUntilStable()
        {
            int safetyCounter = 0;
            while (safetyCounter < 100)
            {
                var matches = MatchFinder.FindMatches(_board);
                if (matches.Count == 0) break;

                // 1. Clear Matches
                _board.Emit(new TilesMatched(matches));
                
                // Remove tiles
                foreach (var pos in matches)
                {
                    _board.SetTile(pos, TileType.None);
                }
                _board.Emit(new TilesCleared(matches));
                
                // Score
                _board.AddScore(matches.Count * 10);

                // 2. Apply Gravity
                ApplyGravity();

                // 3. Refill
                Refill();

                safetyCounter++;
            }
        }

        private void ApplyGravity()
        {
            var moves = new List<FallMove>();
            int width = _board.Width;
            int height = _board.Height;

            for (int x = 0; x < width; x++)
            {
                int destY = 0;
                for (int y = 0; y < height; y++)
                {
                    TileType tile = _board.GetTile(new GridPos(y, x));
                    if (tile != TileType.None)
                    {
                        if (y != destY)
                        {
                            // Move tile down
                            _board.SetTile(new GridPos(destY, x), tile);
                            _board.SetTile(new GridPos(y, x), TileType.None);
                            moves.Add(new FallMove(new GridPos(y, x), new GridPos(destY, x)));
                        }
                        destY++;
                    }
                }
            }

            if (moves.Count > 0)
            {
                _board.Emit(new TilesFell(moves));
            }
        }

        private void Refill()
        {
            var spawns = new List<SpawnInfo>();
            int width = _board.Width;
            int height = _board.Height;

            for (int x = 0; x < width; x++)
            {
                for (int y = 0; y < height; y++)
                {
                    if (_board.GetTile(new GridPos(y, x)) == TileType.None)
                    {
                        // Spawn new tile
                        // Simple random for now, can be improved to avoid immediate matches if requested,
                        // but prompt just says "Refill logic" using IRandomProvider.
                        // Assuming 5 colors for now.
                        int typeInt = _random.NextInt(1, 6); 
                        TileType newType = (TileType)typeInt;
                        
                        _board.SetTile(new GridPos(y, x), newType);
                        spawns.Add(new SpawnInfo(new GridPos(y, x), newType));
                    }
                }
            }

            if (spawns.Count > 0)
            {
                _board.Emit(new TilesSpawned(spawns));
            }
        }
    }
}

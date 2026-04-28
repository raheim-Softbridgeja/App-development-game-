using System.Collections.Generic;
using IslandMatch.Board.Model;

namespace IslandMatch.Board.Services
{
    public class MatchFinder
    {
        public static List<GridPos> FindMatches(BoardModel board)
        {
            var formattedMatches = new HashSet<GridPos>();
            int width = board.Width;
            int height = board.Height;

            // Horizontal
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width - 2; x++) // -2 because we need at least 3
                {
                    TileType type = board.GetTile(new GridPos(y, x));
                    if (type == TileType.None) continue;

                    if (type == board.GetTile(new GridPos(y, x + 1)) &&
                        type == board.GetTile(new GridPos(y, x + 2)))
                    {
                        formattedMatches.Add(new GridPos(y, x));
                        formattedMatches.Add(new GridPos(y, x + 1));
                        formattedMatches.Add(new GridPos(y, x + 2));
                    }
                }
            }

            // Vertical
            for (int x = 0; x < width; x++)
            {
                for (int y = 0; y < height - 2; y++)
                {
                    TileType type = board.GetTile(new GridPos(y, x));
                    if (type == TileType.None) continue;

                    if (type == board.GetTile(new GridPos(y + 1, x)) &&
                        type == board.GetTile(new GridPos(y + 2, x)))
                    {
                        formattedMatches.Add(new GridPos(y, x));
                        formattedMatches.Add(new GridPos(y + 1, x));
                        formattedMatches.Add(new GridPos(y + 2, x));
                    }
                }
            }

            return new List<GridPos>(formattedMatches);
        }
        
        // Helper to check specific matches if needed, but the main one finds all.
        // We can optimize iteration if performance becomes an issue, but for < 100x100 boards this is fine.
    }
}

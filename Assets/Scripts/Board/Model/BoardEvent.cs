using System.Collections.Generic;

namespace IslandMatch.Board.Model
{
    // Base Event
    public abstract class BoardEvent { }

    // Swap Events
    public class SwapCommitted : BoardEvent
    {
        public GridPos From { get; }
        public GridPos To { get; }

        public SwapCommitted(GridPos from, GridPos to)
        {
            From = from;
            To = to;
        }
    }

    public class SwapReverted : BoardEvent
    {
        public GridPos From { get; }
        public GridPos To { get; }

        public SwapReverted(GridPos from, GridPos to)
        {
            From = from;
            To = to;
        }
    }

    // Match & Clear
    public class TilesMatched : BoardEvent
    {
        public IReadOnlyList<GridPos> Tiles { get; }

        public TilesMatched(IReadOnlyList<GridPos> tiles)
        {
            Tiles = tiles;
        }
    }

    public class TilesCleared : BoardEvent
    {
        public IReadOnlyList<GridPos> Tiles { get; }

        public TilesCleared(IReadOnlyList<GridPos> tiles)
        {
            Tiles = tiles;
        }
    }

    // Gravity & Spawn
    public struct FallMove
    {
        public GridPos From;
        public GridPos To;
        
        public FallMove(GridPos from, GridPos to) { From = from; To = to; }
    }

    public class TilesFell : BoardEvent
    {
        public IReadOnlyList<FallMove> Moves { get; }

        public TilesFell(IReadOnlyList<FallMove> moves)
        {
            Moves = moves;
        }
    }

    public struct SpawnInfo
    {
        public GridPos Pos;
        public TileType Type;

        public SpawnInfo(GridPos pos, TileType type) { Pos = pos; Type = type; }
    }

    public class TilesSpawned : BoardEvent
    {
        public IReadOnlyList<SpawnInfo> Spawns { get; }

        public TilesSpawned(IReadOnlyList<SpawnInfo> spawns)
        {
            Spawns = spawns;
        }
    }

    // Game Logic Events
    public class BoardShuffled : BoardEvent { }

    public class ComboStep : BoardEvent
    {
        public int ComboIndex { get; }
        public ComboStep(int comboIndex) { ComboIndex = comboIndex; }
    }

    public class ScoreGained : BoardEvent
    {
        public int Amount { get; }
        public int TotalScore { get; }

        public ScoreGained(int amount, int totalScore)
        {
            Amount = amount;
            TotalScore = totalScore;
        }
    }
}

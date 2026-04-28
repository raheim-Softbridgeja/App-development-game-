using System;
using System.Collections.Generic;

namespace IslandMatch.Board.Model
{
    public class BoardModel
    {
        private readonly TileType[,] _grid;
        public int Width { get; }
        public int Height { get; }

        public int Score { get; private set; }
        public int MovesRemaining { get; private set; }

        public event Action<BoardEvent> OnEvent;

        public BoardModel(int width, int height)
        {
            if (width <= 0 || height <= 0)
                throw new ArgumentException("Dimensions must be positive");

            Width = width;
            Height = height;
            _grid = new TileType[width, height];
        }

        public void SetTile(GridPos pos, TileType type)
        {
            if (!IsValid(pos)) return;
            _grid[pos.col, pos.row] = type;
        }

        public TileType GetTile(GridPos pos)
        {
            if (!IsValid(pos)) return TileType.None;
            return _grid[pos.col, pos.row];
        }

        public void SetMoves(int moves)
        {
            MovesRemaining = moves;
        }

        public void SetScore(int score)
        {
            Score = score;
        }

        public void AddScore(int amount)
        {
            Score += amount;
            Emit(new ScoreGained(amount, Score));
        }

        public void DecrementMoves()
        {
            if (MovesRemaining > 0)
            {
                MovesRemaining--;
            }
        }

        public bool IsValid(GridPos pos)
        {
            return pos.col >= 0 && pos.col < Width && pos.row >= 0 && pos.row < Height;
        }

        public void Emit(BoardEvent evt)
        {
            OnEvent?.Invoke(evt);
        }

        // Helper for raw grid access (copy)
        public TileType[,] GetGridCopy()
        {
            return (TileType[,])_grid.Clone();
        }
    }
}

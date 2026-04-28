using System;

namespace IslandMatch.Board.Model
{
    public struct GridPos : IEquatable<GridPos>
    {
        public int row;
        public int col;

        public GridPos(int row, int col)
        {
            this.row = row;
            this.col = col;
        }

        public bool Equals(GridPos other)
        {
            return row == other.row && col == other.col;
        }

        public override bool Equals(object obj)
        {
            return obj is GridPos other && Equals(other);
        }

        public override int GetHashCode()
        {
            unchecked
            {
                return (row * 397) ^ col;
            }
        }

        public override string ToString()
        {
            return $"({row}, {col})";
        }

        public static bool operator ==(GridPos left, GridPos right)
        {
            return left.Equals(right);
        }

        public static bool operator !=(GridPos left, GridPos right)
        {
            return !left.Equals(right);
        }

        public static GridPos Invalid => new GridPos(-1, -1);
    }
}

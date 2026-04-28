namespace IslandMatch.Board.Model
{
    // Using an enum for simplicity in Phase 1, but wrapping in a struct/class is fine too.
    // For Match-3, simple types are usually distinct integers.
    public enum TileType
    {
        None = 0,
        Red = 1,
        Blue = 2,
        Green = 3,
        Yellow = 4,
        Purple = 5,
        // Special types can be added here
    }
}

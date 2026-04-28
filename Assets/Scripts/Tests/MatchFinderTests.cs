using System.Collections.Generic;
using NUnit.Framework;
using IslandMatch.Board.Model;
using IslandMatch.Board.Services;

namespace IslandMatch.Tests
{
    [TestFixture]
    public class MatchFinderTests
    {
        [Test]
        public void Test_NoMatches()
        {
            var board = new BoardModel(4, 4);
            // 0 1 0 1
            // 1 0 1 0
            // ...
            board.SetTile(new GridPos(0, 0), TileType.Red);
            board.SetTile(new GridPos(0, 1), TileType.Blue);
            board.SetTile(new GridPos(0, 2), TileType.Red);
            board.SetTile(new GridPos(0, 3), TileType.Blue);
            
            var matches = MatchFinder.FindMatches(board);
            Assert.AreEqual(0, matches.Count);
        }

        [Test]
        public void Test_HorizontalMatch()
        {
            var board = new BoardModel(5, 5);
            board.SetTile(new GridPos(0, 0), TileType.Red);
            board.SetTile(new GridPos(0, 1), TileType.Red);
            board.SetTile(new GridPos(0, 2), TileType.Red);

            var matches = MatchFinder.FindMatches(board);
            Assert.AreEqual(3, matches.Count);
            Assert.Contains(new GridPos(0, 0), matches);
            Assert.Contains(new GridPos(0, 1), matches);
            Assert.Contains(new GridPos(0, 2), matches);
        }

        [Test]
        public void Test_VerticalMatch()
        {
            var board = new BoardModel(5, 5);
            board.SetTile(new GridPos(0, 0), TileType.Blue);
            board.SetTile(new GridPos(1, 0), TileType.Blue);
            board.SetTile(new GridPos(2, 0), TileType.Blue);

            var matches = MatchFinder.FindMatches(board);
            Assert.AreEqual(3, matches.Count);
            Assert.Contains(new GridPos(0, 0), matches);
            Assert.Contains(new GridPos(1, 0), matches);
            Assert.Contains(new GridPos(2, 0), matches);
        }

        [Test]
        public void Test_L_ShapeMatch()
        {
            var board = new BoardModel(5, 5);
            // L shape:
            // . B
            // . B
            // B B B
            board.SetTile(new GridPos(0, 0), TileType.Green);
            board.SetTile(new GridPos(0, 1), TileType.Green);
            board.SetTile(new GridPos(0, 2), TileType.Green);
            board.SetTile(new GridPos(1, 2), TileType.Green);
            board.SetTile(new GridPos(2, 2), TileType.Green);

            var matches = MatchFinder.FindMatches(board);
            Assert.AreEqual(5, matches.Count);
        }
    }
}

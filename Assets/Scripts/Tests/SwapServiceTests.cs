using System.Collections.Generic;
using NUnit.Framework;
using IslandMatch.Board.Model;
using IslandMatch.Board.Services;

namespace IslandMatch.Tests
{
    [TestFixture]
    public class SwapServiceTests
    {
        private BoardModel _board;
        private SwapService _swapService;
        private List<BoardEvent> _events;

        [SetUp]
        public void Setup()
        {
            _board = new BoardModel(4, 4);
            _swapService = new SwapService(_board);
            _events = new List<BoardEvent>();
            _board.OnEvent += (evt) => _events.Add(evt);
            _board.SetMoves(10);
        }

        [Test]
        public void Test_Swap_ValidMatch()
        {
            // Set up a board where a swap creates a match
            // R R . B
            // B . . .
            _board.SetTile(new GridPos(0, 0), TileType.Red);
            _board.SetTile(new GridPos(0, 1), TileType.Red);
            _board.SetTile(new GridPos(0, 2), TileType.Blue); // Swap this with (1, 2) which is Red?
            
            // Let's make it simpler:
            // R R B
            // . . R
            // Swap (0,2) [Blue] with (1,2) [Red] -> Match R R R at row 0? No that won't work.
            
            // R R B
            // . . .
            // Swap (0,2) with a Red at (1,2)
             _board.SetTile(new GridPos(0, 0), TileType.Red);
             _board.SetTile(new GridPos(0, 1), TileType.Red);
             _board.SetTile(new GridPos(0, 2), TileType.Blue);
             _board.SetTile(new GridPos(1, 2), TileType.Red); // Neighbor below
             
             // Swap (0,2) and (1,2)
             bool result = _swapService.TrySwap(new GridPos(0, 2), new GridPos(1, 2));
             
             Assert.IsTrue(result);
             Assert.IsInstanceOf<SwapCommitted>(_events[0]);
             Assert.AreEqual(TileType.Red, _board.GetTile(new GridPos(0, 2))); // Should be swapped
             Assert.AreEqual(9, _board.MovesRemaining);
        }

        [Test]
        public void Test_Swap_InvalidMatch_Reverts()
        {
            _board.SetTile(new GridPos(0, 0), TileType.Red);
            _board.SetTile(new GridPos(0, 1), TileType.Blue);
            
            bool result = _swapService.TrySwap(new GridPos(0, 0), new GridPos(0, 1));
            
            Assert.IsFalse(result);
            Assert.IsInstanceOf<SwapReverted>(_events[0]);
            Assert.AreEqual(TileType.Red, _board.GetTile(new GridPos(0, 0))); // Reverted
            Assert.AreEqual(10, _board.MovesRemaining);
        }

        [Test]
        public void Test_Swap_NonAdjacent()
        {
            _board.SetTile(new GridPos(0, 0), TileType.Red);
            _board.SetTile(new GridPos(2, 2), TileType.Blue);
            
            bool result = _swapService.TrySwap(new GridPos(0, 0), new GridPos(2, 2));
            
            Assert.IsFalse(result);
            Assert.AreEqual(0, _events.Count);
        }
    }
}

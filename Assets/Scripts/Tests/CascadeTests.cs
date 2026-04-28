using System.Collections.Generic;
using NUnit.Framework;
using IslandMatch.Board.Model;
using IslandMatch.Board.Services;

namespace IslandMatch.Tests
{
    [TestFixture]
    public class CascadeTests
    {
        private BoardModel _board;
        private ResolveService _resolveService;
        private List<BoardEvent> _events;

        [SetUp]
        public void Setup()
        {
            _board = new BoardModel(4, 5); // 4 width, 5 height
            _events = new List<BoardEvent>();
            _board.OnEvent += (evt) => _events.Add(evt);
            
            // Seeded random for determinism
            var random = new SeededRandomProvider(12345);
            _resolveService = new ResolveService(_board, random);
        }

        [Test]
        public void Test_Resolve_SimpleMatch_ClearsAndRefills()
        {
            // Setup a match at bottom
            // R R R .
            // B B . .
            // . . . .
            _board.SetTile(new GridPos(0, 0), TileType.Red);
            _board.SetTile(new GridPos(0, 1), TileType.Red);
            _board.SetTile(new GridPos(0, 2), TileType.Red);
            
            // Fill others with known non-matching stuff to avoid cascading matches for this simple test
            _board.SetTile(new GridPos(0, 3), TileType.Blue);
            
            // Top rows
            for(int y=1; y<5; y++)
            {
                for(int x=0; x<4; x++)
                {
                    _board.SetTile(new GridPos(y, x), TileType.Green);
                }
            }

            _resolveService.ResolveUntilStable();

            // Verify
            // 1. Match Found & Cleared
            Assert.IsTrue(_events.Exists(e => e is TilesMatched), "Should emit TilesMatched");
            Assert.IsTrue(_events.Exists(e => e is TilesCleared), "Should emit TilesCleared");

            // 2. Gravity (Tiles Above Fall)
            Assert.IsTrue(_events.Exists(e => e is TilesFell), "Should emit TilesFell");
            
            // 3. Spawn (New tiles at top)
            Assert.IsTrue(_events.Exists(e => e is TilesSpawned), "Should emit TilesSpawned");
            
            // Check state:
            // (0,0), (0,1), (0,2) was Red, now should be Green (fell from above)
            Assert.AreEqual(TileType.Green, _board.GetTile(new GridPos(0, 0)));
        }
    }
}

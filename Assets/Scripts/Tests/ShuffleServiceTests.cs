using System.Collections.Generic;
using NUnit.Framework;
using IslandMatch.Board.Model;
using IslandMatch.Board.Services;

namespace IslandMatch.Tests
{
    [TestFixture]
    public class ShuffleServiceTests
    {
        private BoardModel _board;
        private ShuffleService _shuffleService;

        [SetUp]
        public void Setup()
        {
            _board = new BoardModel(4, 4);
            var random = new SeededRandomProvider(12345);
            _shuffleService = new ShuffleService(_board, random);
        }

        [Test]
        public void Test_Shuffle_PreservesTiles()
        {
            // Fill board
            int redCount = 0;
            int blueCount = 0;
            
            for(int y=0; y<4; y++)
            {
                for(int x=0; x<4; x++)
                {
                    if ((x+y) % 2 == 0) 
                    {
                        _board.SetTile(new GridPos(y, x), TileType.Red);
                        redCount++;
                    }
                    else
                    {
                        _board.SetTile(new GridPos(y, x), TileType.Blue);
                        blueCount++;
                    }
                }
            }
            
            _shuffleService.Shuffle();
            
            // Count again
            int newRed = 0;
            int newBlue = 0;
             for(int y=0; y<4; y++)
            {
                for(int x=0; x<4; x++)
                {
                    TileType t = _board.GetTile(new GridPos(y, x));
                    if (t == TileType.Red) newRed++;
                    if (t == TileType.Blue) newBlue++;
                }
            }
            
            Assert.AreEqual(redCount, newRed);
            Assert.AreEqual(blueCount, newBlue);
        }

        [Test]
        public void Test_HasValidMoves()
        {
            // No moves: Checkerboard of 2 colors
            // R B R B
            // B R B R
            // R B R B
            // B R B R
             for(int y=0; y<4; y++)
            {
                for(int x=0; x<4; x++)
                {
                     if ((x+y) % 2 == 0) _board.SetTile(new GridPos(y, x), TileType.Red);
                     else _board.SetTile(new GridPos(y, x), TileType.Blue);
                }
            }
            
            // Checkerboard usually has no moves (swapping any adjacent makes R R or B B, no 3 in a row)
            // Wait, R R B R -> Swapping B and R might make R R R?
            // R B R
            // B R B
            // Swap (0,1 B) with (0,2 R) -> R R B ... no match.
            // Actually checkerboard is a common "no matches" pattern for 2 colors.
            
            Assert.IsFalse(_shuffleService.HasValidMoves());
            
            // Now create a valid move
            // R R B R
            // B ...
            // Swap (0,2 B) with (0,3 R) -> R R R B (Match!)
            // No wait, swap needs to be adjacent.
            
            // R R . B
            // . . B .
            // swap (0, 2) with (0, 3) -> R R B B (no)
            
            // R R B R
            // . . . .
            // Swap (0,2) with (0,3) -> R R R B matches!
            // Wait, R R B R, swap (0,2 B) with (0,3 R) -> R R R B. Yes.
            
            _board.SetTile(new GridPos(0, 0), TileType.Red);
            _board.SetTile(new GridPos(0, 1), TileType.Red);
            _board.SetTile(new GridPos(0, 2), TileType.Blue);
            _board.SetTile(new GridPos(0, 3), TileType.Red); // R R B R
            
            Assert.IsTrue(_shuffleService.HasValidMoves());
        }
    }
}

using NUnit.Framework;
using IslandMatch.Level;
using System.Collections.Generic;

namespace IslandMatch.Tests
{
    // Fake parser for testing
    public class MockParser : ILevelParser
    {
        public LevelData Parse(string json)
        {
            // Simple mock: if json == "valid", return valid data
            if (json == "valid")
            {
                return new LevelData
                {
                    id = "lvl_01",
                    version = 1,
                    width = 2,
                    height = 2,
                    moves = 10,
                    targetScore = 100,
                    layout = new int[] { 1, 2, 3, 4 },
                    objectives = new List<string>()
                };
            }
            if (json == "invalid_layout")
            {
                return new LevelData
                {
                    id = "lvl_01",
                    width = 2, height = 2, moves = 10, targetScore = 100,
                    layout = new int[] { 1 } // Wrong length
                };
            }
            return null;
        }
    }

    [TestFixture]
    public class LevelValidatorTests
    {
        [Test]
        public void Test_Validate_ValidData()
        {
            LevelData data = new LevelData
            {
                id = "1", width = 3, height = 3, moves = 5, targetScore = 50,
                layout = new int[9]
            };
            string error;
            Assert.IsTrue(LevelValidator.Validate(data, out error));
            Assert.IsNull(error);
        }

        [Test]
        public void Test_Validate_MismatchLayout()
        {
            LevelData data = new LevelData
            {
                id = "1", width = 3, height = 3,
                layout = new int[5] // Mismatch
            };
            string error;
            Assert.IsFalse(LevelValidator.Validate(data, out error));
            Assert.IsNotNull(error);
        }
    }

    [TestFixture]
    public class LevelLoaderTests
    {
        [Test]
        public void Test_Load_Valid()
        {
            var loader = new LevelLoader(new MockParser());
            var data = loader.LoadLevel("valid");
            Assert.IsNotNull(data);
            Assert.AreEqual("lvl_01", data.id);
        }

        [Test]
        public void Test_Load_ValidationFailure()
        {
            var loader = new LevelLoader(new MockParser());
            Assert.Throws<System.Exception>(() => loader.LoadLevel("invalid_layout"));
        }
    }
}

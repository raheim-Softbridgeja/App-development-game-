using System;

namespace IslandMatch.Board.Model
{
    public interface IRandomProvider
    {
        int NextInt(int minInclusive, int maxExclusive);
    }

    public class SystemRandomProvider : IRandomProvider
    {
        private readonly Random _random = new Random();

        public int NextInt(int minInclusive, int maxExclusive)
        {
            return _random.Next(minInclusive, maxExclusive);
        }
    }

    public class SeededRandomProvider : IRandomProvider
    {
        private readonly Random _random;

        public SeededRandomProvider(int seed)
        {
            _random = new Random(seed);
        }

        public int NextInt(int minInclusive, int maxExclusive)
        {
            return _random.Next(minInclusive, maxExclusive);
        }
    }
}

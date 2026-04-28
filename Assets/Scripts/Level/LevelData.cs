using System;
using System.Collections.Generic;

namespace IslandMatch.Level
{
    [Serializable]
    public class LevelData
    {
        // "LevelData minimum fields: id, version, width, height, moves, targetScore, layout (flattened int array), objectives (list)"
        
        public string id;
        public int version;
        public int width;
        public int height;
        public int moves;
        public int targetScore;
        public int[] layout;
        public List<string> objectives; // Simplified for now, can be complex objects if needed
    }
    
    public interface ILevelParser
    {
        LevelData Parse(string json);
    }
}

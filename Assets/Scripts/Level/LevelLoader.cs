using System;

namespace IslandMatch.Level
{
    public class LevelLoader
    {
        private readonly ILevelParser _parser;

        public LevelLoader(ILevelParser parser)
        {
            _parser = parser;
        }

        public LevelData LoadLevel(string json)
        {
            if (string.IsNullOrEmpty(json))
                throw new ArgumentException("JSON cannot be empty");

            LevelData data = _parser.Parse(json);
            
            string error;
            if (!LevelValidator.Validate(data, out error))
            {
                throw new Exception($"Validation Failed: {error}");
            }

            return data;
        }
    }
}

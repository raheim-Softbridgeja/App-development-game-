using System;
using System.Collections.Generic;
using System.Linq;

namespace IslandMatch.Level
{
    public class LevelValidator
    {
        public static bool Validate(LevelData data, out string error)
        {
            error = null;
            if (data == null)
            {
                error = "LevelData is null";
                return false;
            }

            if (string.IsNullOrEmpty(data.id))
            {
                error = "Missing ID";
                return false;
            }

            if (data.width <= 0 || data.height <= 0)
            {
                error = $"Invalid dimensions: {data.width}x{data.height}";
                return false;
            }

            if (data.moves <= 0)
            {
                error = "Moves must be > 0";
                return false;
            }

            if (data.targetScore <= 0)
            {
                error = "TargetScore must be > 0";
                return false;
            }

            if (data.layout == null)
            {
                error = "Layout is null";
                return false;
            }

            if (data.layout.Length != data.width * data.height)
            {
                error = $"Layout length ({data.layout.Length}) does not match dimensions ({data.width}x{data.height} = {data.width * data.height})";
                return false;
            }

            return true;
        }
    }
}

using UnityEngine;
using IslandMatch.Board.Model;
using IslandMatch.Board.Services;
using IslandMatch.Level;

public class GameManager : MonoBehaviour
{
    [Header("References")]
    public BoardController boardController;
    public InputManager inputManager;
    public UIController uiController;
    
    [Header("Configuration")]
    public string levelId = "level_001";
    public TextAsset levelJsonFile; // Drag and drop helper, or use Resources

    private BoardModel _board;
    private ResolveService _resolveService;
    private SwapService _swapService;
    private ShuffleService _shuffleService;

    void Start()
    {
        // 1. Load Level
        string json = "";
        if (levelJsonFile != null)
        {
            json = levelJsonFile.text;
        }
        else
        {
            // Fallback load from Resources
            var asset = Resources.Load<TextAsset>($"Levels/{levelId}");
            if (asset != null) json = asset.text;
        }

        if (string.IsNullOrEmpty(json))
        {
            Debug.LogError($"Could not load level {levelId}");
            return;
        }

        // We need a JSON parser adapter if we want to stick to pure C# rules for LevelLoader,
        // but since GameManager is a MonoBehavior, we can use Unity's JsonUtility here OR pass a parser.
        // My LevelLoader expects ILevelParser.
        // Let's make a simple wrapper using UnityEngine.JsonUtility or a simple manual parser.
        // Since LevelData is simple, I'll use a UnityJsonParser.
        
        ILevelParser parser = new UnityJsonParser();
        LevelLoader loader = new LevelLoader(parser);
        LevelData data = loader.LoadLevel(json);

        // 2. Initialize Model
        _board = new BoardModel(data.width, data.height);
        _board.SetMoves(data.moves);
        _board.SetScore(0);

        // Random
        IRandomProvider random = new SystemRandomProvider();

        // 3. Initialize Services
        _resolveService = new ResolveService(_board, random);
        _swapService = new SwapService(_board);
        _shuffleService = new ShuffleService(_board, random);

        // 4. Fill Board (Initial)
        // Parse layout
        for (int i = 0; i < data.layout.Length; i++)
        {
            int row = i / data.width;
            int col = i % data.width;
            TileType t = (TileType)data.layout[i];
            _board.SetTile(new GridPos(row, col), t);
        }

        // 5. Wire Visuals
        boardController.Initialize(_board);
        inputManager.Initialize(_swapService, _board, boardController.tileSize);
        uiController.Initialize(_board);
        
        // 6. Initial Resolve (in case level has matches)
        _resolveService.ResolveUntilStable();
    }

    void Update()
    {
        // Simple loop check
        // In real game, this would be event driven or state machine
        if (UnityEngine.Input.GetKeyDown(KeyCode.Space))
        {
            _resolveService.ResolveUntilStable();
        }
    }
}

// Simple adapter for Unity's JSON generic
public class UnityJsonParser : ILevelParser
{
    public LevelData Parse(string json)
    {
        return JsonUtility.FromJson<LevelData>(json);
    }
}

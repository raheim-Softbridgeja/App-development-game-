using UnityEngine;
using UnityEngine.UI; // Or TMPro
using IslandMatch.Board.Model;

public class UIController : MonoBehaviour
{
    public Text scoreText; // Legacy Text for simple wiring, or TextMeshProUGUI
    public Text movesText;

    private BoardModel _board;

    public void Initialize(BoardModel board)
    {
        _board = board;
        _board.OnEvent += OnBoardEvent;
        UpdateUI();
    }

    private void OnDestroy()
    {
        if (_board != null) _board.OnEvent -= OnBoardEvent;
    }

    private void OnBoardEvent(BoardEvent evt)
    {
        if (evt is ScoreGained || evt is SwapCommitted || evt is TilesMatched)
        {
            UpdateUI();
        }
    }

    private void UpdateUI()
    {
        if (scoreText) scoreText.text = $"Score: {_board.Score}";
        if (movesText) movesText.text = $"Moves: {_board.MovesRemaining}";
    }
}

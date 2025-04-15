import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EngineABGivenDeep{
    private final PositionEvaluator heuristic;
    private final ABPosition abPosition;
    EngineABGivenDeep(PositionEvaluator heuristic, ABPosition position) {
        this.heuristic = heuristic;
        this.abPosition = position;
    }
    public List<Object> choseMove(Position position, int deep) {
        abPosition.init(position);
        Set<Object> moves = abPosition.getMoves();

        List<Object> bestMoves = new ArrayList<>();
        for (int d = 1; d <= deep; d++) {
            double alpha = -1;
            bestMoves.clear();
            List<Object> losingMoves = new ArrayList<>();
            boolean win = false;
            for (Object move : moves) {
                position.applyMove(move);
                abPosition.init(position);
                abPosition.initIterator();
                double value = -abSearch(d - 1, -1, -alpha);
                position.revertMove(move);

                if (value == 1)
                    win = true;
                if (value == -1)
                    losingMoves.add(move);
                if (value > alpha) {
                    alpha = value;
                    bestMoves.clear();
                }
                if (value == alpha && value > -1)
                    bestMoves.add(move);
            }
            if (win || moves.size() - losingMoves.size() <= 1)
                return bestMoves;
            losingMoves.forEach(moves::remove);
        }
        return bestMoves;
    }
    private double abSearch(int deep, double alpha, double beta) {
        GameState state = abPosition.getPosition().state();
        if (state == GameState.WIN)
            return -1;
        if (state == GameState.DRAW)
            return 0;
        if (deep == 0)
            return -heuristic.eval(abPosition.getPosition());

        double maxValue = -1;
        while (abPosition.next(deep > 1)) {
            double value = -abSearch(deep - 1, -beta, -alpha);
            abPosition.back(deep > 1);

            if (value > maxValue) {
                maxValue = value;
                if (value > alpha) {
                    alpha = value;
                    if (value > beta || value == 1) // or >= and choose first best move
                        return value;
                }
            }
        }
        return maxValue;
    }
}

import java.util.ArrayList;
import java.util.List;

public class EngineNodeCounter extends EngineABGivenDeep{
    private final PositionEvaluator heuristic;
    private final ABPosition abPosition;
    private long visitedNodes;
    private long allVisitedNodes;
    EngineNodeCounter(PositionEvaluator heuristic, ABPosition position) {
        super(null, null);
        this.heuristic = heuristic;
        this.abPosition = position;
    }
    public List<Object> choseMove(Position position, int deep) {
        abPosition.init(position);
        visitedNodes = 0;

        for(int d = 1; true; d++) {
            System.out.println("Deep: " + d);
            double alpha = -1;
            List<Object> bestMoves = new ArrayList<>();
            double value = 0;
            for (abPosition.initIterator(); abPosition.next(d == 1); abPosition.back(d == 1, value)) {
                value = -search(d - 1);
                if (value > alpha) {
                    alpha = value;
                    bestMoves.clear();
                }
                if (value == alpha && value > -1)
                    bestMoves.add(abPosition.getMove());
            }
            if (value == 1 || d == deep) {
                allVisitedNodes += visitedNodes;
                System.out.println("Visited nodes: " + visitedNodes);
                System.out.println("All Visited nodes: " + allVisitedNodes);
                return bestMoves;
            }
        }
    }
    private double search(int deep) {
        visitedNodes++;
        if (deep == 0)
            return 0;
        GameState state = abPosition.getPosition().state();
        if (state == GameState.WIN)
            return -1;
        if (state == GameState.DRAW)
            return 0;

        double maxValue = -1;
        while (abPosition.next(deep == 1)) {
            double value = -search(deep - 1);
            abPosition.back(deep == 1, value);
            if (value > maxValue)
                maxValue = value;
        }
        return maxValue;
    }
}

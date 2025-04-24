import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeoutException;

public class EngineAB implements Engine{
    private final PositionEvaluator heuristic;
    private final ABPosition abPosition;
    private long deadline;
    private boolean searchedFullTree;
    private final ThreadMXBean bean = ManagementFactory.getThreadMXBean();
    private long visitedNodes = 0;
    EngineAB(PositionEvaluator heuristic, ABPosition position) {
        this.heuristic = heuristic;
        this.abPosition = position;
    }
    @Override
    public Object choseMove(Position position, long deadline) {
        this.deadline = deadline;
        abPosition.init(position);
        Object prevBestMove = null;

        visitedNodes = 0;
        searchedFullTree = false;
        for(int deep = 1; !searchedFullTree; deep++) {
            System.out.println("Deep: "+deep);
            searchedFullTree = true;
            double alpha = -1;
            Object bestMove = null;
            double value;
            int safeMoves = 0;
            for (abPosition.initIterator(); abPosition.next(deep == 1); abPosition.back(deep == 1, value)) {
                try {
                    value = -abSearch(deep - 1, -1, -alpha);
                } catch (TimeoutException e) {
                    System.out.println("Nodes: "+ visitedNodes);
                    return prevBestMove;
                }
                if (value > -1)
                    safeMoves++;
                if (value > alpha) {
                    bestMove = abPosition.getMove();
                    if (value == 1)
                        return bestMove;
                }
            }
            if (safeMoves == 1)
                return bestMove;
            if (bestMove == null)
                return prevBestMove;
            prevBestMove = bestMove;
        }
        return prevBestMove;
    }
    private double abSearch(int deep, double alpha, double beta) throws TimeoutException {
        visitedNodes++;
        if (bean.getCurrentThreadCpuTime() >= deadline)
            throw new TimeoutException();

        GameState state = abPosition.getPosition().state();
        if (state == GameState.WIN)
            return -1;
        if (state == GameState.DRAW)
            return 0;
        if (deep == 0) {
            searchedFullTree = false;
            return -heuristic.eval(abPosition.getPosition());
        }

        double maxValue = -1;
        while (abPosition.next(deep == 1)) {
            double value = -abSearch(deep - 1, -beta, -alpha);
            abPosition.back(deep == 1, value);

            if (value > maxValue) {
                maxValue = value;
                if (value > alpha) {
                    alpha = value;
                    if (value >= beta)
                        return value;
                }
            }
        }
        return maxValue;
    }
}

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;

public class EngineAB implements Engine{
    private final PositionEvaluator heuristic;
    private long deadline;
    private boolean searchedFullTree;
    private final ThreadMXBean bean = ManagementFactory.getThreadMXBean();
    EngineAB(PositionEvaluator heuristic) {
        this.heuristic = heuristic;
    }
    @Override
    public Position choseMove(Position position, long deadline) {
        this.deadline = deadline;
        Position bestMove = null;

        for(int deep = 1; true; deep++) {
            System.out.println("Deep: "+deep);
            searchedFullTree = true;
            double alpha = -1;
            List<Position> bestMoves = new ArrayList<>();
            for (Position move : position.getChildren()) {
                try {
                    double value = -abSearch(move, deep - 1, -1, -alpha);
                    if (value == 1)
                        return move;
                    if (value > alpha) {
                        alpha = value;
                        bestMoves.clear();
                    }
                    if (value == alpha)
                        bestMoves.add(move);
                } catch (TimeoutException e) {
                    return bestMove;
                }
            }
            bestMove = bestMoves.get((new Random()).nextInt(bestMoves.size()));
            if (searchedFullTree)
                return bestMove;
        }
    }
    private double abSearch(Position position, int deep, double alpha, double beta) throws TimeoutException {
        if (bean.getCurrentThreadCpuTime() >= deadline)
            throw new TimeoutException();

        if (position.state() == GameState.WIN)
            return -1;
        if (position.state() == GameState.DRAW)
            return 0;
        if (deep == 0) {
            searchedFullTree = false;
            return -heuristic.eval(position);
        }

        double maxValue = -1;
        while (position.advanceToNext()) {
            double value = -abSearch(position, deep - 1, -beta, -alpha);
            position.revertToParent();

            if (value > maxValue) {
                maxValue = value;
                if (value > alpha) {
                    alpha = value;
                    if (value > beta) // or >= and choose first best move
                        return value;
                }
            }
        }
        return maxValue;
    }
}

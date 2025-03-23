import java.util.TreeSet;

public class HeuristicBreakthrough implements PositionEvaluator{
    @Override
    public double eval(Position position) {
        if (!(position instanceof PositionBreakthrough pos))
            throw new IllegalArgumentException();

        int pl = 1 - pos.getPlOnTurn();
        TreeSet<Integer>[] pieces = pos.getPieces();

        int[] points = {pieces[0].size(), pieces[1].size()};

        return (points[pl] - points[1 - pl]) / (points[pl] + points[1 - pl] + 1.0);
    }
}

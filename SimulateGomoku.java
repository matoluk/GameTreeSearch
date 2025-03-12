import java.util.Random;

public class SimulateGomoku implements PositionEvaluator{
    private static final Random rand = new Random();
    @Override
    public double eval(Position position) {
        if (!(position instanceof PositionGomoku))
            throw new IllegalArgumentException();
        PositionGomoku pos = new PositionGomoku((PositionGomoku) position);
        boolean firstPl = true;
        while (pos.state() == GameState.ONGOING) {
            pos.childOrdStack.push(rand.nextInt(pos.possibleMoves.size()));
            pos.advanceToNext();
            firstPl = !firstPl;
        }
        if (pos.state() == GameState.DRAW)
            return 0;
        return firstPl ? 1 : -1;
    }
}

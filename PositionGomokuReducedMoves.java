import java.util.*;

public class PositionGomokuReducedMoves extends PositionGomoku{
    private static final int DISTANCE = 2;
    private final Stack<Integer> possibleMovesSizes = new Stack<>();
    PositionGomokuReducedMoves(int size) {
        super(size);
        Move center = new Move(size / 2, size / 2);
        possibleMoves = List.of(center);
    }
    PositionGomokuReducedMoves(PositionGomoku pos) {
        super(pos);

        Set<Move> nearMoves = new HashSet<>();
        for (Move move = new Move(); move != null; move = move.next(getSize()))
            if (get(move) != 0)
                nearMoves.addAll(relatedMoves(move));
        if (nearMoves.isEmpty()) {
            Move center = new Move(getSize() / 2, getSize() / 2);
            if (get(center) == 0)
                nearMoves.add(center);
        }

        possibleMoves = new ArrayList<>(nearMoves);
    }
    private PositionGomokuReducedMoves(PositionGomokuReducedMoves pos) {
        super(pos);
    }
    private Set<Move> relatedMoves(Move move) {
        Set<Move> relatedMoves = new HashSet<>();
        Move from = new Move(
                Integer.max(0, move.x - DISTANCE),
                Integer.max(0, move.y - DISTANCE));
        Move to = new Move(
                Integer.min(getSize() - 1, move.x + DISTANCE),
                Integer.min(getSize() - 1, move.y + DISTANCE));
        Move m = new Move(from.x, from.y);
        while(true) {
            if (get(m) == 0)
                relatedMoves.add(new Move(m.x, m.y));
            m.x++;
            if (m.x > to.x) {
                m.x = from.x;
                m.y++;
                if (m.y > to.y)
                    return relatedMoves;
            }
        }
    }

    @Override
    protected void findPossibleMoves() {
        Move center = new Move(getSize() / 2, getSize() / 2);
        possibleMoves = List.of(center);
    }

    @Override
    protected PositionGomoku copy() {
        return new PositionGomokuReducedMoves(this);
    }

    @Override
    protected void updateStacks(int moveId) {
        super.updateStacks(moveId);
        possibleMovesSizes.push(possibleMoves.size() - 1);
    }
    @Override
    protected void updatePossibleMoves(int moveId) {
        Move move = possibleMoves.get(moveId);
        super.updatePossibleMoves(moveId);

        Set<Move> nearMoves = new HashSet<>(possibleMoves);
        for(Move m : relatedMoves(move))
            if (!nearMoves.contains(m))
                possibleMoves.add(m);
    }
    @Override
    public void revertToParent() {
        possibleMoves.subList(possibleMovesSizes.pop(), possibleMoves.size()).clear();
        super.revertToParent();
    }
}

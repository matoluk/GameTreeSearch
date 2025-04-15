import java.util.Set;

public interface ABPosition {
    void init(Position pos);
    boolean next(boolean updatePossibleMoves);
    //TODO: if leaf then not necessary to update moves, but ALSO ITERATORS.
    void back(boolean updatePossibleMoves);
    void initIterator();
    Object getMove();
    Position getPosition();
    Set<Object> getMoves();
}

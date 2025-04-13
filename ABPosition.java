public interface ABPosition {
    void init(Position pos);
    boolean next(boolean updatePossibleMoves);
    void back(boolean updatePossibleMoves);
    void initIterator();
    Object getMove();
    Position getPosition();
}

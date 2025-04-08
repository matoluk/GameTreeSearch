import java.util.List;

public interface Position {
    Position copy();
    List<Object> moves();
    GameState state();
    Position applyMove(Object move);
    void revertMove(Object move);
}

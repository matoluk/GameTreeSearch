import java.util.List;

public interface Position {
    List<Position> getChildren();
    boolean advanceToNext();
    void revertToParent();
    GameState state();
}

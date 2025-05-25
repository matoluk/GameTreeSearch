import java.util.TreeSet;

public class HeuristicBreakthrough implements PositionEvaluator{
    static final int[] table = {5,15,15,5,5,15,15,5,2,3,3,3,3,3,3,2,4,6,6,6,6,6,6,4,7,10,10,10,10,10,10,7,11,15,15,15,15,15,15,11,16,21,21,21,21,21,21,16,20,28,28,28,28,28,28,20,36,36,36,36,36,36,36,36};
    static final int[] safeTable = {7,22,22,7,7,22,22,7,3,4,4,4,4,4,4,3,6,9,9,9,9,9,9,6,10,15,15,15,15,15,15,10,16,22,22,22,22,22,22,16,24,31,31,31,31,31,31,24,30,42,42,42,42,42,42,30,54,54,54,54,54,54,54,54};

    @Override
    public double eval(Position position) {
        if (!(position instanceof PositionBreakthrough pos))
            throw new IllegalArgumentException();

        int[] points = {0, 0};
        int dif = pos.pieces[0].size() - pos.pieces[1].size();
        if (dif != 0) {
            if (dif > 0)
                points[0] = 10 * dif;
            else
                points[1] = (-10) * dif;
        }

        for (int pl = 0; pl < 2; pl++) {
            int dir = 8 - 16 * pl;
            for (int piece : pos.pieces[pl]){
                int safety = 0;
                if (piece % 8 > 0) {
                    if (pos.pieces[1 - pl].contains(piece + dir - 1))
                        safety--;
                    if (pos.pieces[pl].contains(piece - dir - 1))
                        safety++;
                }
                if (piece % 8 < 7) {
                    if (pos.pieces[1 - pl].contains(piece + dir + 1))
                        safety--;
                    if (pos.pieces[pl].contains(piece - dir + 1))
                        safety++;
                }
                if (pl == 1)
                    piece = 63 - piece;
                if (safety >= 0)
                    points[pl] += safeTable[piece];
                else
                    points[pl] += table[piece];
            }
        }

        return (points[1 - pos.actualPlayer] - points[pos.actualPlayer]) / (points[0] + points[1] + 1.0);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++)
                System.out.print(safeTable[8*i+j] + "\t");
            System.out.println();
        }
    }
}

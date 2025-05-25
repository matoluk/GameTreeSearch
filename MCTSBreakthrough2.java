import java.util.*;

public class MCTSBreakthrough2 implements MCTSPosition{
    PositionBreakthrough positionOrig;
    PositionBreakthrough position;
    int[] boardControlOrig;
    int[] boardControl;
    private static final Random rand = new Random();
    static int[] table = HeuristicBreakthrough.table;
    static int[] safeTable = HeuristicBreakthrough.safeTable;
    static int[] dif = new int[table.length];
    static {
        for (int i = 0; i < table.length; i++)
            dif[i] = HeuristicBreakthrough.safeTable[i] - table[i];
    }
    int maxSimDeep;
    PositionEvaluator heuristic = new HeuristicBreakthrough();
    MCTSBreakthrough2() {
        maxSimDeep = 177;
    }
    MCTSBreakthrough2(int maxSimulationDeep) {
        maxSimDeep = maxSimulationDeep;
    }
    int place(int place, int pl) {
        return place + pl * (63 - 2 * place);
    }
    int evalMove(PositionBreakthrough.Move m) {
        int points = 20;

        if (m.to >= 56 || m.to < 8)
            return 1000000; //todo
        int pl = this.position.actualPlayer;
        int to = m.to % 8 - m.from % 8;
        int sign = 1 - 2 * pl;
        int opPl = 1 - pl;

        points -= boardControl[m.from] * sign >= 0 ? safeTable[place(m.from, pl)] : table[place(m.from, pl)];
        if (to != -1 && m.from % 8 > 0) {
            int left = m.to - to - 1;
            if (position.pieces[pl].contains(left) && boardControl[left] == 0)
                points -= dif[place(left, pl)];
            else if (position.pieces[opPl].contains(left) && boardControl[left] == sign)
                points -= dif[place(left, opPl)];
        }
        if (to != 1 && m.from % 8 < 7) {
            int right = m.to - to + 1;
            if (position.pieces[pl].contains(right) && boardControl[right] == 0)
                points -= dif[place(right, pl)];
            else if (position.pieces[opPl].contains(right) && boardControl[right] == sign)
                points -= dif[place(right, opPl)];
        }

        if (m.takes) {
            if (m.from < 8 || m.from >= 56)
                return 100000;
            points += 10 + (boardControl[m.to] * sign > 0 ? table[place(m.to, opPl)] : safeTable[place(m.to, opPl)]);
            if (to == -1) {
                if (m.to % 8 > 0) {
                    int left = m.from - 2;
                    if (position.pieces[opPl].contains(left) && boardControl[left] == 0)
                        points += dif[place(left, opPl)];
                }
            }
            else {
                if (m.to % 8 < 7) {
                    int right = m.from + 2;
                    if (position.pieces[opPl].contains(right) && boardControl[right] == 0)
                        points += dif[place(right, opPl)];
                }
            }
        }

        int control = boardControl[m.to] * sign;
        if (to != 0)
            control--;
        points += control >= 0 ? safeTable[place(m.to, pl)] : table[place(m.to, pl)];

        if (m.to % 8 > 0) {
            int left = m.to + 8 * sign - 1;
            if (position.pieces[pl].contains(left) && boardControl[left] == -sign)
                points += dif[place(left, pl)];
            else if (position.pieces[opPl].contains(left) && boardControl[left] == 0)
                points += dif[place(left, opPl)];
        }
        if (m.to % 8 < 7) {
            int right = m.to + 8 * sign + 1;
            if (position.pieces[pl].contains(right) && boardControl[right] == -sign)
                points += dif[place(right, pl)];
            else if (position.pieces[opPl].contains(right) && boardControl[right] == 0)
                points += dif[place(right, opPl)];
        }

        if (points < 1)
            points = 1;
        points = points * points;
        return points;
    }
    @Override
    public void init(Position pos) {
        if (!(pos instanceof PositionBreakthrough))
            throw new IllegalArgumentException("Expected PositionBreakthrough");

        positionOrig = (PositionBreakthrough) pos;
        assert positionOrig.size == 8;
        boardControlOrig = new int[64];
        //movesPriorityOrig = new int[2][511];

        for (int pl = 0; pl < 2; pl++) {
            int sign = 1 - 2 * pl;
            for (int piece : positionOrig.pieces[pl]) {
                if (piece % 8 > 0)
                    boardControlOrig[piece + sign * 8 - 1] += sign;
                if (piece % 8 < 7)
                    boardControlOrig[piece + sign * 8 + 1] += sign;
            }
        }
    }

    @Override
    public void init() {
        position = new PositionBreakthrough(positionOrig);
        boardControl = Arrays.copyOf(boardControlOrig, 64);
    }
    @Override
    public void applyMove(Object move) {
        position.applyMove(move);
        if (position.state == GameState.WIN)
            return;

        int sign = 2 * position.actualPlayer - 1;
        PositionBreakthrough.Move m = (PositionBreakthrough.Move) move;
        if (m.from % 8 > 0)
            boardControl[m.from + 8 * sign - 1] -= sign;
        if (m.from % 8 < 7)
            boardControl[m.from + 8 * sign + 1] -= sign;
        if (m.to % 8 > 0) {
            boardControl[m.to + 8 * sign - 1] += sign;
            if (m.takes)
                boardControl[m.to - 8 * sign - 1] += sign;
        }
        if (m.to % 8 < 7) {
            boardControl[m.to + 8 * sign + 1] += sign;
            if (m.takes)
                boardControl[m.to - 8 * sign + 1] += sign;
        }
    }

    @Override
    public Map<Object, Double> getEvaluatedMoves() {
        Map<Object, Double> moves = new HashMap<>();
        for (Object move : position.moves())
            moves.put(move, evalMove((PositionBreakthrough.Move) move) / (double) Integer.MAX_VALUE);
        return moves;
    }

    @Override
    public double simulate() {
        int pl = position.actualPlayer;

        for (int simStep = 0; simStep < maxSimDeep; simStep++) {
            if (position.state == GameState.WIN)
                return 1 - (simStep & 1) * 2;

            double allPoints = 0;
            List<Object> moves = position.moves();
            PositionBreakthrough.Move selectedMove = (PositionBreakthrough.Move) moves.get(0);
            for (Object move : moves) {
                PositionBreakthrough.Move m = (PositionBreakthrough.Move) move;
                int points = evalMove(m);
                allPoints += points;
                if (rand.nextDouble() < points / allPoints)
                    selectedMove = m;
            }
            applyMove(selectedMove);
        }

        position.actualPlayer = pl;
        double score = heuristic.eval(position);

        if (score == 0)
            return 0;
        if (score > 0.25)
            return 1;
        if (score < -0.25)
            return -1;
        return score * 4;
    }

    @Override
    public Position getPosition() {
        return position;
    }
}

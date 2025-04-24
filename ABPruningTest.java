import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

public class ABPruningTest {
    public static final ThreadMXBean bean = ManagementFactory.getThreadMXBean();
    static int deep = 3;

    public static void main(String[] args) {
        /*List<PositionGomoku> list = new ArrayList<>((Set<PositionGomoku>) read("setGomokuPositions"));
        Collections.shuffle(list);
        List<PositionGomoku> positions = list.subList(0, Math.min(100, list.size()));
        write(new HashSet<>(positions), "random100");*/

        Set<PositionGomoku> positions = (Set<PositionGomoku>) read("random100");
        System.out.println("Positions count: " + positions.size());

        EngineABGivenDeep[] engines = {
                new EngineNodeCounter(new HeuristicGomoku(), new ABGomokuRelatedMoves()),
                new EngineABGivenDeep(new HeuristicGomoku(), new ABGomokuRelatedMoves()),
                new EngineABGivenDeep(new HeuristicGomoku(), new ABGomokuSortMoves())
        };
        int engCount = engines.length;
        int sameRes = 0;
        int tests = 0;
        long[] time = new long[engCount];

        for (PositionGomoku pos : positions) {
            System.out.println(pos);

            List<TreeSet<PositionGomoku.Move>> moves = new ArrayList<>();
            long[] durations = new long[engCount];
            for (int i = 0; i < engCount; i++) {
                long startTime = bean.getCurrentThreadCpuTime();
                List<Object> bestMoves = engines[i].choseMove(pos.copy(), deep);
                durations[i] = bean.getCurrentThreadCpuTime() - startTime;
                time[i] += durations[i];

                TreeSet<PositionGomoku.Move> posSet = new TreeSet<>();
                for (Object m : bestMoves)
                    posSet.add((PositionGomoku.Move) m);
                moves.add(posSet);
                System.out.println("Turn duration: " + durations[i] / 1_000_000.0 + "ms");
                System.out.println("Moves: " + moves.get(i));
            }
            tests++;

            if (moves.get(1).equals(moves.get(2)))
                sameRes++;
        }

        System.out.println("Success: " + sameRes + " / " + tests);
        System.out.println("Time: -, " + time[1] / 1_000_000.0 + ", " + time[2] / 1_000_000.0);
    }
    static void write(Object obj, String fileName) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName + ".dat"))) {
            out.writeObject(obj);
            System.out.println("File wrote.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static Object read(String fileName) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName + ".dat"))) {
            return in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
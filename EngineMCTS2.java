import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

public class EngineMCTS2 implements Engine{
    private static final double EXPLORATION_PARAM = 1;
    private static final Random rand = new Random();
    private final MCTSPosition mctsPosition;
    EngineMCTS2(MCTSPosition pos) {
        mctsPosition = pos;
    }
    @Override
    public Object choseMove(Position position, long deadline) {
        mctsPosition.init(position);
        Node root = new Node(null, null);
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        int count = 0;
        while (bean.getCurrentThreadCpuTime() < deadline) {
            mctsPosition.init();
            Node node = selectNode(root);
            if (mctsPosition.getPosition().state() == GameState.WIN) {
                int result = node.win();
                if (result == 1) {
                    while (node.parent.parent != null)
                        node = node.parent.parent;
                    return node.move;
                }
                if (result == -1)
                    return root.getBestMove();
                backPropagate(node, 1);
            }
            else
                backPropagate(node, mctsPosition.simulate());
            count++;
        }
        System.out.println("Simulations: " + count);
        return root.getBestMove();
    }

    private Node selectNode(Node node) {
        while (!node.children.isEmpty()) {
            node = node.selectBestChild();
            mctsPosition.applyMove(node.move);
        }
        if (node.visits == 1) {
            node.expand();
            if (!node.children.isEmpty()) {
                node = node.selectBestChild();
                mctsPosition.applyMove(node.move);
            }
        }
        return node;
    }

    private void backPropagate(Node node, double result) {
        while (node != null) {
            node.visits++;
            node.wins += result;
            node = node.parent;
            result = -result;
        }
    }
    private class Node {
        Node parent;
        Object move;
        double wins = 0;
        int visits = 0;
        List<Node> children = new ArrayList<>();

        Node(Node parent, Object move) {
            this.parent = parent;
            this.move = move;
        }

        void expand() {
            if (mctsPosition.getPosition().state() == GameState.ONGOING)
                for (Object move : mctsPosition.getMoves())
                    children.add(new Node(this, move));
        }

        Node selectBestChild() {
            return children.stream()
                    .max(Comparator.comparingDouble(n -> n.wins / (n.visits + 1) +
                            EXPLORATION_PARAM * Math.sqrt(Math.log(visits + 1) / (n.visits + 1))))
                    .orElse(children.get(rand.nextInt(children.size())));
        }

        Object getBestMove() {
            return children.stream()
                    .max(Comparator.comparingDouble(n -> n.wins / (n.visits + 1)))
                    .map(n -> n.move)
                    .orElse(children.get(rand.nextInt(children.size())).move);
        }
        int win() {
            if (parent == null)
                return -1;
            if (parent.parent == null)
                return 1;
            List<Node> nodes = parent.parent.children;
            int size = nodes.size();
            if (size > 1) {
                int index = nodes.indexOf(parent);
                size--;
                nodes.set(index, nodes.get(size));
                nodes.remove(size);
                return 0;
            }
            return parent.parent.win();
        }
    }
}

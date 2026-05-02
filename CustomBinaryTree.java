public class CustomBinaryTree {
    private Node root;

    private static class Node {
        Witness suspect;
        int matchScore; 
        Node left, right;

        public Node(Witness suspect, int score) {
            this.suspect = suspect;
            this.matchScore = score;
        }
    }

    public void insert(Witness suspect, int score) {
        root = insertRec(root, suspect, score);
    }

    private Node insertRec(Node root, Witness suspect, int score) {
        if (root == null) {
            root = new Node(suspect, score);
            return root;
        }
        if (score < root.matchScore) {
            root.left = insertRec(root.left, suspect, score);
        } else {
            root.right = insertRec(root.right, suspect, score);
        }
        return root;
    }

    // Cari node dengan skor tertinggi (paling kanan)
    public Witness findMostLikelyCulprit() {
        if (root == null) return null;
        Node current = root;
        while (current.right != null) {
            current = current.right;
        }
        return current.suspect;
    }
    
    public int getHighestScore() {
        if (root == null) return 0;
        Node current = root;
        while (current.right != null) {
            current = current.right;
        }
        return current.matchScore;
    }
}

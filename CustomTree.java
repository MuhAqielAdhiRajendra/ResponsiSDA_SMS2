import java.util.ArrayList;
import java.util.List;

public class CustomTree<T> {
    private TreeNode<T> root;

    public CustomTree(T rootData) {
        root = new TreeNode<>(rootData);
    }

    public TreeNode<T> getRoot() {
        return root;
    }

    public static class TreeNode<T> {
        private T data;
        private List<TreeNode<T>> children;

        public TreeNode(T data) {
            this.data = data;
            this.children = new ArrayList<>();
        }

        public void addChild(TreeNode<T> child) {
            this.children.add(child);
        }

        public T getData() { return data; }
        public List<TreeNode<T>> getChildren() { return children; }
    }
}

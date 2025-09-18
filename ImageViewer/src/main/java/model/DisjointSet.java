package model;

public class DisjointSet {
    private int[] parent;  // 父节点
    private int[] size;   //  大小

    public DisjointSet(int n) {
        parent = new int[n];
        this.size = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            this.size[i] = 1;
        }
    }

    // 找到p的老大
    public int find(int p) {
        if (parent[p] != p) {
            parent[p] = find(parent[p]);  // 路径压缩
        }
        return parent[p];
    }

    // union 是让两个集合“相交”，即选出新老大，p、q是原老大索引 PS:union只会让p或q的老大变值，不会改变下面小弟的老大值
    public void union(int p, int q) {
        int rootP = find(p);
        int rootQ = find(q);
        if (rootP != rootQ) { // 在p和q不是同一个老大的情况下
            if (size[rootP] < size[rootQ]) {
                parent[rootP] = rootQ;
                size[rootQ] += size[rootP];
            } else {
                parent[rootQ] = rootP;
                size[rootP] += size[rootQ];
            }
        }
    }
}

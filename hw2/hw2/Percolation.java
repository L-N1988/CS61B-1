package hw2;

import edu.princeton.cs.algs4.WeightedQuickUnionUF;

public class Percolation {
    private class Site {
        boolean open;
        boolean isConnectedBottom;

        Site() {
            this.open = false;
        }
    }

    Site[][] sites;
    private int numberOfOpenSites;
    private WeightedQuickUnionUF uf;
    private int topVirtualSite;
    private boolean percolated;

    public Percolation(int N) {
        if (N <= 0) {
            throw new IllegalArgumentException("N < 0");
        }
        sites = new Site[N][N];
        for (Site[] row : sites) {
            for (int i = 0; i < N; i++) {
                row[i] = new Site();
            }
        }

        numberOfOpenSites = 0;
        uf = new WeightedQuickUnionUF(N * N + 2);
        topVirtualSite = uf.count() - 1;
        percolated = false;
        for (int i = 0; i < sites[0].length; i++) {
            uf.union(i, topVirtualSite);
            sites[sites.length - 1][i].isConnectedBottom = true;
        }
    }

    private int xyTo1D(int r, int c) {
        return r * sites.length + c;
    }

    private Site indexToSite(int index) {
        int r = index / sites.length;
        int c = index - r * sites.length;
        return sites[r][c];
    }

    private boolean notValid(int row, int col) {
        return row < 0 || row >= sites.length || col < 0 || col >= sites.length;
    }

    private void addUnion(int row, int col, int dr, int dc) {
        if (!notValid(row + dr, col + dc) && isOpen(row + dr, col + dc)) {
            uf.union(xyTo1D(row, col), xyTo1D(row + dr, col + dc));
            if (!percolated) {
                int parent = uf.find(xyTo1D(row, col));
                indexToSite(parent).isConnectedBottom = sites[row][col].isConnectedBottom || indexToSite(parent).isConnectedBottom;
            }
        }
    }


    public void open(int row, int col) {
        if (notValid(row, col)) {
            throw new IndexOutOfBoundsException("Open " + row + "," + col);
        }
        if (!sites[row][col].open) {
            sites[row][col].open = true;
            addUnion(row, col, 0, 1);
            addUnion(row, col, 0, -1);
            addUnion(row, col, 1, 0);
            addUnion(row, col, -1, 0);
            if (!percolated) {
                int parent = uf.find(xyTo1D(row, col));
                if (indexToSite(parent).isConnectedBottom && uf.connected(parent, topVirtualSite)) {
                    percolated = true;
                }
            }
            numberOfOpenSites += 1;
        }
    }

    public boolean isOpen(int row, int col) {
        if (notValid(row, col)) {
            throw new IndexOutOfBoundsException("isOpen " + row + "," + col);
        }
        return sites[row][col].open;
    }

    public boolean isFull(int row, int col) {
        if (notValid(row, col)) {
            throw new IndexOutOfBoundsException("isFull " + row + "," + col);
        }
        return isOpen(row, col) && uf.connected(xyTo1D(row, col), topVirtualSite);
    }

    public int numberOfOpenSites() {
        return numberOfOpenSites;
    }

    public boolean percolates() {
        return percolated;
    }

    public static void main(String[] args) {
        Percolation p = new Percolation(5);
    }
}
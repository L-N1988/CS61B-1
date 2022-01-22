package hw2;

import edu.princeton.cs.algs4.WeightedQuickUnionUF;

import javax.print.attribute.standard.Sides;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Percolation {
    private class Site {
        boolean open;

        Site() {
            this.open = false;
        }
    }

    Site[][] sites;
    private int numberOfOpenSites;
    private WeightedQuickUnionUF uf;
    private int topVirtualSite;
    private boolean bottomIsFull;
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
        for (int i = 0; i < sites[0].length; i++) {
            uf.union(i, topVirtualSite);
        }
    }

    private int xyTo1D(int r, int c) {
        return r * sites.length + c;
    }

    private boolean notValid(int row, int col) {
        return row < 0 || row >= sites.length || col < 0 || col >= sites.length;
    }

    private void addUnion(int row, int col, int dr, int dc) {
        if (!notValid(row + dr, col + dc) && isOpen(row + dr, col + dc)) {
            uf.union(xyTo1D(row, col), xyTo1D(row + dr, col + dc));
        }
    }


    public void open(int row, int col) {
        if (notValid(row, col)) {
            throw new IndexOutOfBoundsException("Open " + row + "," + col);
        }
        sites[row][col].open = true;
        addUnion(row, col, 0, 1);
        addUnion(row, col, 0, -1);
        addUnion(row, col, 1, 0);
        addUnion(row, col, -1, 0);
        numberOfOpenSites += 1;
        if (! bottomIsFull) {
            for (int i = 0; i < sites.length; i++) {
                if (uf.connected(xyTo1D(sites.length - 1, i), topVirtualSite)) {
                    bottomIsFull = true;
                    break;
                }
            }

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
//        return uf.connected(bottomVirtualSite, topVirtualSite);
        return bottomIsFull;
    }

    public static void main(String[] args) {
        Percolation p = new Percolation(5);
    }
}
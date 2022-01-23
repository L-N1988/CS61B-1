package hw2;

import edu.princeton.cs.introcs.StdOut;
import edu.princeton.cs.introcs.StdStats;
import edu.princeton.cs.introcs.StdRandom;
import edu.princeton.cs.introcs.Stopwatch;

public class PercolationStats {
    double[] fraction;
    int time;

    public PercolationStats(int N, int T, PercolationFactory pf) {
        validate(N, T);
        this.fraction = new double[T];
        this.time = 0;
        for (int t = 0; t < T; t++) {
            Percolation percolation = pf.make(N);
            while (!percolation.percolates()) {
                int x = StdRandom.uniform(N);
                int y = StdRandom.uniform(N);
                percolation.open(x, y);
            }
            this.time = t;
            this.fraction[t] = (double) percolation.numberOfOpenSites() / N * N;
        }
    }   // perform T independent experiments on an N-by-N grid

    private void validate(int N, int T) {
        if (N <= 0 || T <= 0) {
            throw new IllegalArgumentException("N " + N + "T " + T);
        }
    }

    public double mean() {
        return StdStats.mean(this.fraction, 0, this.time + 1);
    } // sample mean of percolation threshold

    public double stddev() {
        return StdStats.stddev(this.fraction, 0, this.time + 1);
    } // sample standard deviation of percolation threshold

    public double confidenceLow() {
        return mean() - 1.96 * stddev() / Math.sqrt(this.time);
    } // low endpoint of 95% confidence interval

    public double confidenceHigh() {
        return mean() + 1.96 * stddev() / Math.sqrt(this.time);
    } // high endpoint of 95% confidence interval

    public static void main(String[] args) {
        PercolationFactory pf = new PercolationFactory();

        Stopwatch timer1 = new Stopwatch();
        PercolationStats speedTest1 = new PercolationStats(200, 100, pf);
        double time1 = timer1.elapsedTime();
        StdOut.printf("%.2f seconds\n", time1); // WQU 0.44s QF 22.68 seconds

        Stopwatch timer2 = new Stopwatch();
        PercolationStats speedTest2 = new PercolationStats(200, 200, pf);
        double time2 = timer2.elapsedTime();
        StdOut.printf("%.2f seconds\n", time2); // WQU 0.78s QF 43.56 seconds
    }
}

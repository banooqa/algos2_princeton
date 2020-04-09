import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.StdOut;

public class SeamCarver {
    private Picture pic;
    private double[][] energyArr;
    private int width;
    private int height;
    private int[][] source;
    private double[][] cummEnergyArr;

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null)
            throw new IllegalArgumentException();
        pic = new Picture(picture);
        this.width = pic.width();
        this.height = pic.height();
        energyArr = new double[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                energyArr[i][j] = energy(j, i);
            }
        }
    }

    // current picture
    public Picture picture() {
        Picture pic1 = new Picture(pic);
        return pic1;
    }

    // width of current picture
    public int width() {
        return this.width;
    }

    // height of current picture
    public int height() {
        return this.height;
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            throw new IllegalArgumentException();
        }
        double rX, rY, bX, bY, gX, gY;
        double eny;
        if (x == 0 || y == 0 || x == (width() - 1) || y == (height() - 1))
            return 1000;
        int col1, col2;
        col1 = pic.getRGB(x + 1, y);
        col2 = pic.getRGB(x - 1, y);
        int col1R = getRed(col1);
        int col1G = getGreen(col1);
        int col1B = getBlue(col1);
        int col2R = getRed(col2);
        int col2G = getGreen(col2);
        int col2B = getBlue(col2);

        rX = ((col1R - col2R) * (col1R - col2R));
        bX = ((col1B - col2B) * (col1B - col2B));
        gX = ((col1G - col2G) * (col1G - col2G));
        col1 = pic.getRGB(x, y + 1);
        col2 = pic.getRGB(x, y - 1);
        col1R = getRed(col1);
        col1G = getGreen(col1);
        col1B = getBlue(col1);
        col2R = getRed(col2);
        col2G = getGreen(col2);
        col2B = getBlue(col2);
        rY = ((col1R - col2R) * (col1R - col2R));
        bY = ((col1B - col2B) * (col1B - col2B));
        gY = ((col1G - col2G) * (col1G - col2G));
        eny = Math.sqrt((rX + gX + bX + rY + bY + gY));
        return eny;
    }

    private int getRed(int col) {
        return (col / (256 * 256)) % 256;
    }

    private int getGreen(int col) {
        return (col / 256) % 256;
    }

    private int getBlue(int col) {
        return (col % 256);
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        // transposing energy array and exchanging height and width.
        double[][] tEnergyArr = new double[width][height];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                tEnergyArr[j][i] = energyArr[i][j];
            }
        }
        energyArr = tEnergyArr;
        int temp = this.width;
        this.width = this.height;
        this.height = temp;

        // reuse findVerticalSeam function
        int[] horSeam = findVerticalSeam();

        // transposing energy array back and exchanging height and width again.
        temp = this.width;
        this.width = this.height;
        this.height = temp;
        tEnergyArr = new double[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                tEnergyArr[i][j] = energyArr[j][i];
            }
        }
        energyArr = tEnergyArr;
        return horSeam;
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        int minIdx = -1;
        double minCummEnergy = Double.POSITIVE_INFINITY;
        int[] seam = new int[height];
        source = new int[height][width];
        cummEnergyArr = new double[height][width];
        for (int i = 0; i < height; i++) { // initialization
            for (int j = 0; j < width; j++) {
                if (i == 0) // handling top row
                    cummEnergyArr[i][j] = 1000;
                else
                    cummEnergyArr[i][j] = Double.POSITIVE_INFINITY;
                source[i][j] = -1;
            }
        }

        // special case for narrow image
        if (width <= 2 || height <= 2) {
            for (int y = height - 1; y >= 0; y--) {
                seam[y] = 0;
            }
            return seam;
        }


        for (int y = 0; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                processChildren(y, x);
            }
        }
        for (int x = 1; x < width - 1; x++) {
            if (cummEnergyArr[height - 2][x] < minCummEnergy) {
                minIdx = x;
                minCummEnergy = cummEnergyArr[height - 2][x];
            }
        }
        seam[height - 1] = minIdx;
        seam[height - 2] = minIdx;
        for (int y = height - 3; y > 0; y--) {
            seam[y] = source[y + 1][seam[y + 1]];
        }
        seam[0] = seam[1];
        source = null;
        cummEnergyArr = null;
        return seam;
    }

    private void processChildren(int y, int x) {
        if (x - 1 > 0) {
            handleChild(y + 1, x - 1, x);
        }
        handleChild(y + 1, x, x);
        if (x + 1 < width - 1) {
            handleChild(y + 1, x + 1, x);
        }
    }

    private void handleChild(int yC, int xC, int xP) {
        int yP = yC - 1;
        double cummEnergy = cummEnergyArr[yP][xP] + energyArr[yC][xC];
        if ((cummEnergy <= cummEnergyArr[yC][xC])) {
            source[yC][xC] = xP;
            cummEnergyArr[yC][xC] = cummEnergy;
        }
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        if (seam == null || height <= 1)
            throw new IllegalArgumentException();
        if (seam.length != width)
            throw new IllegalArgumentException();
        for (int i = 0; i < seam.length; i++) {
            if ((seam[i] < 0) || (seam[i] > height - 1))
                throw new IllegalArgumentException();
            if (i < seam.length - 1)
                if (Math.abs(seam[i] - seam[i + 1]) > 1)
                    throw new IllegalArgumentException();
        }

        Picture pic1 = new Picture(width, height - 1);
        for (int j = 0; j < width; j++) {
            for (int i = 0; i < height - 1; i++) {
                if (i < seam[j])
                    pic1.set(j, i, pic.get(j, i));
                else if (i >= seam[j])
                    pic1.set(j, i, pic.get(j, i + 1));
            }
        }
        this.pic = pic1;
        this.height--;
        energyArr = new double[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                energyArr[i][j] = energy(j, i);
            }
        }
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        if (seam == null || width <= 1)
            throw new IllegalArgumentException();
        if (seam.length != height)
            throw new IllegalArgumentException();
        for (int i = 0; i < seam.length; i++) {
            if ((seam[i] < 0) || (seam[i] > width - 1))
                throw new IllegalArgumentException();
            if (i < seam.length - 1)
                if (Math.abs(seam[i] - seam[i + 1]) > 1)
                    throw new IllegalArgumentException();
        }
        Picture pic1 = new Picture(width - 1, height);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width - 1; j++) {
                if (j < seam[i])
                    pic1.set(j, i, pic.get(j, i));
                else if (j >= seam[i])
                    pic1.set(j, i, pic.get(j + 1, i));
            }
        }
        this.pic = pic1;
        this.width--;
        energyArr = new double[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                energyArr[i][j] = energy(j, i);
            }
        }
    }

    //  unit testing (optional)
    public static void main(String[] args) {
        Picture pic = new Picture(args[0]);
        StdOut.printf("image is %d pixels wide by %d pixels high.\n", pic.width(),
                      pic.height());

        SeamCarver sc = new SeamCarver(pic);

        StdOut.printf("Printing energy calculated for each pixel.\n");
        for (int i = 0; i < pic.height(); i++) {
            for (int j = 0; j < pic.width(); j++) {
                StdOut.printf("%9.2f ", sc.energyArr[i][j]);
            }
            StdOut.println();
        }
    }

}

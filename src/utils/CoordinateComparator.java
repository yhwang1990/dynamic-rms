package utils;

import java.util.Comparator;

public class CoordinateComparator implements Comparator<Tuple> {

    int coordinate;

    public CoordinateComparator(int coordinate) {
        this.coordinate = coordinate;
    }

    @Override
    public int compare(Tuple t1, Tuple t2) {
        if (t1.value[coordinate] < t2.value[coordinate]) {
            return -1;
        } else if (t1.value[coordinate] > t2.value[coordinate]) {
            return 1;
        } else {
            return (t1.idx - t2.idx);
        }
    }
}


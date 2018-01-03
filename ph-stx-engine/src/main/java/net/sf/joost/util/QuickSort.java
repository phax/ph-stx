package net.sf.joost.util;

public class QuickSort {

    /* A nonstandard unstable in-place quicksort implementation based on Cormen et al.(1990) that does not
    *  require the caller to respect the invariants of compareTo in the Comparable interface.
    *  Solely intended for use by AbstractGroupBase unless similar issues are discovered elsewhere.
    *  Please cf. https://github.com/phax/ph-stx/issues/2 for discussion
    */
    public static void sort(Comparable[] input) {
        _quicksort(input, 0, input.length -1);
    }

    private static void _quicksort(Comparable[] a, int lo, int hi) {
        if (lo < hi) {
            int p = _partition(a, lo, hi);
            _quicksort(a, lo, p - 1);
            _quicksort(a, p + 1, hi);
        }
    }

    private static int _partition(Comparable[] a, int lo, int hi) {
        Comparable temp;
        Comparable pivot = a[hi];
        int i = lo - 1;
        for(int j = lo; j < hi; j++) {
            if(a[j].compareTo(pivot) < 0) {
                i++;
                temp = a[i];
                a[i] = a[j];
                a[j] = temp;
            }
        }

        if(a[hi].compareTo(a[i+1]) < 0) {
            temp = a[i+1];
            a[i+1] = a[hi];
            a[hi] = temp;
        }

        return i+1;
    }

}

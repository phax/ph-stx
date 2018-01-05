package net.sf.joost.util;

import javax.annotation.Nonnull;

public final class QuickSort
{
  private QuickSort ()
  {}

  /*
   * A nonstandard unstable in-place quicksort implementation based on Cormen et
   * al.(1990) that does not require the caller to respect the invariants of
   * compareTo in the Comparable interface. Solely intended for use by
   * AbstractGroupBase unless similar issues are discovered elsewhere. Please
   * cf. https://github.com/phax/ph-stx/issues/2 for discussion
   */
  public static <T extends Comparable <T>> void sort (@Nonnull final T [] input)
  {
    _quicksort (input, 0, input.length - 1);
  }

  private static <T extends Comparable <T>> void _quicksort (final T [] a, final int lo, final int hi)
  {
    if (lo < hi)
    {
      final int p = _partition (a, lo, hi);
      _quicksort (a, lo, p - 1);
      _quicksort (a, p + 1, hi);
    }
  }

  private static <T extends Comparable <T>> int _partition (final T [] a, final int lo, final int hi)
  {
    final T pivot = a[hi];
    int i = lo - 1;
    for (int j = lo; j < hi; j++)
    {
      if (a[j].compareTo (pivot) < 0)
      {
        i++;
        final T temp = a[i];
        a[i] = a[j];
        a[j] = temp;
      }
    }

    if (a[hi].compareTo (a[i + 1]) < 0)
    {
      final T temp = a[i + 1];
      a[i + 1] = a[hi];
      a[hi] = temp;
    }

    return i + 1;
  }
}

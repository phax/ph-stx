package net.sf.joost.util;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(JUnitQuickcheck.class)
public class QuickSortTest {

    @Property
    public void sortsThings(Integer[] myLittleComparable) throws Exception {
        QuickSort.sort(myLittleComparable);
        assertTrue(isSorted(myLittleComparable));
    }

    public static boolean isSorted(Comparable[] data)
    {
        for(int i = 1; i < data.length; i++)
        {
            if(data[i-1].compareTo(data[i]) > 0)
            {
                return false;
            }
        }
        return true;
    }

}

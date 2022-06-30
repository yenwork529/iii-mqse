package org.iii.esd.afc.utils;

import java.math.BigDecimal;

public class AreaUtils {

    public static boolean isInTriangle(Point a, Point b, Point c, Point x) {
        BigDecimal abc = area(a, b, c);

        BigDecimal abx = area(a, b, x);
        BigDecimal acx = area(a, c, x);
        BigDecimal bcx = area(b, c, x);

        if (abc.compareTo(abx.add(acx).add(bcx)) == 0) { return true; } else { return false; }
    }

    // formula area = (Ax(By -Cy) + Bx(Cy -Ay) + Cx(Ay - By))/2
    public static BigDecimal area(Point a, Point b, Point c) {
        return a.x.multiply(b.y.subtract(c.y)).add(b.x.multiply(c.y.subtract(a.y))).add(c.x.multiply(a.y.subtract(b.y)))
                  .divide(new BigDecimal(2)).abs();
    }
}

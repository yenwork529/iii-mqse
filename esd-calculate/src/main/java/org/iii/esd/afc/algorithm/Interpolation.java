package org.iii.esd.afc.algorithm;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

@Log4j2
public class Interpolation {
    public static double calculate(double[] x, double[] y, double interpolationX) {
        UnivariateInterpolator interpolator = new SplineInterpolator();
        UnivariateFunction function = interpolator.interpolate(x, y);
        double interpolatedY = function.value(interpolationX);
        log.debug("f(" + interpolationX + ") = " + interpolatedY);
        return interpolatedY;
    }
}

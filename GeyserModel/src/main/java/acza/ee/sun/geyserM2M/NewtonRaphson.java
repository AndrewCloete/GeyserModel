/* --------------------------------------------------------------------------------------------------------
 * DATE:	15 Jul 2015
 * AUTHOR:	Cloete A.H
 * PROJECT:	M-Eng, Inteligent geyser M2M system.	
 * ---------------------------------------------------------------------------------------------------------
 * DESCRIPTION: Example implementation of Geyser Model.
 * ---------------------------------------------------------------------------------------------------------
 * PURPOSE: 
 * ---------------------------------------------------------------------------------------------------------
 */

package acza.ee.sun.geyserM2M;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NewtonRaphson {
	
	private static final Logger logger = LogManager.getLogger(NewtonRaphson.class);
	
	public static final int MAX_ITERATIONS = 45;
	public double cylinder_radius; // Radius of cylinder.
	public double cylinder_length; 		// Length of cylinder.
	
	/**
	 * Constructor
	 * @param cylinder_radius in meters
	 * @param cylinder_length in meters
	 */
	public NewtonRaphson(double cylinder_radius, double cylinder_length){
		this.cylinder_radius = cylinder_radius;
		this.cylinder_length = cylinder_length;
	}

	/**
	 *  Returns arc length of circular segment for given volume.
	 * @param volume in liters
	 * @return
	 */
	public double calculateArcLength(double volume) {
		boolean accurate = false;
		int iterations = 0;
		double x1;
		double C = (2 * volume * 0.001) / (cylinder_length * (Math.pow(cylinder_radius, 2)));
		x1 = Math.PI; // Initial estimate.
		while (!accurate && iterations < MAX_ITERATIONS) {
			// Perform iteration until desired result is reached.
			x1 = NRresult(x1, C);
			if (Math.abs(f(x1, C)) < 0.000001) {
				accurate = true;
			}
			iterations++;
		}
		double s = x1 * cylinder_radius;
		if (!accurate) {
			logger.error("NewtonRapson did not converge.");
		}
		return s;
	}
	
	/**
	 *  Returns surface area of node with specified arcLength.
	 * @param arcLength
	 * @return
	 */
	public double surfaceArea(double arcLength) {
		return 2 * segmentArea(arcLength) + (arcLength * cylinder_length); // 2*sides + belly
	}
	
	/**
	 *  Returns chord length of circular segment for given arc length.
	 * @param arcLength
	 * @return
	 */
	public double chordLength(double arcLength) {
		double x = arcLength / cylinder_radius;
		return 2 * cylinder_radius * Math.sin(x / 2);
	}
	
	/**
	 *  Returns the surface area of the circular segment with given arc legnth.
	 * @param arcLength
	 * @return
	 */
	public double segmentArea(double arcLength) {
		double x = arcLength / cylinder_radius;
		return (Math.pow(cylinder_radius, 2) * (x - Math.sin(x))) / 2;
	}

	// Definition of function f.
	public static double f(double x, double C) {
		return C - x + Math.sin(x);
	}

	// Returns derivative of function f.
	public static double df(double x) {
		return -1 + Math.cos(x);
	}

	// Returns result of one iteration of Newton-Raphson method.
	public static double NRresult(double x0, double C) {
		double x1 = 0;
		x1 = x0 - (f(x0, C) / df(x0));
		return x1;
	}


}


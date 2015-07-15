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

public class UsagePoint {
	
	public final long timestamp;
	public final double usage_litres;
	
	public UsagePoint(long timestamp, double usage_litres){
		this.timestamp = timestamp;
		this.usage_litres = usage_litres;
	}

}

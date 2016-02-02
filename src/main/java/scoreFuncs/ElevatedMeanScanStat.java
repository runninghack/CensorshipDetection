package scoreFuncs;

import java.util.ArrayList;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.stat.StatUtils;

/**
 * This is an example of statistic function.
 * @author baojian
 *
 */
public class ElevatedMeanScanStat implements Function {
	
	private final double[] c ; // attribute 1 : each node i has a feature c_i
	
	public ElevatedMeanScanStat(double[] c){
		this.c = c ;
	}

	@Override
	public double[] getGradient(double[] x) {
		if(x == null || c == null || x.length != c.length){
			new IllegalArgumentException("Error : Invalid parameters ...") ;
			System.exit(0) ;
		}
		double[] gradient = new double[this.c.length] ;
		double sigmaX = StatUtils.sum(x) ;
		if(sigmaX == 0.0D){
			System.out.println("Error : the denominator should not be negative ...");
			System.exit(0) ;
		}
		double sigmaCX = new ArrayRealVector(x).dotProduct(new ArrayRealVector(c)) ;
		for(int i = 0 ; i < gradient.length ; i++){
			gradient[i] = (2.0D*c[i]*sigmaCX / sigmaX ) - Math.pow(sigmaCX / sigmaX, 2) ;
		}
		return gradient ;
	}

	@Override
	public double getFuncValue(double[] x) {
		
		double funcValue = 0.0D ;
		
		if(x == null || c == null || x.length != c.length){
			new IllegalArgumentException("Error : Invalid parameters ...") ;
			System.exit(0) ;
		}
		double sigmaX = StatUtils.sum(x) ;
		double sigmaCX = new ArrayRealVector(x).dotProduct(new ArrayRealVector(c)) ;
		if(sigmaX <= 0.0D){
			System.out.println("Error : the denominator should be positive ...");
			System.exit(0) ;
		}
		funcValue = Math.pow(sigmaCX, 2) / sigmaX ;
		
		return funcValue ;
	}

	/**
	 * We don't need to implement this method
	 */
	@Override
	public double[] getArgMinFx(ArrayList<Integer> S) {
		//TODO try to find a way to find a minimum value
		return null;
	}
	
}

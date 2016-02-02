package scoreFuncs;

import java.util.ArrayList;

/**
 * @author Baojian
 *
 */
public interface Function {
	
	public double[] getGradient(double[] x) ;
	public double getFuncValue(double[] x) ;
	public double[] getArgMinFx(ArrayList<Integer> S) ;

}

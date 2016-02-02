package scoreFuncs;

import java.util.ArrayList;
import java.lang.Math;

public class ProtestScanStat implements Function {
	private double[] Z;
	private int[] R ;
	private double[] lambdas ;
	
	public ProtestScanStat(double[] c, int[] R, double[] lambdas){
		this.Z = c ;
		this.R = R ;
		this.lambdas = lambdas;
	}

	@Override
	public double[] getGradient(double[] Y) {
		double[] gradient = new double[this.Z.length];
		double ZtY = 0.0D;
		double lambdastY = 0.0D;
		for(int i=0;i<Y.length;i++){
			if(Y[i] == 1.0D){
				ZtY += Z[i];
				lambdastY += lambdas[i];
			}
		}
		for(int i = 0 ; i < gradient.length ; i++){
			gradient[i] = R.length + (Math.log(ZtY) - Math.log(lambdastY) - Math.log(R.length)) * Z[i] - (ZtY/lambdastY) * lambdas[i];
		} 
		//System.out.println("gradients value : "+Arrays.toString(gradient)) ;
		return gradient;
	}

	@Override
	public double getFuncValue(double[] Y) {
		double count_Y = 0;
		double ZtY = 0.0D;
		double lambdastY = 0.0D;
		for(int i=0;i<Y.length;i++){
			if(Y[i] == 1.0D){
				count_Y += 1;
				ZtY += Z[i];
				lambdastY += lambdas[i];
			}
		}
		//System.out.println("count of Y : "+ String.valueOf(count_Y));
		//System.out.println("fun value : "+Arrays.toString(graphModelIHT.resultNodes_Tail)) ;
		double score = R.length * count_Y + ZtY * (Math.log(ZtY) - Math.log(lambdastY) - Math.log(R.length) -1);
		return score;
	}

	/**
	 * We don't need to care about this method in Graph Model IHT algorithm
	 *  (non-Javadoc)
	 * @see scoreFuncs.Function#getArgMinFx(java.util.ArrayList)
	 */
	@Override
	public double[] getArgMinFx(ArrayList<Integer> S) {
		return null;
	}
}

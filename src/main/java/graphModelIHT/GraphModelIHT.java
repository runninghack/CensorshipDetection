package graphModelIHT;

import headApprox.PCSFHead;
import scoreFuncs.ElevatedMeanScanStat;
import scoreFuncs.ProtestScanStat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.StatUtils;

import apdmIO.ConnectedComponents;
import base.Constants;
import base.Utils;
import tailApprox.PCSFTail;

/**
 * Algorithm : graph Model IHT (Iterative Hard thresholding) algorithm
 * We make the following assumption in the analysis of this algorithm : 
 * 1) f(x) satisfies Model GRSC
 * 2) it calls tail approximation algorithm
 * 3) it also calls head approximation algorithm
 * @author Baojian (bzhou6@albany.edu)
 */
public class GraphModelIHT {

	private final double[] c ;	// 1Xn dimension input parameter for func
	private final ArrayList<Integer[]> edges ;
	private final ArrayList<Double> edgeCosts ;
	private final int s ;		// total sparsity of S
	private final int g ;		// # of connected components in forest F
	private final double B ;	// bound on the total weight w(F) of edges in the forest F.
	private final int t ;		// number of iterations
	private final int[] trueSubGraph ;
	private final boolean singleNodeInitial ;
	private final Constants.StatisticFunc scoreFunc ;
	private final String outputFileName ;
	private int verboseLevel = 0 ;
	private int[] R;
	private double[] lambdas;


	/**
	 * These are results that algorithm returned.
	 * 
	 * */
	public double[] x ;	//this is the final vector that we get from algorithm
	public int[] resultNodes_supportX ;	//the subset of support(X), this subset may not be connected.
	public int[] resultNodes_Tail ;	//return the connected subset.
	public double funcValue ;	//return the final function value

	
	public GraphModelIHT(
			ArrayList<Integer[]> edges, ArrayList<Double> edgeCosts, 
			double[] c, double [] b, int s, int g, double B, 
			int t,boolean singleNodeInitial, int[] trueSubGraph
			,Constants.StatisticFunc scoreFunction,String outputFileName,
			int [] R,double[] lambdas){

		this.edges = edges ;

		if(edgeCosts == null){
			edgeCosts = new ArrayList<Double>() ;
			for(int i = 0 ; i < this.edges.size() ; i++){
				edgeCosts.add(1.0D) ; 	//The default of each edge cost is 1.0D
			}
		}
		this.edgeCosts = edgeCosts ;
		this.c = c ;
		this.s = s ;
		this.g = g ;
		this.B = B ;
		this.t = t ;
		this.singleNodeInitial = singleNodeInitial ;
		this.trueSubGraph = trueSubGraph ;
		this.scoreFunc = scoreFunction ;
		this.outputFileName = outputFileName ;
		this.R = R;
		this.lambdas = lambdas;
		x = run() ; // run the algorithm
	}

	private double[] run(){

		double[] x ;
		if(this.singleNodeInitial == true){
			x = this.initializeX_RandomSingleNode() ; //return X0 with only one entry has 1.0D value
		}else{
			x = this.initializeX_MaximumCC() ; //return X0 with maximum connected component nodes have 1.0D values
		}

		ArrayList<Double> fValues = new ArrayList<Double>() ;
		for(int i = 0 ; i < this.t ; i ++){ //	t iterations

			//System.out.println("============================iteration: "+ i+"============================")	;
			fValues.add(func(x)) ;
			//head approximation
			double[] gradientF = gradientFunc(x) ; 	//Gradient for the function Elevated mean Scan Statistic
			//System.out.println(Arrays.toString(gradientF)) ;
			PCSFHead pcsfHead = new PCSFHead(edges, edgeCosts, gradientF, s, g, B, trueSubGraph) ;
			//System.out.println("head : "+pcsfHead.bestForest.nodesInF.size()+ " "+pcsfHead.bestForest.nodesInF.toString()) ;
			double letterXi = 1.0D ;
			double[] y = new double[gradientF.length] ;
			for(int jj = 0 ; jj < y.length ; jj++){
				if(pcsfHead.bestForest.nodesInF.contains(jj)){
					y[jj] = x[jj] + letterXi * gradientF[jj] ;	
				}else{
					y[jj] = x[jj] ;
				}
			}

			//tail approximation
			PCSFTail pcsfTail = new PCSFTail(edges,edgeCosts,y,s,g,B,trueSubGraph) ;
			//calculate x^{i+1}
			//System.out.println("len is : "+ pcsfTail.bestForest.nodesInF.size()+" " +pcsfTail.bestForest.nodesInF.toString() ) ;
			for(int j = 0 ; j < x.length ; j++){ x[j] = 0.0D ; }
			for(int j: pcsfTail.bestForest.nodesInF){ x[j] = y[j] ; }

			for(int j = 0 ; j < x.length ; j++){
				if(x[j] < 0.0D){
					x[j] = 0.0D ;
				}
				if(x[j] > 1.0D){
					x[j] = 1.0D ;
				}
			}

			if(verboseLevel >= 0){
				//System.out.println("number of head nodes : "+pcsfHead.bestForest.nodesInF.size()) ;
				//System.out.println("number of tail nodes : "+pcsfTail.bestForest.nodesInF.size()) ;
			}
			resultNodes_Tail = Utils.getIntArrayFromIntegerList(pcsfTail.bestForest.nodesInF) ; // they are not equal
		}

		int[] nodes = null ;
		for(int i = 0 ; i < x.length ; i++){
			if(x[i]==0.0D){
				// do nothing
			}else{
				// get nonzero nodes
				nodes = ArrayUtils.add(nodes, i) ; 	
			}
		}
		resultNodes_supportX = nodes ;
		funcValue = func(x) ;
		writeDouble2File(fValues) ;
		return x ;
	}

	private void writeDouble2File(ArrayList<Double> fValues){

		if(outputFileName == null || outputFileName.isEmpty()){
			return ;
		}

		try {
			FileWriter fileWriter = new FileWriter(new File(outputFileName),true) ;
			fileWriter.write(scoreFunc+" : ");
			for(double v:fValues){
				fileWriter.write(v+" ");	
			}
			fileWriter.write("\n");
			fileWriter.close() ;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private double func(double[] x){
		double result = 0.0D ;
		if(scoreFunc == Constants.StatisticFunc.ElevatedMeanScanStat){
			ElevatedMeanScanStat el = new ElevatedMeanScanStat(c) ;
			result = el.getFuncValue(x);
		}else{
			ProtestScanStat pss = new ProtestScanStat(c, R, lambdas) ;
			result = pss.getFuncValue(x);
		}
		return result ;
	}

	private double[] gradientFunc(double[] x){
		double[] result = null ;
		if(scoreFunc == Constants.StatisticFunc.ElevatedMeanScanStat){
			ElevatedMeanScanStat el = new ElevatedMeanScanStat(c) ;
			result  = el.getGradient(x) ;
		}else{
			ProtestScanStat pss = new ProtestScanStat(c, R, lambdas) ;
			result = pss.getGradient(x) ;
		}
		return result;
	}

	private double[] initializeX_MaximumCC(){

		ArrayList<ArrayList<Integer>> adj = new ArrayList<ArrayList<Integer>>() ;
		for(int i = 0 ; i < this.c.length ; i++){
			adj.add(new ArrayList<Integer>()) ;
		}
		for(Integer[] edge:this.edges){
			adj.get(edge[0]).add(edge[1]) ;
			adj.get(edge[1]).add(edge[0]) ;
		}

		ConnectedComponents cc = new  ConnectedComponents(adj) ;
		int[] abnormalNodes = null ;
		
		double mean = StatUtils.mean(c) ;
		double std = Math.sqrt(StatUtils.variance(c)) ;
		//System.out.println(" "+ (mean - 1*std));
		for(int i = 0 ; i < c.length ; i++){
			if(c[i] <= mean - std){
				abnormalNodes = ArrayUtils.add(abnormalNodes, i) ;
			}
		}
		cc.computeCCSubGraph(abnormalNodes) ;
		int[] largestCC = cc.findLargestConnectedComponet(abnormalNodes) ;
		double[] x0 = new double[this.c.length] ;
		for(int i = 0 ; i < x0.length ; i++){
			x0[i] = 0.0D ;
		}
		for(int i = 0 ; i < largestCC.length ; i++){
			x0[largestCC[i]] = 1.0D ;
		}

		return x0 ;
	}
	/*
	private double[] initializeX_MaximumCC(){

		ArrayList<ArrayList<Integer>> adj = new ArrayList<ArrayList<Integer>>() ;
		for(int i = 0 ; i < this.c.length ; i++){
			adj.add(new ArrayList<Integer>()) ;
		}
		for(Integer[] edge:this.edges){
			adj.get(edge[0]).add(edge[1]) ;
			adj.get(edge[1]).add(edge[0]) ;
		}

		ConnectedComponents cc = new  ConnectedComponents(adj) ;
		int[] abnormalNodes = null ;
		for(int i = 0 ; i < this.c.length ; i++){
			if(c[i] >= 0.6D){
				abnormalNodes = ArrayUtils.add(abnormalNodes, i) ;
			}
		}
		cc.computeCCSubGraph(abnormalNodes) ;
		int[] largestCC = cc.findLargestConnectedComponet(abnormalNodes) ;
		double[] x0 = new double[this.c.length] ;
		for(int i = 0 ; i < x0.length ; i++){
			x0[i] = 0.0D ;
		}
		for(int i = 0 ; i < largestCC.length ; i++){
			x0[largestCC[i]] = 1.0D ;
		}

		return x0 ;
	}*/

	private double[] initializeX_RandomSingleNode(){
		int[] abnormalNodes = null ;
		for(int i = 0 ; i < this.c.length ; i++){
			if(this.c[i] == 1.0D){
				abnormalNodes = ArrayUtils.add(abnormalNodes, i) ;
			}
		}
		int index = new Random().nextInt(abnormalNodes.length) ;
		double[] x0 = new double[this.c.length] ;
		for(int i = 0 ; i < x0.length ; i++){
			x0[i] = 0.0D ;
		}
		x0[abnormalNodes[index]] = 1.0D ;
		return x0 ;
	}

	/**
	 * get a support of a vector
	 * @param x
	 * @return a subset of nodes corresponding the index of vector x with entries not equal to zero
	 */
	public ArrayList<Integer> support(double[] x){
		if(x == null){
			return null ;
		}
		ArrayList<Integer> nodes = new ArrayList<Integer>() ;
		for(int i = 0 ; i < x.length ; i++){
			if( x[i] == 0.0D ){
				// do nothing
			}else{
				nodes.add(i) ;
			}
		}
		return nodes ;
	}

	/**
	 * @return the conncetd subgraph
	 */
	public int[] getTailNodes(){
		int[] nodes = null ;
		for(int k : this.resultNodes_Tail){
			nodes = ArrayUtils.add(nodes, k) ;
		}
		return nodes ;
	}

}

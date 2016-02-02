package TestGraphModelIHT;

import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import apdmIO.APDMInputFormat;
import base.Constants;
import base.Utils;
import graphModelIHT.GraphModelIHT;

public class TestGraphModelIHT {

	/**
	 * You can add your own test here
	 */
	@Test
	public void testGraphIHT_ProtestStat(){
		//String rootFolder = "data/SimulationData/GridData/SingleCom_100/" ;
		//APDMInputFormat apdm = new APDMInputFormat(new File(rootFolder+"simulation.txt")) ;
		String rootFolder = "data/SimulationData/GridData/" ;
		APDMInputFormat apdm = new APDMInputFormat(new File(rootFolder+"2015_05_01_30_graph.txt")) ;
		
		
		int[] R = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30};
		double[] lambdas = new double[100];
		for(int i=0;i<100;i++){
			lambdas[i] = Math.random();
		}
		JSONParser parser = new JSONParser();
		try{
			Object obj = parser.parse(new FileReader(rootFolder+"2015_05_01_30_lambda.txt"));
			JSONObject jsonObject = (JSONObject) obj;
			 int name = (int) jsonObject.get("0");
			 System.out.println(name);
		}catch(Exception e){
			System.out.println("==============Error=======================") ;		
		}


		//edges and edgeCosts
		ArrayList<Integer[]> edges = new ArrayList<Integer[]>() ;	//1. graph G, input parameter G
		ArrayList<Double> edgeCosts = new ArrayList<Double>() ;		//2. edge costs c
		for(int[] edge:apdm.inputData.edges.keySet()){
			edges.add(new Integer[]{edge[0],edge[1]}) ;
			edgeCosts.add(apdm.inputData.edges.get(edge)) ;
		}
		
		//vector c
		double[] c = new double[apdm.numNodes] ;
		for(int i = 0 ; i < apdm.getPValue().length ; i++){
			if( apdm.getPValue()[i] <= Constants.AlphaMax ){
				c[i] = 1.0D ;
			}else{
				c[i] = 0.0D ;
			}
		}

		//sparsity s
		int s = apdm.trueSubGraphNodes.length ;
		
		System.out.println("=====================================") ;
		System.out.println("number of nodes: "+apdm.numNodes) ;
		Arrays.sort(apdm.trueSubGraphNodes) ;
		System.out.println("true subgraph : "+ apdm.trueSubGraphNodes.length+" "+Arrays.toString(apdm.trueSubGraphNodes)) ;
		System.out.println("s : " + s) ;
		System.out.println("=====================================") ;
		
		//call graph model iht algorithm
		//GraphModelIHT graphModelIHT = new GraphModelIHT(edges, s, c,Constants.StatisticFunc.ElevatedMeanScanStat) ;
		GraphModelIHT graphModelIHT = new GraphModelIHT(edges, null, c, null, s, 1, s-1+0.0D, 10, false, null, Constants.StatisticFunc.ElevatedMeanScanStat, null, R, lambdas) ;
		System.out.println("=====================================") ;
		System.out.println("supportX : "+Arrays.toString(graphModelIHT.resultNodes_supportX)) ;
		System.out.println("connected subgraph : "+Arrays.toString(graphModelIHT.resultNodes_Tail)) ;
		System.out.println("function value : "+graphModelIHT.funcValue) ;
		int len = Utils.intersect(apdm.trueSubGraphNodes, graphModelIHT.resultNodes_supportX).length ;
		double rec = len*1.0D / apdm.trueSubGraphNodes.length*1.0D ;
		double pre = len*1.0D / graphModelIHT.resultNodes_supportX.length*1.0D ;
		DecimalFormat df = new DecimalFormat("####0.00000") ;
		System.out.println("precision : "+ df.format(pre) +" ; recall : "+ df.format(rec) ) ;
		System.out.println("=====================================") ;
	}

}

package TestGraphModelIHT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import apdmIO.APDMInputFormat;
import base.Utils;
import graphModelIHT.GraphModelIHT;

public class Run_grid {

	public static void main(String[] args) throws IOException {
		int[] sec_node_num = {100,400};
		double[] sec_sparsity = {0.05, 0.1, 0.2};
		double [] q_value = {0.01,0.1,0.5,0.7};
		boolean[] sec_noise = {true,false};
		
		FileWriter fileWriter = new FileWriter(new File("output/grid_results.csv"),true) ;
		
		for(int i:sec_node_num){
			for(double j:sec_sparsity){
				for(boolean k:sec_noise){
					for(double q:q_value){
						String noise = k?"noise":"clean";
						String fname = "grid_" + String.valueOf(i) + "_"+ String.valueOf(q)+ "_"+ String.valueOf(j) + "_" + String.valueOf(noise);
						for(int f=0;f<10;f++){
							//System.out.println(fname + "_" + String.valueOf(f));
							String result = run(fname + "_" + String.valueOf(f));
							String final_line = String.valueOf(i) + ","+ String.valueOf(q)+ ","+ String.valueOf(j) + "," + String.valueOf(k) + "," + String.valueOf(f) + "," + result;
							fileWriter.write(final_line);
							fileWriter.write("\n");
						}
					}
				}
			}
		}
		fileWriter.close() ;
	}
	public static String run(String filename) throws IOException{
		String rootFolder = "data/SimulationData/GridData/grid_data/" ;
		APDMInputFormat apdm = new APDMInputFormat(new File(rootFolder+filename+".txt")) ;
		
		// lambdas and R
		int[] R = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30 };
		ArrayList<String> List = new ArrayList<String>(); 
		BufferedReader br = new BufferedReader(new FileReader(rootFolder+ filename + "_lambdas.txt"));
		String line = null;
		while ((line = br.readLine()) != null) {
			List.add(line);
		}
		br.close();
		double[] lambdas = new double[List.size()];
		for(int i=0;i<List.size();i++){
			String s = (String) List.get(i);
			lambdas[i] = Double.parseDouble(s);
			}

		//edges and edgeCosts
		ArrayList<Integer[]> edges = new ArrayList<Integer[]>() ;	//1. graph G, input parameter G
		for(int[] edge:apdm.inputData.edges.keySet()){
			edges.add(new Integer[]{edge[0],edge[1]}) ;
		}
		
		//node weights
		double[] c = apdm.getPValue();
	
		//sparsity s
		int s = apdm.trueSubGraphNodes.length ;
		Arrays.sort(apdm.trueSubGraphNodes) ;

		
		GraphModelIHT graphModelIHT = new GraphModelIHT(edges, null, c, null, s/2, 1, s-1+0.0D, 10, false, null, null, null, R, lambdas) ;
		int len = Utils.intersect(apdm.trueSubGraphNodes, graphModelIHT.resultNodes_supportX).length ;
		double rec = len*1.0D / apdm.trueSubGraphNodes.length*1.0D ;
		double pre = len*1.0D / graphModelIHT.resultNodes_supportX.length*1.0D ;
		DecimalFormat df = new DecimalFormat("####0.00000");
		System.out.println("precision : "+ df.format(pre) +" ; recall : "+ df.format(rec) ) ;
		String results = df.format(pre) + "," + df.format(rec);
		return results;
	}
}

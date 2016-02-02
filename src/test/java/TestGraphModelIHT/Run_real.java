package TestGraphModelIHT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.json.*;
import org.apache.commons.lang3.ArrayUtils;

import apdmIO.APDMInputFormat;
import graphModelIHT.GraphModelIHT;

public class Run_real {

	public static void main(String[] args) throws IOException {
		
		File folder = new File("data/RealData_China/2015_05/");
		File[] files = folder.listFiles();

		List<File> listOfFiles=Arrays.asList(files);
		List<File> graphFiles = listOfFiles.stream()
			    .filter(f -> f.getName().contains("graph")).collect(Collectors.toList());
		for(int i=0;i<graphFiles.size();i++){
			
			String fname = graphFiles.get(i).getName();
			System.out.println(fname);
			run(fname);
		}
	}
	public static double run(String filename) throws IOException{
		System.out.println("running");
		System.out.println(filename);
		long startTime=System.currentTimeMillis();
		String[] elements = filename.split("_", -1);
		ArrayList<Integer> R_list = new ArrayList<Integer>();
		int day = Integer.parseInt(elements[2]);
		int length = Integer.parseInt(elements[3]);
		IntStream.range(day,day + length)
				.forEach(R_list::add);
		int[] R = ArrayUtils.toPrimitive(R_list.toArray(new Integer[0]));
		
		String rootFolder = "data/RealData_China/2015_05/" ;
		APDMInputFormat apdm = new APDMInputFormat(new File(rootFolder+filename)) ;
		
		BufferedReader br = new BufferedReader(new FileReader(rootFolder+ filename.replace("graph","lambda")));
		String line = br.readLine();
		
		JSONObject obj = new JSONObject(line.trim());
		ArrayList<Double> lambdas_list = new ArrayList<Double>();
		Iterator<?> keys = obj.keys();
		while(keys.hasNext()) {
		    String key = (String)keys.next();
		    lambdas_list.add(obj.getDouble(key));
		}
		double[] lambdas = lambdas_list.stream().mapToDouble(d -> d).toArray();
		br.close();

		//edges and edgeCosts
		ArrayList<Integer[]> edges = new ArrayList<Integer[]>() ;	//1. graph G, input parameter G
		ArrayList<Double> edgeCosts = new ArrayList<Double>() ;		//2. edge costs c
		for(int[] edge:apdm.inputData.edges.keySet()){
			edges.add(new Integer[]{edge[0],edge[1]}) ;
			edgeCosts.add(apdm.inputData.edges.get(edge)) ;
		}
		
		double[] c = apdm.getPValue();
	
		//sparsity s
		int s = apdm.trueSubGraphNodes.length ;
		System.out.println(String.valueOf(s));
		
		Arrays.sort(apdm.trueSubGraphNodes) ;
		
		GraphModelIHT graphModelIHT = new GraphModelIHT(edges, null, c, null, s/2, 1, s-1+0.0D, 10, false, null, null, null, R, lambdas) ;
		//System.out.println("connected subgraph : "+Arrays.toString(graphModelIHT.resultNodes_Tail)) ;
		FileWriter fileWriter = new FileWriter(new File("real_data_results.csv"),true);
		fileWriter.write(filename + ",");
		fileWriter.write(String.valueOf(graphModelIHT.funcValue) + ",");
		List<Integer> index_list= IntStream.of(graphModelIHT.resultNodes_Tail).boxed().collect(Collectors.toList());
		
		List<String> words = new ArrayList<String>();
		BufferedReader br2 = new BufferedReader(new FileReader(rootFolder+ filename.replace("graph","index")));
		String line2 = br2.readLine();
		JSONObject obj2 = new JSONObject(line2.trim());
		for(Integer a:index_list){
			words.add(obj2.getString(String.valueOf(a)));
		}
		String words_String= words.stream().collect(Collectors.joining("\t"));
		br2.close();

		long endTime=System.currentTimeMillis();
		fileWriter.write(String.valueOf(endTime-startTime) + ",");
		fileWriter.write(words_String + "\n");
		fileWriter.close();
		System.out.println(String.valueOf(endTime-startTime));
		return graphModelIHT.funcValue;
	}
}

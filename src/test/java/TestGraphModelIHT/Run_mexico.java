package TestGraphModelIHT;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
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

public class Run_mexico {
	
	private double max_score = 0;
	public double getMax_score() {
		return max_score;
	}
	public void setMax_score(double max_score) {
		this.max_score = max_score;
	}

	private int[] best_subgraph;
	public int[] getBest_subgraph() {
		return best_subgraph;
	}
	public void setBest_subgraph(int[] best_subgraph) {
		this.best_subgraph = best_subgraph;
	}
	
	private List<String> ignore_words;
	public List<String> getIgnore_words() {
		return ignore_words;
	}
	public void setIgnore_words(List<String> ignore_words) {
		this.ignore_words = ignore_words;
	}
	
	private boolean is_first_time;
	public void setIs_first_time(boolean is_first_time) {
		this.is_first_time = is_first_time;
	}
	public boolean getIs_first_time() {
		return this.is_first_time;
	}
	
	private String bestWindow;
	public String getBestWindow() {
		return bestWindow;
	}
	public void setBestWindow(String bestWindow) {
		this.bestWindow = bestWindow;
	}
	
	private List<String[]> subgraph;
	public List<String[]> getSubgraph() {
		return subgraph;
	}
	public void setSubgraph(List<String[]> subgraph) {
		this.subgraph = subgraph;
	}
	
	private List<Double> weights;
	public List<Double> getWeights() {
		return weights;
	}
	public void setWeights(List<Double> weights) {
		this.weights = weights;
	}
	
	public static void main(String[] args) throws IOException {
		String month_path = "data/Mexico/2014_09/";
		String date = month_path.substring(month_path.lastIndexOf("/") - 8,month_path.lastIndexOf("/") );
		Run_mexico runner = new Run_mexico();
		runner.setIs_first_time(true);

		Writer fileWriter = new BufferedWriter(new OutputStreamWriter(
			    new FileOutputStream("output/mexico/" + date + "_results.txt"), "UTF-8"));
		
		for(int i=0;i<5;i++){
			System.out.println(i);
			runner.run_month(month_path);
			String words_String= runner.getIgnore_words().stream().collect(Collectors.joining("\t"));
			fileWriter.write(String.valueOf(i) + "," + runner.getBestWindow() + "," + words_String + "\n");
			
			writeGraph("output/mexico/" + date + "_" + String.valueOf(i) + "_graph.txt", runner.getIgnore_words(),runner.getWeights(),runner.getSubgraph());
			
			runner.setIs_first_time(false);
			
		}
		fileWriter.close();
	}
	
	public static void writeGraph(String filePath, List<String> nodes, List<Double> weights, List<String[]> edges)throws IOException{
		Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"));
		for(String n:nodes){
			fw.write(n + "\t");
		}
		fw.write("\n");
		for(double w:weights){
			fw.write(String.valueOf(w) + "\t");
		}
		fw.write("\n");
		for(String[] e:edges){
			fw.write(e[0] +"\t" + e[1] + "\n");
		}
		fw.close();
	}
	
	
	public void run_month(String folder_path) throws IOException {
		long startTime=System.currentTimeMillis();
		File folder = new File(folder_path);
		File[] files = folder.listFiles();

		List<File> listOfFiles=Arrays.asList(files);
		List<File> graphFiles = listOfFiles.stream()
			    .filter(f -> f.getName().contains("graph")).collect(Collectors.toList());
		this.setMax_score(0);
		for(int i=0;i<graphFiles.size();i++){
			String fname = graphFiles.get(i).getName();
			this.run_timewindow(folder_path,fname);
		}
		long endTime=System.currentTimeMillis();
		System.out.println("***" + String.valueOf(endTime-startTime) + "***");
	}
	
	public void run_timewindow(String rootFolder, String filename) throws IOException{
		System.out.println(filename);
		long startTime=System.currentTimeMillis();
		//R
		//String[] elements = filename.split("_", -1);
		ArrayList<Integer> R_list = new ArrayList<Integer>();
		int day = Integer.parseInt(filename.substring(8,10));
		int length = Integer.parseInt(filename.substring(11,12));
		IntStream.range(day,day + length)
				.forEach(R_list::add);
		int[] R = ArrayUtils.toPrimitive(R_list.toArray(new Integer[0]));
		
		//lambdas
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
		
		//word dictionary
		BufferedReader br2 = new BufferedReader(new FileReader(rootFolder+ filename.replace("graph","index")));
		String line2 = br2.readLine();
		JSONObject word_dict = new JSONObject(line2.trim());
		br2.close();
		
		//edges and c
		APDMInputFormat apdm = new APDMInputFormat(new File(rootFolder+filename)) ;
		ArrayList<Integer[]> edges = new ArrayList<Integer[]>() ;	//1. graph G, input parameter G
		for(int[] edge:apdm.inputData.edges.keySet()){
			edges.add(new Integer[]{edge[0],edge[1]}) ;
		}
		double[] c = apdm.getPValue();
		
		//detect only up
		for(int i=0;i<c.length;i++){
			if(c[i] < lambdas[i]){
				c[i] = lambdas[i];
			}
		}
		
		//adjust most anomalous nodes in order to detect other anomalous nodes
		if(!this.getIs_first_time()){
			for(String iw: this.getIgnore_words()){
				for(int i=0;i<apdm.numNodes;i++){
					if(word_dict.getString(String.valueOf(i)).equals(iw)){
						c[i] = lambdas[i];
					}
				}
			}
		}
		
		//sparsity s
		//int s = apdm.trueSubGraphNodes.length ;
		int s = 7;
		
		//Arrays.sort(apdm.trueSubGraphNodes);
		
		GraphModelIHT graphModelIHT = new GraphModelIHT(edges, null, c, null, s/2, 1, s-1+0.0D, 10, false, null, null, null, R, lambdas) ;
		long endTime=System.currentTimeMillis();
		System.out.println(String.valueOf(endTime-startTime));
		//post-process
		List<Integer> index_list= IntStream.of(graphModelIHT.resultNodes_Tail).boxed().collect(Collectors.toList());
		List<String> words = new ArrayList<String>();
		List<Double> weights = new ArrayList<Double>();
		for(Integer a:index_list){
			words.add(word_dict.getString(String.valueOf(a)));
			weights.add(c[a]);
		}
		ArrayList<String[]> sub_edges = new ArrayList<String[]>() ;
		for(Integer[] e:edges){
			if(index_list.contains(e[0]) && index_list.contains(e[1]) && !word_dict.getString(String.valueOf(e[0])).equals(word_dict.getString(String.valueOf(e[1])))){
				sub_edges.add(new String[]{word_dict.getString(String.valueOf(e[0])),word_dict.getString(String.valueOf(e[1]))});
			}
		}
		if(graphModelIHT.funcValue > this.getMax_score()){
			this.setMax_score(graphModelIHT.funcValue);
			this.setBest_subgraph(graphModelIHT.resultNodes_Tail);
			this.setIgnore_words(words);
			this.setBestWindow(filename);
			this.setSubgraph(sub_edges);
			this.setWeights(weights);
		}
		long endTime2=System.currentTimeMillis();
		System.out.println(String.valueOf(endTime2-startTime));
	}
}

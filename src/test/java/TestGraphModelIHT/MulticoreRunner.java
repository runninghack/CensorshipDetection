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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.json.*;
import org.apache.commons.lang3.ArrayUtils;

import apdmIO.APDMInputFormat;
import graphModelIHT.GraphModelIHT;

public class MulticoreRunner {
	//================== Setters and getters start =====================
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
	private String up_down;
	public String getUp_down() {
		return up_down;
	}
	public void setUp_down(String up_down) {
		this.up_down = up_down;
	}
	//================== Setters and getters end =====================
	public MulticoreRunner(){
		this.setIs_first_time(true);
	}
	
	public MulticoreRunner(String month_folder, String result_folder, String up_down) throws IOException{
		String month_path = month_folder;
		String date = month_path.substring(month_path.lastIndexOf("/") - 8,month_path.lastIndexOf("/") );
		this.setIs_first_time(true);
		this.setUp_down(up_down);
		ArrayList<String> templist = new ArrayList<String>() {{add("No words");}};
		this.setIgnore_words(templist);

		Writer fileWriter = new BufferedWriter(new OutputStreamWriter(
			    new FileOutputStream(result_folder + date + "_results.txt"), "UTF-8"));
		
		for(int i=0;i<5;i++){
			System.out.println(i);
			run_month(month_path);
			String words_String= this.getIgnore_words().stream().collect(Collectors.joining("\t"));
			fileWriter.write(String.valueOf(i) + "," + this.getBestWindow() + "," + words_String + "\n");
			
			writeGraph(result_folder + date + "_" + String.valueOf(i) + "_graph.txt", this.getIgnore_words(),this.getWeights(),this.getSubgraph());
			
			this.setIs_first_time(false);
		}
		fileWriter.close();
	}
	
	private void run_month(String folder_path) throws IOException {
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
		System.out.println("***" + folder_path + " done in:" + String.valueOf(endTime-startTime) + "***");
	}
	
	private void writeGraph(String filePath, List<String> nodes, List<Double> weights, List<String[]> edges)throws IOException{
		Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"));
		try{
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
		catch(NullPointerException ex){
			fw.write("NO data");
			fw.close();
		}
	}

	public void run_timewindow(String rootFolder, String filename) throws IOException{
		//System.out.println(filename);
		long startTime=System.currentTimeMillis();
		//R
		//String[] elements = filename.split("_", -1);
		ArrayList<Integer> R_list = new ArrayList<Integer>();
		int day = Integer.parseInt(filename.substring(8,10));
		int length = Integer.parseInt(filename.split("_", -1)[1]);
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
		
		//detect only up or down
		if(this.getUp_down().equals("down")){
			for(int i=0;i<c.length;i++){
				if(c[i] > lambdas[i]){
					c[i] = lambdas[i];
				}
			}
		}else if(this.getUp_down().equals("up")){
			for(int i=0;i<c.length;i++){
				if(c[i] < lambdas[i]){
					c[i] = lambdas[i];
				}
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
				
		GraphModelIHT graphModelIHT = 
				new GraphModelIHT(edges, null, c, null, s/2, 1, s-1+0.0D, 10, false, null, null, null, R, lambdas) ;
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
			if(index_list.contains(e[0]) && 
					index_list.contains(e[1]) && 
					!word_dict.getString(String.valueOf(e[0])).equals(word_dict.getString(String.valueOf(e[1])))){
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
		//System.out.println(String.valueOf(endTime2-startTime));
	}
	
	private static List<String> get_subfolders(String folder){
	    File directory = new File(folder);
	    File[] fList = directory.listFiles();
	    List<String> paths = new ArrayList<String>();

	    for (File file : fList) {
	        if (file.isDirectory()) {
	        	paths.add(file.getAbsolutePath());
	        }
	    }
	    return paths;
	}
	
	private static List<String[]> generate_settings(String root, String out){
		List<String> paths = get_subfolders(root);
		List<String[]> settings = paths.stream()
				.map((x) -> new String[]{x+"/",out})
				.collect(Collectors.toList());
		return settings;
	}
	
	public static void run_single_month(String root, String filename) throws IOException{
		long startTime=System.currentTimeMillis();
		MulticoreRunner runner = new MulticoreRunner();
		runner.run_timewindow(root, filename);
		System.out.println(runner.getIgnore_words().stream().collect(Collectors.joining(", ")));
		long endTime2=System.currentTimeMillis();
		//System.out.println(String.valueOf(endTime2-startTime));
	}
	
	public static void run_multicore(){
		ExecutorService pool = Executors.newFixedThreadPool(50) ;
		
		//on rambo
		List<String[]> settings = generate_settings("/home/tayu/mexico/aggregate_graph/outputs/","/home/tayu/results/mexico/");
		settings.addAll(generate_settings("/home/tayu/brazil/aggregate_graph/outputs/","/home/tayu/results/brazil/"));
		settings.addAll(generate_settings("/home/tayu/venezuela/aggregate_graph/outputs/","/home/tayu/results/venezuela/"));

		//on local data
//		List<String[]> settings = generate_settings("data/mexico/","output/mexico/");
//		settings.addAll(generate_settings("data/brazil/","output/brazil/"));
//		settings.addAll(generate_settings("data/venezuela/","output/venezuela/"));

		
		for(String ud:new String[]{"up","down", "up_and_down"}){
			for(String[] s:settings){
				System.out.println("Load task:" + s[0] + " - " + ud);
				String writePath = s[1] + ud + "/";
				File file = new File(writePath);
				if (!file.exists()) {
					file.mkdirs();
				}
				pool.execute(new Thread(){ public void run(){
					try {new MulticoreRunner(s[0],writePath,ud);}
					catch (IOException e) {e.printStackTrace();}
					}
				});
			}
		}
		pool.shutdown();
	}
	
	public static void main(String[] args) throws IOException {
		run_multicore();
		//run_single_month("data/venezuela/2014_02/", "2014-02-01_5_graph.txt");
	}
}

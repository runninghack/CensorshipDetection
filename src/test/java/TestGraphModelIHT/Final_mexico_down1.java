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

public class Final_mexico_down1 {
	private double max_score = 0;
	private int[] best_subgraph;
	private List<String> ignore_words = new ArrayList<String>();
	private List<String> temp_words;
	private boolean is_first_time;
	private String bestWindow;
	private List<String[]> subgraph;
	private List<Double> weights;
	private String up_down;

	public Final_mexico_down1(){
		this.is_first_time = true;
	}
	
	public Final_mexico_down1(String month_folder, String result_folder, String up_down) throws IOException{
		String month_path = month_folder;
		String date = month_path.substring(month_path.lastIndexOf("/") - 8,month_path.lastIndexOf("/") );
		this.is_first_time = true;
		this.up_down = up_down;
		ArrayList<String> templist = new ArrayList<String>() {{add("No words");}};
		this.ignore_words.addAll(templist);

		Writer fileWriter = new BufferedWriter(new OutputStreamWriter(
			    new FileOutputStream(result_folder + date + "_results.txt"), "UTF-8"));
		
		for(int i=0;i<5;i++){
			System.out.println(i);
			run_month(month_path);
			String words_String= this.temp_words.stream().collect(Collectors.joining("\t"));
			fileWriter.write(String.valueOf(i) + "," + this.bestWindow + "," + words_String + "\n");
			
			writeGraph(result_folder + date + "_" + String.valueOf(i) + "_graph.txt", this.temp_words,this.weights,this.subgraph);
			this.temp_words = new ArrayList<String>();
			this.is_first_time=false;
		}
		fileWriter.close();
	}
	
	private void run_month(String folder_path) throws IOException {
		System.out.println(folder_path);
		long startTime=System.currentTimeMillis();
		File folder = new File(folder_path);
		File[] files = folder.listFiles();

		List<File> listOfFiles=Arrays.asList(files);
		List<File> graphFiles = listOfFiles.stream()
			    .filter(f -> f.getName().contains("graph")).collect(Collectors.toList());
		this.max_score = 0;
		for(int i=0;i<graphFiles.size();i++){
			String fname = graphFiles.get(i).getName();
			this.run_timewindow(folder_path,fname);
		}
		this.ignore_words.addAll(this.temp_words);
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
			fw.write("No data");
			fw.close();
		}
	}

	public void run_timewindow(String rootFolder, String filename) throws IOException{
		System.out.println(filename);
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
		double[] c2 = new double[c.length];
		for(int i=0;i<c.length;i++){
			c2[i] = c[i];
		}
		
		//detect only up or down
		if(this.up_down.equals("down")){
			for(int i=0;i<c.length;i++){
				if(c2[i] > lambdas[i]){
					c2[i] = lambdas[i];
				}
			}
		}else if(this.up_down.equals("up")){
			for(int i=0;i<c.length;i++){
				if(c2[i] < lambdas[i]){
					c2[i] = lambdas[i];
				}
			}
		}
		
		
		if(!this.is_first_time){
			for(String iw: this.ignore_words){
				for(int i=0;i<apdm.numNodes;i++){
					if(word_dict.getString(String.valueOf(i)).equals(iw)){
						c2[i] = lambdas[i];
					}
				}
			}
		}
		
		//sparsity s
		//int s = apdm.trueSubGraphNodes.length ;
		int s = 8;
				
		GraphModelIHT graphModelIHT = 
				new GraphModelIHT(edges, null, c2, null, s/2, 1, s-1+0.0D, 10, false, null, null, null, R, lambdas) ;
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
		if(graphModelIHT.funcValue > this.max_score){
			this.max_score = graphModelIHT.funcValue;
			this.best_subgraph = graphModelIHT.resultNodes_Tail;
			this.temp_words = words;
			this.bestWindow = filename;
			this.subgraph = sub_edges;
			this.weights = weights;
		}
		long endTime2=System.currentTimeMillis();
		System.out.println(String.valueOf(endTime2-startTime));
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
		Final_mexico_down1 runner = new Final_mexico_down1();
		runner.run_timewindow(root, filename);
		System.out.println(runner.temp_words.stream().collect(Collectors.joining(", ")));
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
					try {new Final_mexico_down1(s[0],writePath,ud);}
					catch (IOException e) {e.printStackTrace();}
					}
				});
			}
		}
		pool.shutdown();
	}
	
	public static void run_instance(String month_folder, String result_folder, String up_down)throws IOException {
		File file = new File(result_folder);
		if (!file.exists()) {
			file.mkdirs();
		}
		new Final_mexico_down1(month_folder,result_folder,up_down);
	}
	
	public static void main(String[] args) throws IOException {
		//run_multicore();
		//run_single_month("data/venezuela/2014_02/", "2014-02-01_5_graph.txt");
		run_instance("/home/tayu/mexico/aggregate_graph/outputs/2014_01/","/home/zhanglei/results/mexico/down/","down");
		run_instance("/home/tayu/mexico/aggregate_graph/outputs/2014_02/","/home/zhanglei/results/mexico/down/","down");
		run_instance("/home/tayu/mexico/aggregate_graph/outputs/2014_03/","/home/zhanglei/results/mexico/down/","down");
		run_instance("/home/tayu/mexico/aggregate_graph/outputs/2014_04/","/home/zhanglei/results/mexico/down/","down");
		run_instance("/home/tayu/mexico/aggregate_graph/outputs/2014_05/","/home/zhanglei/results/mexico/down/","down");
		run_instance("/home/tayu/mexico/aggregate_graph/outputs/2014_06/","/home/zhanglei/results/mexico/down/","down");

	}

}

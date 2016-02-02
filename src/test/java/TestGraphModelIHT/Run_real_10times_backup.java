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

public class Run_real_10times_backup {
	
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
	public static void main(String[] args) throws IOException {
		String month_path = "data/RealData_China/2015_05/";
		Run_real_10times_backup runner = new Run_real_10times_backup();
		runner.setIs_first_time(true);

		//FileWriter fileWriter = new FileWriter(new File("output/" + month_path.substring(month_path.lastIndexOf("/")) + "_results.txt"),true);
		Writer fileWriter = new BufferedWriter(new OutputStreamWriter(
			    new FileOutputStream("output/" + month_path.substring(month_path.lastIndexOf("/") - 8,month_path.lastIndexOf("/") ) + "_results.txt"), "UTF-8"));

		for(int i=0;i<10;i++){
			System.out.println(i);
			runner.run_month(month_path);
			String words_String= runner.getIgnore_words().stream().collect(Collectors.joining("\t"));
			fileWriter.write(String.valueOf(i) + "," + runner.getBestWindow() + "," + words_String + "\n");
			runner.setIs_first_time(false);
		}
		
		fileWriter.close();
	}
	
	
	public void run_month(String folder_path) throws IOException {
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
	}
	
	public void run_timewindow(String rootFolder, String filename) throws IOException{
		String[] elements = filename.split("_", -1);
		ArrayList<Integer> R_list = new ArrayList<Integer>();
		int day = Integer.parseInt(elements[2]);
		int length = Integer.parseInt(elements[3]);
		IntStream.range(day,day + length)
				.forEach(R_list::add);
		int[] R = ArrayUtils.toPrimitive(R_list.toArray(new Integer[0]));
		
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
		
		//word dictionary
		BufferedReader br2 = new BufferedReader(new FileReader(rootFolder+ filename.replace("graph","index")));
		String line2 = br2.readLine();
		JSONObject obj2 = new JSONObject(line2.trim());
		br2.close();

		//edges
		ArrayList<Integer[]> edges = new ArrayList<Integer[]>() ;	//1. graph G, input parameter G
		for(int[] edge:apdm.inputData.edges.keySet()){
			edges.add(new Integer[]{edge[0],edge[1]}) ;
		}
		
		double[] c = apdm.getPValue();
		if(!this.getIs_first_time()){
			for(String iw: this.getIgnore_words()){
				for(int i=0;i<apdm.numNodes;i++){
					if(obj2.getString(String.valueOf(i)).equals(iw)){
						c[i] = lambdas[i];
					}
				}
			}
		}
		
		//sparsity s
		int s = apdm.trueSubGraphNodes.length ;
		
		Arrays.sort(apdm.trueSubGraphNodes);
		
		GraphModelIHT graphModelIHT = new GraphModelIHT(edges, null, c, null, s, 1, s-1+0.0D, 10, false, null, null, null, R, lambdas) ;
		
		List<Integer> index_list= IntStream.of(graphModelIHT.resultNodes_Tail).boxed().collect(Collectors.toList());
		List<String> words = new ArrayList<String>();
		for(Integer a:index_list){
			words.add(obj2.getString(String.valueOf(a)));
		}

		if(graphModelIHT.funcValue > this.getMax_score()){
			this.setMax_score(graphModelIHT.funcValue);
			this.setBest_subgraph(graphModelIHT.resultNodes_Tail);
			this.setIgnore_words(words);
			this.setBestWindow(filename);
		}
	}
}

package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import createData.ALS.CreateData;
import createData.Hepatitis.TableGeneratorCategories;

public class MultiHMMmissingRemover {
	private static String[] exams = {"GPT","GOT","ZTT","TTT","T-BIL","D-BIL","I-BIL","ALB","CHE","T-CHO","TP","Type","Activity"};	
	private static String data = "C:\\hepat_data030704\\";
	private static String path = data +"data\\";
	private static String outPath = "C:\\Users\\Daniel\\Documents\\GitHub\\HMMinR\\multidata\\";
	private static HashMap<String,Boolean> keep = new HashMap<String, Boolean>();


	public static void main(String[] args) { 
		
		MultiHMMmissingRemover y = new MultiHMMmissingRemover();
		
		int steps = TableGeneratorCategories.steps;
		HashMap<String, HashMap<String,String>> read = new HashMap<String, HashMap<String,String>>();
		HashSet<String> s1 = new HashSet<String>();
		try {
			for(int i= 0; i< exams.length;i++){
				HashSet<String> s2 = new HashSet<String>();
				HashMap<String,String> lines = new HashMap<String,String>();
				BufferedReader in = new BufferedReader(new FileReader(path+exams[i]+".csv"));
				String line = in.readLine();
				//out.write(line + "\n");
				int size = line.split(",",-1).length;
				String[] split;
				while((line = in.readLine()) != null){
					split = line.split(",");
					if(split[0].equals("307")){
						@SuppressWarnings("unused")
						int j = split.length; 
					}	
					boolean missing = false;
					for (String string : split) {
						if(string.equals("")){
							missing = true;
							break;
						}
					}		
					if(!missing && split.length == size){
						lines.put(split[0],line);
						if(i==0){
							//keep.put(split[0],false);
							s1.add(split[0]);
						}
						s2.add(split[0]);
					}
				}
				in.close();	
				s1.retainAll(s2);
				read.put(exams[i], lines);
			}
			for(String s : s1){
				keep.put(s, true);
			}

			HashMap<String, String> multi = y.createMultiLines(s1,read,steps);

			y.buildEachExam(read, multi,steps);
			CreateData create =  new CreateData();
			create.CSV2arff(outPath,exams[0]);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private HashMap<String, String> createMultiLines( HashSet<String> s1, HashMap<String, HashMap<String, String>> read, int steps) {
		HashMap<String, String> multi = new HashMap<String, String>();
		String line = null;
		String header = null;
		boolean head = true;
		for(String id: s1 ){
			for(int j = 1; j < steps ; j++ ){
				for(int i= 0; i< exams.length;i++){
					HashMap<String, String> a = read.get(exams[i]);
					if(j==1 && i == 0){
						line = a.get(id).split(",",-1)[0] + ","; 
						header = "PatientID,";
					}
					line += a.get(id).split(",",-1)[j] + "_"+ exams[i]+ ",";
					header += exams[i]+"_"+(j-1)+ "," ;
				}
			}
			multi.put(id,line);
			if(head){
				multi.put("header",	header);
			}
		}
		//System.out.println(multi.get("header"));
		//System.out.println(s1.size());
		return multi;
	}
	
	class StringComparator implements Comparator<String> {
	    @Override
	    public int compare(String s1, String s2) {
	    	Integer a = Integer.parseInt(s1);
	    	Integer b = Integer.parseInt(s2);
	        return a < b ? -1 : a == b ? 0 : 1;
	    }
	}

	private void buildEachExam(HashMap<String, HashMap<String, String>> read, HashMap<String, String> multi, int steps)
			throws IOException {
		for(int i= 0; i< exams.length;i++){
			BufferedWriter out = new BufferedWriter(new FileWriter(outPath+exams[i]+".csv"));
			String header = multi.get("header");
			header += exams[i]+ (steps-1) + "\n"; 
			out.write(header);
			HashMap<String, String> e = read.get(exams[i]);
			ArrayList<String> ids =  new ArrayList<String>();
			ids.addAll(e.keySet());
			Collections.sort(ids, new StringComparator());
			for(String a: ids){
				String s = e.get(a);
				String id = s.split(",",-1)[0];
				if(keep.get(id) != null){
					String output = multi.get(id);
					output += s.split(",",-1)[(steps-1)] + "_"+ exams[i];
					out.write(output + "\n");
				}
			}			
			out.close();
		}
	}
}
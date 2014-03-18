package createData.ALS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Stats {
	private static  String path = "C:" + File.separator + "PROACT_2013_08_27_ALL_FORMS" +
			File.separator + "FormatedData"+ File.separator + "Approach2" + File.separator;
	private static BufferedReader in;

	public Stats(BufferedReader bufferedReader) {
		in = bufferedReader;
	}

	public static void main(String[] args) {
		Stats stat;
		try {
			stat = new Stats(new BufferedReader(new FileReader(path+"DiagnoseData.csv")));
			stat.countPatients();
			in.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void countPatients() throws IOException {
		String line;
		in.readLine();
		HashMap<String,Integer> bal = new HashMap<String, Integer>();
		HashMap<String,Integer> lastValue = new HashMap<String, Integer>();
		HashMap<String,Integer> count = new HashMap<String, Integer>();
		int patients = 0;
		int male = 0;
		int female = 0;
		String last = null;
		String lastClass = null;
		String[] split= null;
		ArrayList<String> c = new ArrayList<String>();
		while((line = in.readLine())!= null){
			split = line.split(",",-1);
			if(last == null){
				last = split[0];
				patients++;
				if(split[1].equals("Female")){
					female++;
				}else{
					male++;
				}
				String cla = split[split.length-1];
				lastClass = cla;
				Integer val = bal.get(cla);
				if(val == null){
					bal.put(cla,1);
				}else{
					bal.put(cla, (val+1));
				}
				c.add(line);
			}else{
				if(split[0].equals(last)){
					String cla = split[split.length-1];
					lastClass = cla;
					Integer val = bal.get(cla);
					if(val == null){
						bal.put(cla,1);
					}else{
						bal.put(cla, (val+1));
					}
					c.add(line);
				}else{
					last = split[0];
					patients++;
					if(split[1].equals("Female")){
						female++;
					}else{
						male++;
					}
					Integer val = lastValue.get(lastClass);
					if(val == null){
						lastValue.put(lastClass, 1);
					}else{
						lastValue.put(lastClass, val+1);
					}
					String cla = split[split.length-1];
					val = bal.get(cla);
					if(val == null){
						bal.put(cla,1);
					}else{
						bal.put(cla, (val+1));
					}
					c = new ArrayList<String>();
					c.add(line);
				}
			}
		}
		System.out.println("Patients\t"+ patients);
		System.out.println("Male\t" + male);
		System.out.println("Female\t" + female);
		System.out.println("\nALL");
		System.out.println("Classe\t Num");
		int total = 0;
		for(String key:bal.keySet()){
			total+=bal.get(key);
		}
		for(String key:bal.keySet()){
			System.out.println(key+"\t"+bal.get(key)+"\t"+(bal.get(key)*100.0)/total);
		}
		System.out.println("\nLAST");
		System.out.println("Classe\t Num");
		total = 0;
		for(String key:lastValue.keySet()){
			total+=lastValue.get(key);
		}
		for(String key:lastValue.keySet()){
			System.out.println(key+"\t"+lastValue.get(key)+"\t"+(lastValue.get(key)*100.0)/total);
		}
	}



}

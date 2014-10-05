package tdm.classification.createData.ALSNEW;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Normalize {
	
	public String path = "C:" + File.separator + "PROACT_2013_08_27_ALL_FORMS" + File.separator + "DiagnosisData"+ File.separator;

	public static void main(String[] args) {
	}
	
	public void findMinMax() throws NumberFormatException, IOException{
		BufferedReader in = new BufferedReader(new FileReader(path+"DiagnoseDataStats.csv"));

		String header = in.readLine();
		String[] head = header.split(",",-1);

		ArrayList<Float> max = new ArrayList<Float>();
		ArrayList<Float> min = new ArrayList<Float>();
		ArrayList<HashMap<Integer,Integer>> counts = new ArrayList<HashMap<Integer,Integer>>();
		for(int i=2; i < head.length-1 ; i++){
			HashMap<Integer, Integer> tree = new HashMap<Integer, Integer>();
			for(int j=0; j<10;j++){
				tree.put(j, 0);
			}
			counts.add(tree);
			max.add(Float.MIN_VALUE);
			min.add(Float.MAX_VALUE);
		}
		in.close();
	}
}

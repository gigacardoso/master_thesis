package createData.ALS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Discretize {

	public String path = "C:" + File.separator + "PROACT_2013_08_27_ALL_FORMS" + File.separator + "DiagnosisData"+ File.separator;
	/**
	 * 10 - {(float) 6.1,(float) 13.7, (float) 11.7, (float) 0.653,(float) 0.544,(float) 0.549,(float) 7.9,(float) 12.6,(float) 9.9,(float) 5.1,(float) 9.86,(float) 22.830 };
	 */
	public static void main(String[] args) {
		try {
			Discretize d = new Discretize();
			d.stats();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stats() throws NumberFormatException, IOException{
		BufferedReader in = new BufferedReader(new FileReader(path+"DiagnoseDataStats.csv"));

		Float[] divider = {(float) 6.1,(float) 13.7, (float) 11.7, (float) 0.653,(float) 0.544,
				(float) 0.549,(float) 7.9,(float) 12.6,(float) 9.9,(float) 5.1,(float) 9.86,(float) 22.830 };
		Float[] mins = {(float) 18,(float) 59,(float) 0,(float) 0,(float) 0.2,(float) 0,(float) 40,
				(float) 72,(float) 45,(float) 9,(float) 0,(float) 36.7};
		String header = in.readLine();
		String[] head = header.split(",",-1);
		String line;

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

		while ((line = in.readLine()) != null) {
			String[] splited = line.split(",",-1);
			//SubjectID,Sex,Age,Height,P_of_Normal_(Trial_1),Subject_Liters_(Trial_1),Subject_Liters_(Trial_2),Subject_Liters_(Trial_3),Blood_Pressure_(Diastolic),Blood_Pressure_(Systolic),Pulse,Respiratory_Rate,Temperature,Weight,ALSFRS-R_Total
			for(int i=2; i < head.length-1 ; i++){
				if(splited[i].length()>0){
					int c = (int)((Float.parseFloat(splited[i])-mins[i-2])/divider[i-2]);
//					System.out.println(c);
					if(c == 10){
						c=9;
					}
					int current = counts.get(i-2).get(c);
					counts.get(i-2).put(c,++current);
				}
			}
		}
		in.close();

		for(int i=2; i < head.length-1 ; i++){
			System.out.println(head[i]);
			HashMap<Integer, Integer> tree = counts.get(i-2);
			for(Integer c:tree.keySet()){
				System.out.println("count "+c +"\t-\t"+ tree.get(c));
			}
		}
	}
}

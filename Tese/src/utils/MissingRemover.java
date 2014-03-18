package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MissingRemover {

	private static String[] exams = {"GPT","GOT","ZTT","TTT","T-BIL","D-BIL","I-BIL","ALB","CHE","T-CHO","TP","Type","Activity"};	
	private static String data = "C:\\hepat_data030704\\";
	private static String path = data +"data\\";
	private static String outPath = "C:\\Users\\Daniel\\Documents\\GitHub\\HMMinR\\";

	public static void main(String[] args) {
		try {
			for(int i= 0; i< exams.length;i++){
				BufferedReader in = new BufferedReader(new FileReader(path+exams[i]+".csv"));
				BufferedWriter out = new BufferedWriter(new FileWriter(outPath+exams[i]+".csv"));
				String line = in.readLine();
				out.write(line + "\n");
				int size = line.split(",",-1).length;
				String[] split;
				while((line = in.readLine()) != null){
					split = line.split(",");
					if(split.length == size){
						out.write(line + "\n");
					}
				}
				in.close();
				out.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

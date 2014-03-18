package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;

public class MissingRemover {
	
	private static String[] exams = {"GPT","GOT","ZTT","TTT","T-BIL","D-BIL","I-BIL","ALB","CHE","T-CHO","TP","Type","Activity"};	
	private static String data = "C:\\hepat_data030704\\";
	private static String path = data +"data\\";
	
	public static void main(String[] args) {
		for(int i= 0; i< exams.length;i++){
			try {
				BufferedReader in = new BufferedReader(new FileReader(path+exams[i]+".csv"));
				BufferedWriter out = new BufferedWriter(new FileWriter(path+exams[i]+".csv"));
				in.close();
				out.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}

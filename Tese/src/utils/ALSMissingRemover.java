package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ALSMissingRemover {

	private int[] steps = {3,5,6};
	private static String data = "C:\\ALSHMM\\data\\";
	private static String multidata = "C:\\ALSHMM\\multidata\\";

	public static void main(String[] args) {

		try {
			ALSMissingRemover a = new ALSMissingRemover();
			a.remove(data);
			a.remove(multidata);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void remove(String path) throws IOException {
		for(int step: steps){	
			removeMissingFromFolder(path + step+"\\");
		}
	}

	private void removeMissingFromFolder(String path)
			throws FileNotFoundException, IOException {
		File file = new File(path);
		String[] myFiles, split;
		String line = null;
		if (file.isDirectory()) {
			myFiles = file.list();
			for (int i = 0; i < myFiles.length; i++) {
				File myFile = new File(file, myFiles[i]);
				if (!myFile.isDirectory()) {
					String name = myFile.getName();
					BufferedReader in = new BufferedReader(new FileReader(myFile));
					BufferedWriter out = new BufferedWriter(new FileWriter(path+"nomissing\\"+name));
					out.write(in.readLine() + "\n");
					
					while((line = in.readLine())!= null ){
						split = line.split(",",-1);
						boolean write = true;
						for (int j = 0; j < split.length; j++) {
							if(split[j].equals("missing")){
								write = false;
								break;
							}
						}
						if(write){
							out.write(line+"\n");
						}
					}
					in.close();
					out.close();
				}	
			}
		}
	}

}

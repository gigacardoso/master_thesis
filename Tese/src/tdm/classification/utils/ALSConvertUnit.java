package tdm.classification.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class ALSConvertUnit {

	private  String path = "C:" + File.separator + "PROACT_2013_08_27_ALL_FORMS" + File.separator + "ToUse"+ File.separator;
	private HashMap<String,Double> heights = new HashMap<String, Double>();

	public static void main(String[] args) {
		ALSConvertUnit a = new ALSConvertUnit();
		try {

			a.convertTemperature("VITALS_Data_original.csv","VITALS_Data_temp.csv");
			System.out.println("..");
			a.convertHeight("VITALS_Data_temp.csv","VITALS_Data_height.csv");
			a.saveheights();
			System.out.println("..");
			a.convertWeight("VITALS_Data_temp.csv","VITALS_Data_weight.csv");
			a.addHeights("VITALS_Data_weight.csv","VITALS_Data.csv");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addHeights(String inputfile, String outputfile) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(path+inputfile));
		BufferedWriter out = new BufferedWriter(new FileWriter(path+outputfile));
		String line = in.readLine();
		out.write(line + "\n");
		String[] split = line.split(",",-1);
		int index = 0;
		for (int i = 0; i < split.length; i++) {
			if(split[i].equals("Height")){
				index = i;
				break;
			}
		}
		while((line = in.readLine()) != null){
			split = line.split(",",-1);
			String output = "";
			for (int i = 0; i < split.length; i++) {
				if(i == index){
					Double height = heights.get(split[0]) ;
					if(height == null){
						output += ",";
					}else{
						output += height + ",";
					}
				}else{
					output += split[i] + ",";
				}
			}
			output = output.substring(0, output.length() -1);
			out.write(output + "\n");
		}
		in.close();
		out.close();
	}

	private void convertHeight(String inputfile, String outputfile) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(path+inputfile));
		BufferedWriter out = new BufferedWriter(new FileWriter(path+outputfile));
		String line = in.readLine();
		out.write(line + "\n");
		String[] split = line.split(",",-1);
		int index = 0;
		int cm = 0;
		int inch = 0;
		int notemp = 0;
		for (int i = 0; i < split.length; i++) {
			if(split[i].equals("Height")){
				index = i;
				break;
			}
		}
		while((line = in.readLine()) != null){
			split = line.split(",",-1);
			String tmp = split[index];
			if(tmp.length()>0){
				Double height = Double.parseDouble(tmp);
				if(height >= 80){
					cm++;
					out.write(line+ "\n");
				}
				else{
					String output = "";
					double cel = 0;
					for (int i = 0; i < split.length; i++) {
						if(i == index){
							cel = (height/0.39370) ;
							output += cel + ",";
						}else{
							output += split[i] + ",";
						}
					}
					output = output.substring(0, output.length() -1);
					out.write(output + "\n");
					inch++;
//					System.out.println(cel);
				}
			}else{
				notemp++;
				out.write(line+ "\n");
			}
		}
		in.close();
		out.close();
		System.out.println("Cm -" + cm);
		System.out.println("Inches -" + inch);
		System.out.println("NoHeight -" + notemp);
	}

	private void saveheights() throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(path+"VITALS_Data_height.csv"));
		String line = in.readLine();
		String[] split = line.split(",",-1);
		int index = 0;
		for (int i = 0; i < split.length; i++) {
			if(split[i].equals("Height")){
				index = i;
				break;
			}
		}
		while((line = in.readLine()) != null){
			split = line.split(",",-1);
			String tmp = split[index];
			if(tmp.length()>0){
				Double i = Double.parseDouble(tmp);
				heights.put(split[0], i);
			}
		}
		in.close();
		HashMap<Integer, Integer> counts = new HashMap<Integer, Integer>();
		for (String string : heights.keySet()) {
			double h = heights.get(string);
//			DecimalFormat df = new DecimalFormat("#.#");
//			String s = df.format(height);
//			s = s.replace(",",".");
//			height = Double.parseDouble(s);
			int height  = (int) h;
			Integer i =counts.get(height);
			if(i==null){
				i = 0;
			}
			i++;
			counts.put(height, i);
		}
		int total = 0;
		int n = 0;
		for (Integer i : counts.keySet()) {
			System.out.println("Bin "+i+" -> "+ counts.get(i));
			total += i*counts.get(i);
			n += counts.get(i);
		}
		System.out.println("keys - " + counts.keySet().size());
		System.out.println(total);
		System.out.println(n);
		System.out.println("Average - " + (total/n));
	}

	private void convertWeight(String inputfile, String outputfile) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(path+inputfile));
		BufferedWriter out = new BufferedWriter(new FileWriter(path+outputfile));
		String line = in.readLine();
		out.write(line + "\n");
		String[] split = line.split(",",-1);
		int index = 0;
		int kilo = 0;
		int pound = 0;
		int kg = 0;
		int tallheavy = 0;
		int shortheavy = 0;
		int shortlight = 0;
		int notemp = 0;
		for (int i = 0; i < split.length; i++) {
			if(split[i].equals("Weight")){
				index = i;
				break;
			}
		}
		int unit = index+2;
		int u = 0;
		HashMap<String,Integer> units = new HashMap<String, Integer>();
		boolean has =false;
		int kilounit = 0;
		while((line = in.readLine()) != null){
			split = line.split(",",-1);
			String tmp = split[index];
			if(tmp.length()>0){
				if(split[unit].length()>0){
					u++;
					Integer i = units.get(split[unit]);
					if(i == null){
						i= 1;
					}else{
						i++;
					}
					units.put(split[unit],i);
					has = true;
				}
				Double weight = Double.parseDouble(tmp);
				Double height = heights.get(split[0]);
				if(height == null){
					height = 0.0;
				}
				if(weight > 130 && height < 160){
					shortheavy++;
					if(has){
						//kg
						kilo++;
						kilounit++;
						has = false;
						out.write(line+ "\n");
						//						System.out.println("weight - "+weight );
						//						System.out.println("height - "+height );
					}
					else{
						//pounds
						pound++;
						String output = "";
						double cel = 0;
						for (int i = 0; i < split.length; i++) {
							if(i == index){
								cel = ((weight/2.2046)) ;
								output += cel + ",";
							}else{
								output += split[i] + ",";
							}
						}
						output = output.substring(0, output.length() -1);
						out.write(output + "\n");
					}
				}
				else{
					if(weight > 130){
						//kg
						kilo++;
						out.write(line+ "\n");
						tallheavy++;
					}else{
						if(weight > 86 && height < 165 && height != 0){
							//pounds 
							pound++;
							shortlight++;
							String output = "";
							double cel = 0;
							for (int i = 0; i < split.length; i++) {
								if(i == index){
									cel = ((weight/2.2046)) ;
									output += cel + ",";
								}else{
									output += split[i] + ",";
								}
							}
							output = output.substring(0, output.length() -1);
							out.write(output + "\n");
						}else{
							//kilogram
							out.write(line+ "\n");
							kg++;
							kilo++;
						}
					}
				}
			}			else{
				notemp++;
				out.write(line+ "\n");
			}
		}
		in.close();
		out.close();
		System.out.println("Kg -" + kilo);
		System.out.println("Pound -" + pound);
		//		System.out.println("tallHeavy (kg) -" + tallheavy);
		//		System.out.println("shortHeavy (pounds) -" + shortheavy);
		//		System.out.println("shortLight (pounds) -" + shortlight);
		//		System.out.println("Kg -" + kg);
		//		System.out.println("With Unit -" + u);
		//		for (String string : units.keySet()) {
		//			System.out.println(string +" - "+ units.get(string));
		//		}
		//		System.out.println("KiloUnite -" + kilounit );
	}

	private void convertTemperature(String inputfile, String outputfile) throws IOException{
		BufferedReader in = new BufferedReader(new FileReader(path+inputfile));
		BufferedWriter out = new BufferedWriter(new FileWriter(path+outputfile));
		String line = in.readLine();
		out.write(line + "\n");
		String[] split = line.split(",",-1);
		int index = 0;
		int c = 0;
		int f = 0;
		int notemp = 0;
		for (int i = 0; i < split.length; i++) {
			if(split[i].equals("Temperature")){
				index = i;
				break;
			}
		}
		while((line = in.readLine()) != null){
			split = line.split(",",-1);
			String tmp = split[index];
			if(tmp.length()>0){
				Double temp = Double.parseDouble(tmp);
				if(temp <45){
					c++;
					out.write(line+ "\n");
				}
				else{
					String output = "";
					double cel = 0;
					for (int i = 0; i < split.length; i++) {
						if(i == index){
							cel = ((temp - 32.0) * (5.0/9.0)) ;
							output += cel + ",";
						}else{
							output += split[i] + ",";
						}
					}
					output = output.substring(0, output.length() -1);
					out.write(output + "\n");
					f++;
					System.out.println(cel);
				}
			}else{
				notemp++;
				out.write(line+ "\n");
			}
		}
		in.close();
		out.close();
		System.out.println("C -" + c);
		System.out.println("F -" + f);
		System.out.println("NoTemp -" + notemp);
	}
}

package createData.Hepatitis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TableGenerators {

	public void createDateTable() throws IOException {
		FileWriter table = new FileWriter(new File("DimDate.table"));
		table.write("DateID\tMonth\tYear\n");

		for (int i = 1981; i < 2002; i++) {
			for (int j = 1; j < 13; j++) {
				String id = String.format("%d%02d", i, j);
				String month = String.format("%02d", j);
				String year = String.format("%d", i);
				table.write(id + "\t" + month + "\t" + year + "\n");
			}
		}

		table.close();
	}

	public void createBiopsyTable() throws IOException {
		FileWriter table = new FileWriter(new File("DimBiopsy.table"));
		table.write("BiospsyID\tType\tFibrosis\tActivity\n");

		int id = 1;

		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 5; j++) {
				for (int k = 0; k < 5; k++) {
					String type;
					String activity;
					if (i == 0) {
						type = "B";
					} else {
						type = "C";
					}
					if (k == 4) {
						activity = "";
					} else {
						activity = Integer.toString(k);
					}
					table.write(id + "\t" + type + "\t" + j + "\t" + activity
							+ "\n");

					id++;
				}
			}
		}

		table.close();
	}

	public void createExameTable() throws IOException {
		FileWriter table = new FileWriter(new File("DimExam.table.new"));

		String[][] tests = {{"GPT","GOT","ZTT", "TTT", "T-BIL", "D-BIL", "I-BIL"}, {"ALB","CHE","T-CHO", "TP"},{"WBC","PLT"}, {"RBC","HGB","HCT", "MCV"}};
		String[][] results = {{"N","H","VH","UH"},{"VL","L","N","H","VH"},{"UL","VL","L","N","H"},{"L","N","H"}};

		int examID = 0;

		table.write("ExamID\tCodeName\tResult\n");

		for(int i = 0;i < tests.length; i++) {
			for (int j = 0; j < tests[i].length; j++) {
				for (int k = 0; k < results[i].length; k++) {
					examID++;
					table.write(examID + "\t" + tests[i][j] + "\t" + tests[i][j] + "_" + results[i][k] + "\n");

				}
			}
		}

		table.close();
	}

	public void cleanExamTable() throws IOException {
		FileWriter table = new FileWriter(new File("DimExam.table.2nd"));
		BufferedReader reader = new BufferedReader(new FileReader(
				"olab_e030704.csv"));

		String entry = reader.readLine();
		table.write(entry + "\n");
		entry = reader.readLine();

		while ((entry = reader.readLine()) != null) {
			String[] line = entry.split("\t");
			
			if (line[3].equals("GPT") || line[3].equals("GOT")
					|| line[3].equals("ZTT") || line[3].equals("TTT")
					|| line[3].equals("T-BIL") || line[3].equals("D-BIL")
					|| line[3].equals("I-BIL") || line[3].equals("ALB")
					|| line[3].equals("CHE") || line[3].equals("T-CHO")
					|| line[3].equals("TP")) {
				table.write(entry + "\n");
			}
		}
		table.close();
		reader.close();
	}

	public String categorizeExamResult(String exam, double result) {
		String value = "";

		if (exam.equals("GPT") || exam.equals("GOT")){
			if(result < 40) {
				value = "N";
			} else if(result >= 40 && result < 100) {
				value = "H";
			} else if(result >= 100 && result < 200) {
				value = "VH";
			} else if(result >= 200) {
				value = "UH";
			}
		} else if(exam.equals("ZTT")) {
			if(result < 12) {
				value = "N";
			} else if(result >= 12 && result < 24) {
				value = "H";
			} else if(result >= 24 && result < 36) {
				value = "VH";
			} else if(result >= 36) {
				value = "UH";
			}
		} else if(exam.equals("TTT")) {
			if(result < 5) {
				value = "N";
			} else if(result >= 5 && result < 10) {
				value = "H";
			} else if(result >= 10 && result < 15) {
				value = "VH";
			} else if(result >= 15) {
				value = "UH";
			}
		} else if(exam.equals("T-BIL")) {
			if(result < 1.2) {
				value = "N";
			} else if(result >= 1.2 && result < 2.4) {
				value = "H";
			} else if(result >= 2.4 && result < 3.6) {
				value = "VH";
			} else if(result >= 3.6) {
				value = "UH";
			}
		} else if(exam.equals("D-BIL")) {
			if(result < 0.3) {
				value = "N";
			} else if(result >= 0.3 && result < 0.6) {
				value = "H";
			} else if(result >= 0.6 && result < 0.9) {
				value = "VH";
			} else if(result >= 0.9) {
				value = "UH";
			}
		} else if(exam.equals("I-BIL")) {
			if(result < 0.9) {
				value = "N";
			} else if(result >= 0.9 && result < 1.8) {
				value = "H";
			} else if(result >= 1.8 && result < 2.7) {
				value = "VH";
			} else if(result >= 2.7) {
				value = "UH";
			}
		} else if(exam.equals("ALB")) { 
			if(result < 3) {
				value = "VL";
			} else if(result >= 3 && result < 3.9) {
				value = "L";
			} else if(result >= 3.9 && result < 5.1) {
				value = "N";
			} else if(result >= 5.1 && result < 6) {
				value = "H";
			} else if(result >= 6) {
				value = "VH";
			}
		} else if(exam.equals("CHE")) { 
			if(result < 100) {
				value = "VL";
			} else if(result >= 100 && result < 180) {
				value = "L";
			} else if(result >= 180 && result < 430) {
				value = "N";
			} else if(result >= 430 && result < 510) {
				value = "H";
			} else if(result >= 510) {
				value = "VH";
			}
		} else if(exam.equals("T-CHO")) { 
			if(result < 90) {
				value = "VL";
			} else if(result >= 90 && result < 125) {
				value = "L";
			} else if(result >= 125 && result < 220) {
				value = "N";
			} else if(result >= 220 && result < 255) {
				value = "H";
			} else if(result >= 255) {
				value = "VH";
			}
		} else if(exam.equals("TP")) { 
			if(result < 5.5) {
				value = "VL";
			} else if(result >= 5.5 && result < 6.5) {
				value = "L";
			} else if(result >= 6.5 && result < 8.2) {
				value = "N";
			} else if(result >= 8.2 && result < 9.2) {
				value = "H";
			} else if(result >= 9.2) {
				value = "VH";
			}
		} else if(exam.equals("WBC")) {
			if(result < 2) {
				value = "UL";
			} else if(result >= 2 && result < 3) {
				value = "VL";
			} else if(result >= 3 && result < 4) {
				value = "L";
			} else if(result >= 4 && result < 9) {
				value = "N";
			} else if(result >= 9) {
				value = "H";
			}
		} else if(exam.equals("PLT")) {
			if(result < 50) {
				value = "UL";
			} else if(result >= 50 && result < 100) {
				value = "VL";
			} else if(result >= 100 && result < 150) {
				value = "L";
			} else if(result >= 150 && result < 350) {
				value = "N";
			} else if(result >= 350) {
				value = "H";
			}
		} else if(exam.equals("RBC")) {
			if(result < 3.75) {
				value = "L";
			} else if(result >= 3.75 && result < 5) {
				value = "N";
			} else if(result >= 5) {
				value = "H";
			}
		} else if(exam.equals("HGB")) {
			if(result < 12) {
				value = "L";
			} else if(result >= 12 && result < 18) {
				value = "N";
			} else if(result >= 18) {
				value = "H";
			}
		} else if(exam.equals("HCT")) {
			if(result < 36) {
				value = "L";
			} else if(result >= 36 && result < 45) {
				value = "N";
			} else if(result >= 45) {
				value = "H";
			}
		} else if (exam.equals("MCV")){
			if(result < 84) {
				value = "L";
			} else if(result >= 84 && result < 95) {
				value = "N";
			} else if(result >= 95) {
				value = "H";
			}
		}
		return value;
	}

	public void createExamTable() throws IOException {
		FileWriter table = new FileWriter(new File("DimExam.table"));
		BufferedReader reader = new BufferedReader(new FileReader(
				"DimExam.table.txt"));

		table.write("ExamID\tCodeName\tResult\n");
		String entry = reader.readLine();
		entry = reader.readLine();

		String[] line = entry.split("\t");
		String codename = line[3];
		int id = Integer.parseInt(line[0]);
		int date = Integer.parseInt(line[1]);
		double result = Double.parseDouble(line[4]);

		int counter = 1;
		int position = 0;

		while ((entry = reader.readLine()) != null) {
			line = entry.split("\t");

			if (Integer.parseInt(line[0]) != id
					|| Integer.parseInt(line[1]) != date
					|| !line[3].equals(codename)) {
				result = result / counter;
				counter = 1;
				position++;
				table.write(Integer.toString(position) + "\t" + codename + "\t"
						+ codename + "_" + categorizeExamResult(codename, result) + "\n");
				try {
					result = Double.parseDouble(line[4]);
				} catch (Exception e) {
				}
			} else if (Integer.parseInt(line[1]) == date)
				if (line[3].equals(codename)) {
					try {
						result = result + Double.parseDouble(line[4]);
					} catch (Exception e) {
					}
					counter++;
				} else {
					result = result / counter;
					counter = 1;
					position++;
					table.write(Integer.toString(position) + "\t" + codename
							+ "\t" + codename + "_" + categorizeExamResult(codename, result) + "\n");

					try {
						result = Double.parseDouble(line[4]);
					} catch (Exception e) {
						System.err.println("Muahahah");
					}
				}

			id = Integer.parseInt(line[0]);
			date = Integer.parseInt(line[1]);
			codename = line[3];
		}
		reader.close();
		table.close();
	}

	public void addPrimaryKey() throws IOException {
		FileWriter table = new FileWriter(new File("FactHepatitis.table.new"));
		BufferedReader reader = new BufferedReader(new FileReader(
				"FactHepatitis.table"));

		int id = 1;
		String line;

		table.write("TransactionID\t" + reader.readLine() + "\n");

		while((line = reader.readLine()) != null) {
			table.write(id++ + "\t" + line + "\n");
		}

		table.close();
		reader.close();
	}

	public static void main(String[] args) throws IOException {
		System.out.println("Generate tables: starting");
		TableGenerators generator = new TableGenerators();

		//		System.out.println("Create DimDateTable: starting");
		//		generator.createDateTable();
		//		System.out.println("Create DimDateTable: done");
		//
		//		System.out.println("Create DimBiopsyTable: starting");
		//		generator.createBiopsyTable();
		//		System.out.println("Create DimBiopsyTable: done");
		//
		//		System.out.println("Create DimExamTable: starting");
		//		generator.createExamTable();
		//		generator.createExameTable();
		//		System.out.println("Create DimExamTable: done");

		generator.addPrimaryKey();
		//
		System.out.println("Generate tables: done");
	}

}

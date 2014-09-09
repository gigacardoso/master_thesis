package tdm.classification.createData.Hepatitis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TableGeneratorValues {
	
	private String data = "C:\\hepat_data030704\\";
	@SuppressWarnings("unused")
	private String andreia = data + "andreia\\";

	public void cleanExamTable() throws IOException {
		FileWriter table = new FileWriter(new File("DimExam.table.2nd"));
		BufferedReader reader = new BufferedReader(new FileReader(data+
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
	
	public static void main(String[] args) {
		TableGeneratorValues table = new TableGeneratorValues();
		try {
			table.cleanExamTable();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}

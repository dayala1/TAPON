package main;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class CSVDriver {

	public static void main(String[] args) {
		FileWriter fileWriter;
		CSVPrinter csvFilePrinter;
		List<String> record;
		String lineSeparator;
		CSVFormat csvFormat;
		
		record = new ArrayList<String>();
		record.add("TestH1");
		record.add("TestH2");
		record.add("TestH3");
		
		lineSeparator = "\n";
		csvFormat = CSVFormat.DEFAULT.withRecordSeparator(lineSeparator);
		
		fileWriter = null;
		csvFilePrinter = null;
		
		try {
			fileWriter = new FileWriter("test.csv");
			csvFilePrinter = new CSVPrinter(fileWriter, csvFormat);
			csvFilePrinter.printRecord(record);
			record.clear();
			record.add("This is");
			record.add("a simple");
			record.add("test");
			csvFilePrinter.printRecord(record);

		} catch (Exception e) {
		} finally {
			try {
				csvFilePrinter.close();
				fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	//Constructors---------------------------------------------------

	//Properties-----------------------------------------------------

	//Internal state-------------------------------------------------

	//Interface methods----------------------------------------------

	//Ancillary methods----------------------------------------------
}

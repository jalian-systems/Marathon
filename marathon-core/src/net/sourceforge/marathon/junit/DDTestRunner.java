/*******************************************************************************
 *  
 *  Copyright (C) 2010 Jalian Systems Private Ltd.
 *  Copyright (C) 2010 Contributors to Marathon OSS Project
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Library General Public License for more details.
 * 
 *  You should have received a copy of the GNU Library General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Project website: http://www.marathontesting.com
 *  Help: Marathon help forum @ http://groups.google.com/group/marathon-testing
 * 
 *******************************************************************************/
package net.sourceforge.marathon.junit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.IConsole;
import au.com.bytecode.opencsv.CSVReader;

public class DDTestRunner {

	Pattern pattern = Pattern
			.compile(".*use_data_file\\s*\\(\\s*\"([^\"]*)\".*$|.*use_data_file\\s*\\(\\s*'([^\']*)'.*$");

	private final String scriptText;
	private final IConsole console;
	private int nTests = 1;

	private String fileName = null;

	private List<String[]> data;

	private String[] header;

	private int currentIndex;

	private String[] currentData;

	int runIndex = 0;

	public DDTestRunner(IConsole console, String scriptText) throws IOException {
		this.console = console;
		this.scriptText = scriptText;
		processForDataFile(scriptText);
	}

	public DDTestRunner(IConsole console, File file) throws IOException {
		this.console = console;
		this.scriptText = getScript(file);
		processForDataFile(scriptText);
	}

	private String getScript(File file) throws IOException {
		int size = (int) file.length();
		char[] cs = new char[size + 64];
		FileReader fileReader = new FileReader(file);
		int n = fileReader.read(cs);
		fileReader.close();
		return new String(cs, 0, n);
	}

	private void processForDataFile(String scriptText) throws IOException {
		BufferedReader br = new BufferedReader(new StringReader(scriptText));
		String line;
		while ((line = br.readLine()) != null) {
			Matcher matcher = pattern.matcher(line);
			if (matcher.matches()) {
				fileName = matcher.group(2);
				readCSVData();
				return;
			}
		}
	}

	private void readCSVData() throws IOException {
		File dataFile = new File(fileName);
		if (!dataFile.exists()) {
			File dataDir = new File(
					System.getProperty(Constants.PROP_PROJECT_DIR), "TestData");
			dataFile = new File(dataDir, fileName);
		}
		CSVReader reader = null;
		try {
			reader = new CSVReader(new FileReader(dataFile));
			data = reader.readAll();
			if (data == null || data.size() < 2) {
				throw new IllegalArgumentException("No data in CSV file?");
			}
			header = data.get(0);
			currentIndex = 1;
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	public String getScriptText() {
		return scriptText;
	}

	public IConsole getConsole() {
		return console;
	}

	public boolean hasNext() {
		if (fileName == null)
			return nTests-- > 0;
		return csvHasNext();
	}

	private boolean csvHasNext() {
		while (currentIndex < data.size()) {
			String[] datum = data.get(currentIndex);
			if (datum.length > 1 || (datum.length == 1 && !"".equals(datum[0])))
				break;
			currentIndex++;
		}
		return currentIndex < data.size();
	}

	public void next() {
		if (fileName != null) {
			currentData = data.get(currentIndex);
			currentIndex++;
		}
	}

	public Properties getDataVariables() {
		Properties props = new Properties();
		if (fileName == null)
			return props;
		for (int i = 0; i < Math.min(currentData.length, header.length); i++) {
			props.put(header[i], makeString(currentData[i]));
		}
		return props;
	}

	private String makeString(String string) {
		if (string.startsWith("\"") && string.endsWith("\""))
			return string;
		if (string.startsWith("'") && string.endsWith("'"))
			return string;
		if (isNumber(string))
			return string;
		return "\"" + string + "\"";
	}

	private boolean isNumber(String string) {
		try {
			Integer.parseInt(string);
			return true;
		} catch (NumberFormatException e) {
			try {
				Float.parseFloat(string);
				return true;
			} catch (NumberFormatException e1) {
				try {
					Double.parseDouble(string);
					return true;
				} catch (NumberFormatException e2) {
				}
			}
		}
		return false;
	}

	public boolean isDDT() {
		return fileName != null;
	}

	public String getName() {
		if (!isDDT())
			return "";
		if (header[0].equals("marathon_test_name"))
			return " - " + currentData[0];
		if (runIndex == 0) {
			runIndex = 2;
			return "";
		}
		return " - " + runIndex++;
	}
}

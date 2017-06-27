package utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import org.apache.commons.lang.StringUtils;

public class Chronometer {
	public static long getThreadTime() {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		return bean.getCurrentThreadCpuTime();
	}
	
	public static long getThreadTime(String process) {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		return bean.getThreadCpuTime(findThread(process));
	}
	
	private static long findThread(String process) {
		long ret = -1;
		try {
			Runtime runtime = Runtime.getRuntime();
			String cmds[] = {"cmd", "/c", "tasklist"};
			Process proc = runtime.exec(cmds);
			InputStream inputstream = proc.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
			
			String processLine = null;
			String line;
			while ((line = bufferedreader.readLine()) != null && processLine == null)
			    if (line.startsWith(process))
			    	processLine = line;
			bufferedreader.close();
			inputstreamreader.close();
			inputstream.close();
			
			processLine = StringUtils.remove(processLine, process);
			processLine = processLine.trim();
			
			StringBuffer retStr = new StringBuffer();
			for (int i = 0; i <= processLine.length() && processLine.charAt(i) != ' '; i++)
				retStr.append(processLine.charAt(i));
			
			ret = Long.valueOf(retStr.toString());
		} catch (Exception oops) {
			oops.printStackTrace();
		}
		
		return ret;
	}
}

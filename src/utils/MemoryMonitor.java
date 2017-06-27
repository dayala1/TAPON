/**
 * ${file_name}
 * 
 * Copyright (C) 2015-16 Fundación FIDETIA
 * Authors (A) 2015-16 Rafael Corchuelo and Patricia Jiménez
 * 
 */

package utils;

public class MemoryMonitor {

	// Internal state ---------------------------------------------------------

	private static Runtime runtime;

	static {
		MemoryMonitor.runtime = Runtime.getRuntime();
	}

	// Properties -------------------------------------------------------------

	public static long getTotalMemory() {
		return MemoryMonitor.runtime.totalMemory();
	}

	public static long getUsedMemory() {
		return MemoryMonitor.runtime.totalMemory() - MemoryMonitor.runtime.freeMemory();
	}

	public static long getFreeMemory() {
		return MemoryMonitor.runtime.freeMemory();
	}

	public static long getMaxMemory() {
		return MemoryMonitor.runtime.maxMemory();
	}

	// Business methods -------------------------------------------------------

	public static void collect() {
		try {
			Runtime.getRuntime().gc();
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
			;
		}
	}

}

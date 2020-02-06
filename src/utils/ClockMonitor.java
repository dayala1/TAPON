/**
 * ${file_name}
 * 
 * Copyright (C) 2015-16 Fundación FIDETIA
 * Authors (A) 2015-16 Rafael Corchuelo and Patricia Jiménez
 * 
 */

package utils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import org.joda.time.DateTime;

public class ClockMonitor {

	// Internal state ---------------------------------------------------------

	private static final ThreadMXBean mxBean;

	static {
		mxBean = ManagementFactory.getThreadMXBean();
	}

	// Business methods -------------------------------------------------------

	private DateTime startMoment, endMoment;
	private long startWallTime, endWallTime;
	private long beginCPUTime, endCPUTime;
	private long accumulatedCPUTime, accumulatedWallTime;

	public void start() {
		resume();
		this.accumulatedWallTime = 0;
		this.accumulatedCPUTime = 0;
	}

	public void pause() {
		this.endMoment = DateTime.now();
		this.endWallTime = System.nanoTime();
		this.endCPUTime = ClockMonitor.mxBean.getCurrentThreadCpuTime();
		this.accumulatedCPUTime += this.endCPUTime - this.beginCPUTime;
		this.accumulatedWallTime += this.endWallTime - this.startWallTime;
		this.startMoment = DateTime.now();
		this.startWallTime = System.nanoTime();
		this.beginCPUTime = ClockMonitor.mxBean.getCurrentThreadCpuTime();
	}

	public void resume() {
		this.startMoment = DateTime.now();
		this.startWallTime = System.nanoTime();
		this.endWallTime = 0;
		this.beginCPUTime = ClockMonitor.mxBean.getCurrentThreadCpuTime();
		this.endCPUTime = 0;
	}

	public void stop() {
		this.endMoment = DateTime.now();
		this.endWallTime = System.nanoTime();
		this.endCPUTime = ClockMonitor.mxBean.getCurrentThreadCpuTime();
	}
	
	public DateTime getStartMoment() {
		return this.startMoment;
	}
	
	public DateTime getEndMoment() {
		return this.endMoment;
	}
	public long getWallTime() {
		long result;

		result = this.endWallTime - this.startWallTime + this.accumulatedWallTime;

		return result;
	}
	
	public long getCPUTime() {
		long result;

		result = this.endCPUTime - this.beginCPUTime + this.accumulatedCPUTime;

		return result;
	}
	
}

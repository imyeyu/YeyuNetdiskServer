package net.imyeyu.netdisk.server.bean;

public class ServerStatus {

	private double cpuUse;
	private long memUse;
	private long memMax;
	private long diskUse;
	private long diskMax;
	
	public ServerStatus() {
		this.cpuUse = -1;
		this.memUse = -1;
		this.memMax = -1;
		this.diskUse = -1;
		this.diskMax = -1;
	}
	
	public double getCpuUse() {
		return cpuUse;
	}

	public void setCpuUse(double cpuUse) {
		this.cpuUse = cpuUse;
	}

	public long getMemUse() {
		return memUse;
	}

	public void setMemUse(long memUse) {
		this.memUse = memUse;
	}

	public long getMemMax() {
		return memMax;
	}

	public void setMemMax(long memMax) {
		this.memMax = memMax;
	}

	public long getDiskUse() {
		return diskUse;
	}

	public void setDiskUse(long diskUse) {
		this.diskUse = diskUse * 1000;
	}

	public long getDiskMax() {
		return diskMax;
	}

	public void setDiskMax(long diskMax) {
		this.diskMax = diskMax * 1000;
	}
}
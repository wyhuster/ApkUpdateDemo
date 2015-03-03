package com.gtja.tonywang.yyzupdate.model;

public class UpdateModel {
	private String version;
	private String loadAddress;
	private String forceLowVersion;
	private String noticeText;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getLoadAddress() {
		return loadAddress;
	}

	public void setLoadAddress(String loadAddress) {
		this.loadAddress = loadAddress;
	}

	public String getForceLowVersion() {
		return forceLowVersion;
	}

	public void setForceLowVersion(String forceLowVersion) {
		this.forceLowVersion = forceLowVersion;
	}

	public String getNoticeText() {
		return noticeText;
	}

	public void setNoticeText(String noticeText) {
		this.noticeText = noticeText;
	}
}

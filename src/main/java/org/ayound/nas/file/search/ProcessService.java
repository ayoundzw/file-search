package org.ayound.nas.file.search;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ProcessService {

	// 私有静态实例，防止被引用，此处赋值为null，目的是实现延迟加载
	private static ProcessService instance = null;

	// 当前处理文件的索引
	private long currentCount;
	// 总文件数量
	private long totalCount;
	// 当前正在处理的文件名
	private String message;
	// 开始时间
	private Date startTime;
	// 结束时间
	private Date endTime;
	// 运行状态
	private String status;
	// 是否结束
	private boolean isFinished;

	private String sourceFilePaths;

	private String IndexFilePath;

	// 私有构造方法，防止被实例化
	private ProcessService() {
		this.currentCount = 0;
		this.totalCount = 0;
		this.message = null;
		this.startTime = null;
		this.endTime = null;
		this.status = "Stopped";
		this.isFinished = true;
		this.sourceFilePaths = null;
		this.IndexFilePath = null;
	}

	// 静态工程方法，创建实例
	public static synchronized ProcessService getInstance() {
		if (instance == null) {
			instance = new ProcessService();
		}
		return instance;
	}

	// 开始索引
	public void startIndexing() {
		this.startTime = new Date();
		this.isFinished = false;
		this.message = "开始处理";
		this.status = "Running";
	}

	// 更新当前数量
	public void updateCurrentCount(long count) {
		this.currentCount = count;
	}

	// 结束索引
	public void endIndexing() {
		this.endTime = new Date();
		this.isFinished = true;
		this.message = "处理结束";
		this.status = "Stopped";
	}

	// 获取属性
	public long getCurrentCount() {
		return currentCount;
	}

	public long getTotalCount() {
		return totalCount;
	}

	public Date getStartTime() {
		return startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public String getStatus() {
		return status;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void setCurrentCount(long currentCount) {
		this.currentCount = currentCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	

	public String getSourceFilePaths() {
		return sourceFilePaths;
	}

	public void setSourceFilePaths(String sourceFilePaths) {
		this.sourceFilePaths = sourceFilePaths;
	}

	public String getIndexFilePath() {
		return IndexFilePath;
	}

	public void setIndexFilePath(String indexFilePath) {
		IndexFilePath = indexFilePath;
	}

	// 格式化日期
	private String formatDate(Date date) {
		if (date == null) {
			return "N/A";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(date);
	}

}
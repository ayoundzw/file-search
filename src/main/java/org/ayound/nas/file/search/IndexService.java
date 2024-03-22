package org.ayound.nas.file.search;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.springframework.stereotype.Component;

@Component
public class IndexService {
	// 索引服务实例
	private static IndexService instance;
	Directory indexDirectory;

	IndexWriter indexWriter;

	Analyzer analyzer;

	// 私有构造函数，防止外部实例化
	private IndexService() throws IOException {
		analyzer = new SmartChineseAnalyzer();
		Path indexPath = Paths.get(Configuration.getIndexFilePath());
		indexDirectory = new NIOFSDirectory(indexPath);
		
	}

	// 获取索引服务单例对象
	public static IndexService getInstance() throws IOException {
		if (instance == null) {
			instance = new IndexService();
		}
		return instance;
	}

	public void update() throws IOException {
		Path indexPath = Paths.get(Configuration.getIndexFilePath());
		indexDirectory = new NIOFSDirectory(indexPath);
		analyzer = new SmartChineseAnalyzer();
	}

	public Directory getIndexDirectory() throws IOException {
		Path indexPath = Paths.get(Configuration.getIndexFilePath());
		return new NIOFSDirectory(indexPath);
	}

	public Analyzer getAnalyzer() {
		return analyzer;
	}

	public IndexWriter newIndexWriter() throws IOException {
		IndexWriterConfig indexerConfig = new IndexWriterConfig(analyzer);
		indexerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		return new IndexWriter(indexDirectory, indexerConfig);
	}

}
package cn.keking.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.ayound.nas.file.search.ProcessService;
import org.ayound.nas.file.search.Searcher;
import org.ayound.nas.file.search.StartScanThread;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.keking.config.ConfigConstants;

@RestController
@RequestMapping("/api")
public class NasFileSearchController {

	@RequestMapping("/start")
	String start() {
		StartScanThread thread = new StartScanThread();
		thread.start();
		return "ok";
	}

	@RequestMapping("/end")
	String end() {
		ProcessService.getInstance().endIndexing();
		return "ok";
	}

	@RequestMapping("/status")
	String status() {
		ProcessService.getInstance().setIndexFilePath(ConfigConstants.getIndexFilePath());
		ProcessService.getInstance().setSourceFilePaths(ConfigConstants.getSourceFilePaths());
		JSONObject obj = new JSONObject(ProcessService.getInstance());
		return obj.toString();
	}

	@RequestMapping("/get/{id}")
	String get(@PathVariable(value = "id", required = true) Integer id) throws IOException {
		Searcher searcher = Searcher.getInstance();
		String ret = searcher.get(id);
		return ret;
	}

	@RequestMapping("/download/{id}/**")
	void download(@PathVariable(value = "id", required = true) Integer id, HttpServletRequest req,
			HttpServletResponse response) throws IOException {
		boolean validate = false;
		String downloadHeader = req.getHeader(ConfigConstants.DOWNLOAD_HEADER);
		if (downloadHeader != null) {
			if (StringUtils.equals(downloadHeader, ConfigConstants.getDownloadKey())) {
				validate = true;
			}
		} else {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth.getName() != null && !StringUtils.equals("anonymousUser", auth.getName())) {
				validate = true;
			}
		}

		if (!validate) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		Searcher searcher = Searcher.getInstance();
		Document doc = searcher.getPath(id);
		if (doc == null) {
			response.sendError(403);
			return;
		}
		String path = doc.get("path");
		String contentType = doc.get("type");
		InputStream inputStream = new FileInputStream(path);// 文件的存放路径
		response.reset();
		response.setContentType(contentType);
		String filename = new File(path).getName();
		response.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
		ServletOutputStream outputStream = response.getOutputStream();
		byte[] b = new byte[1024];
		int len;
		// 从输入流中读取一定数量的字节，并将其存储在缓冲区字节数组中，读到末尾返回-1
		while ((len = inputStream.read(b)) > 0) {
			outputStream.write(b, 0, len);
		}
		inputStream.close();
	}

	@RequestMapping("/search")
	String search(@RequestParam(value = "type", required = false, defaultValue = "f") String type,
			@RequestParam(value = "text", required = false, defaultValue = "") String text,
			@RequestParam(value = "page", defaultValue = "10", required = false) int page,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
			@RequestParam(value = "sortField", required = false, defaultValue = "") String sortField,
			@RequestParam(value = "direct", required = false, defaultValue = "false") boolean direct)
			throws IOException {
		Searcher searcher = Searcher.getInstance();
		JSONObject ret = searcher.search(type, text, page, pageSize, sortField, direct);
		return ret.toString();
	}

	public static void main(String[] args) {
		SpringApplication.run(NasFileSearchController.class, args);
	}

}

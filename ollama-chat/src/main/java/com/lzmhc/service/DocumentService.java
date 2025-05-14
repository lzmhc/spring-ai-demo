package com.lzmhc.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ArrayUtil;
import com.lzmhc.mapper.mysql.mapper.DocumentMapper;
import com.lzmhc.reader.ParagraphTextReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static com.lzmhc.reader.ParagraphTextReader.END_PARAGRAPH_NUMBER;
import static com.lzmhc.reader.ParagraphTextReader.START_PARAGRAPH_NUMBER;

/**
 * @Description: 文档服务
 * @Author: jay
 * @Date: 2024/3/18 10:02
 * @Version: 1.0
 */
@Service
@Slf4j
public class DocumentService {
	@Autowired
	private VectorStore vectorStore;
	@Autowired
	private OllamaChatModel chatClient;
	private TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
	@Autowired
	private DocumentMapper documentMapper;
	@Autowired
	private MinioService minioService;


	/**
	 * 使用spring ai解析txt文档
	 *
	 * @param file
	 * @throws MalformedURLException
	 */
	public List<Document> paragraphTextReader(File file, String fileName) {
		List<Document> docs = null;
		try {
			ParagraphTextReader reader = new ParagraphTextReader(new FileUrlResource(file.toURI().toURL()), 5);
			reader.getCustomMetadata().put("filename", file.getName());
			reader.getCustomMetadata().put("filepath", file.getAbsolutePath());
			docs = reader.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		vectorStore.add(docs);
		documentMapper.updateEmbedding(fileName);
		return docs;
	}

	/**
	 * 合并文档列表
	 *
	 * @param documentList 文档列表
	 * @return 合并后的文档列表
	 */
//	private List<Document> mergeDocuments(List<Document> documentList) {
//		List<Document> mergeDocuments = new ArrayList();
//		//根据文档来源进行分组
//		Map<String, List<Document>> documentMap = documentList.stream().collect(Collectors.groupingBy(item -> ((String) item.getMetadata().get("source"))));
//		for (Map.Entry<String, List<Document>> docListEntry : documentMap.entrySet()) {
//			//获取最大的段落结束编码
//			int maxParagraphNum = (int) docListEntry.getValue()
//					.stream().max(Comparator.comparing(item -> ((int) item.getMetadata().get(END_PARAGRAPH_NUMBER)))).get().getMetadata().get(END_PARAGRAPH_NUMBER);
//			//根据最大段落结束编码构建一个用于合并段落的空数组
//			String[] paragraphs = new String[maxParagraphNum];
//			//用于获取最小段落开始编码
//			int minParagraphNum = maxParagraphNum;
//			for (Document document : docListEntry.getValue()) {
//				//文档内容根据回车进行分段
//				String[] tempPs = document.getContent().split("\n");
//				//获取文档开始段落编码
//				int startParagraphNumber = (int) document.getMetadata().get(START_PARAGRAPH_NUMBER);
//				if (minParagraphNum > startParagraphNumber) {
//					minParagraphNum = startParagraphNumber;
//				}
//				//将文档段落列表拷贝到合并段落数组中
//				System.arraycopy(tempPs, 0, paragraphs, startParagraphNumber - 1, tempPs.length);
//			}
//			//合并段落去除空值,并组成文档内容
//			Document mergeDoc = new Document(ArrayUtil.join(ArrayUtil.removeNull(paragraphs), "\n"));
//			//合并元数据
//			mergeDoc.getMetadata().putAll(docListEntry.getValue().get(0).getMetadata());
//			//设置元数据:开始段落编码
//			mergeDoc.getMetadata().put(START_PARAGRAPH_NUMBER, minParagraphNum);
//			//设置元数据:结束段落编码
//			mergeDoc.getMetadata().put(END_PARAGRAPH_NUMBER, maxParagraphNum);
//			mergeDocuments.add(mergeDoc);
//		}
//		return mergeDocuments;
//	}

	/**
	 * 根据关键词搜索向量库
	 *
	 * @param keyword 关键词
	 * @return 文档列表
	 */
//	public List<Document> search(String keyword) {
//		return mergeDocuments(vectorStore.similaritySearch(keyword));
//	}

	/**
	 * 问答,根据输入内容回答
	 *
	 * @param message 输入内容
	 * @return 回答内容
	 */
//	public String chat(String message) {
//		//查询获取文档信息
//		List<Document> documents = search(message);
//
//		//提取文本内容
//		String content = documents.stream()
//				.map(Document::getContent)
//				.collect(Collectors.joining("\n"));
//
//		//封装prompt并调用大模型
//		String chatResponse = chatClient.call(getChatPrompt2String(message, content));
//		return chatResponse;
//	}

	/**
	 * 获取prompt
	 *
	 * @param message 提问内容
	 * @param context 上下文
	 * @return prompt
	 */
	private String getChatPrompt2String(String message, String context) {
		String promptText = """
				请用仅用以下内容回答"%s":
				%s
				""";
		return String.format(promptText, message, context);
	}
	// DocumentService.java
//	public Flux<String> streamChat(String query) {
////		return Flux.fromIterable(splitToChunks(processQuery(query)))
////				.delayElements(Duration.ofMillis(50));
//		return Mono.fromCallable(() -> processQuery(query))
//				.subscribeOn(Schedulers.boundedElastic()) // 使用弹性线程池
//				.flatMapMany(content ->
//						Flux.fromIterable(splitToChunks(content))
//								.delayElements(Duration.ofMillis(30))
//								.onErrorResume(e -> {
//									log.error("Stream chat failed", e);
//									return Flux.just("服务暂时不可用，请稍后重试");
//								}));
//	}

//	private String processQuery(String query) {
//		String res = chat(query);
//		return res;
//	}

	private List<String> splitToChunks(String content) {
		// 将内容分割成字符数组或段落
		return Arrays.asList(content.split("(?<=\\G.{50})")); // 每50字符分块
	}

	/**
	 * 向量化文档
	 * @param file
	 * @return
	 */
	public Mono<Void> documentStore(String file){
		Resource resource = null;
		try {
			InputStream inputStream = minioService.getFile(file);
			resource = new ByteArrayResource(inputStream.readAllBytes());
		}catch (IOException e){
			throw new RuntimeException(e.getMessage());
		}
		try {
			int documentId = documentMapper.getDocumentId(file);
			TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);
			List<Document> rawDocumentList = tikaDocumentReader.read();
			List<Document> splitDocumentList = tokenTextSplitter.split(rawDocumentList);
			List<Document> hasMetaDocumentList = splitDocumentList.stream().map(item ->{
			Map<String, Object> metadata = item.getMetadata();
			metadata.put("open_id", StpUtil.getLoginIdAsString());
			metadata.put("document_id", documentId);
			return new Document(item.getText(), metadata);
		}).toList();
			vectorStore.accept(hasMetaDocumentList);
			documentMapper.updateEmbedding(file);
		}catch(Exception e){
			throw  new RuntimeException(e.getMessage());
		}
		return Mono.empty();
	}

	/**
	 * 获取检索知识库文档
	 */
	public Flux<com.lzmhc.mapper.mysql.entity.Document> getKnowledgeList(){
		return Flux.fromIterable(documentMapper.getKnowledgeList(StpUtil.getLoginIdAsString()))
				.onErrorResume(e -> {
					// 捕获异常并返回错误信号
					return Flux.error(new RuntimeException("获取知识列表失败: " + e.getMessage(), e));
				});
	}
	public List<com.lzmhc.mapper.mysql.entity.Document> getKnowledges(){
		return documentMapper.getDocumentList(StpUtil.getLoginIdAsString());
	}
	/**
	 * 获取文件列表
	 */
	public Flux<com.lzmhc.mapper.mysql.entity.Document> getFiles(){
		List<com.lzmhc.mapper.mysql.entity.Document> fileList = documentMapper.getFileList();
		return Flux.fromIterable(fileList);
	}
}

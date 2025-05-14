package com.lzmhc.mcpserver;

import cn.dev33.satoken.stp.StpUtil;
import com.lzmhc.mapper.mysql.entity.Document;
import com.lzmhc.mapper.mysql.mapper.DocumentMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class KnowledgeService {
    private final DocumentMapper documentMapper;
    public KnowledgeService(DocumentMapper documentMapper){
        this.documentMapper = documentMapper;
    }
    @Tool(description = "查询我的知识库")
    public String getKnowledgeList(){
        List<Document> documentList = documentMapper.getDocumentList(StpUtil.getLoginIdAsString());
        if (documentList.isEmpty()) {
            return "你的知识库为空";
        }

        String result = documentList.stream()
                .map(doc -> "- " + doc.getFileName()) // 每个文档一行
                .collect(Collectors.joining("\n"));
        return "你的知识库列表:\n" + result;
    }
}

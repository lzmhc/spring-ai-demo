package com.lzmhc.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatRequest {
    private String model;
    private Messages[] messages;
    private boolean stream;
    /*
     是否启用知识库
     */
    private boolean flag;
    /**
     * 知识库ID
     */
    private List<String> documentId;

    /**
     * 资源ID
     * @return
     */
    private String resourceId;
    public Messages[] getMessages(){
        return messages;
    }
}

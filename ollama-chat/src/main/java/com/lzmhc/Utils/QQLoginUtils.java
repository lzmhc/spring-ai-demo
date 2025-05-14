package com.lzmhc.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class QQLoginUtils {
    @Value("${sa-token.oauth2.qq.client-id}")
    private String appId;
    @Value("${sa-token.oauth2.qq.client-secret}")
    private String appKey;
    @Value("${sa-token.oauth2.qq.redirect-uri}")
    private String redirectUri;
    private ObjectMapper mapper;
    /**
     * 发送请求
     */
    public String httpClient(String url, MultiValueMap<String, String> params){
        RestTemplate client = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        client.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(params, headers);
        ResponseEntity<String> response = client.exchange(url, HttpMethod.POST, requestEntity, String.class);
        return response.getBody();
    }
    /**
     * 获取accesstoken
     */
    public Map getAccessToken(String code){
        String url = "https://graph.qq.com/oauth2.0/token";
        MultiValueMap params = new LinkedMultiValueMap();
        params.add("grant_type", "authorization_code");
        params.add("client_id", appId);
        params.add("client_secret", appKey);
        params.add("code", code);
        params.add("redirect_uri", redirectUri);
        params.add("fmt", "json");
        String json = httpClient(url, params);
        log.info("response={}", json);
        if(mapper == null){
            mapper=new ObjectMapper();
        }
        Map<String, Object> tmpMap = null;
        try{
            tmpMap = mapper.readValue(json, Map.class);
        }catch (Exception e){
            log.info("Error!");
        }
        return tmpMap;
    }
    /**
     * 刷新accesstoken
     */
    public Map getNewAccessToken(String refresh_token){
        String url = "https://graph.qq.com/oauth2.0/token";
        MultiValueMap params = new LinkedMultiValueMap();
        params.add("grant_type", "refresh_token");
        params.add("client_id", appId);
        params.add("client_secret", appKey);
        params.add("refresh_token", refresh_token);
        String json = httpClient(url, params);
        if (mapper==null){
            mapper=new ObjectMapper();
        }
        Map<String , Object> tmpMap = null;
        try{
            tmpMap = mapper.readValue(json, Map.class);
        }catch (Exception e){
            log.info("Error!");
        }
        return tmpMap;
    }
    /**
     * 获取OpenId
     */
    public Map getOpenId(String access_token){
        String url = "https://graph.qq.com/oauth2.0/me";
        MultiValueMap params = new LinkedMultiValueMap();
        params.add("access_token", access_token);
        params.add("fmt", "json");
        String json=httpClient(url, params);
        log.info("response={}", json);
        if(mapper==null){
            mapper=new ObjectMapper();
        }
        Map<String , Object> tmpMap=null;
        try {
            tmpMap = mapper.readValue(json, Map.class);
        }catch (Exception e){
            log.info("Error!");
        }
        log.info("tmpMap={}", tmpMap);
        return tmpMap;
    }
    /**
     * 获取用户信息
     */
    public Map getUserInfo(String access_token, String openid){
        log.info("AccessToken={}", access_token);
        log.info("OpenId={}", openid);
        MultiValueMap params = new LinkedMultiValueMap();
        params.add("access_token", access_token);
        params.add("oauth_consumer_key", appId);
        params.add("openid", openid);
        String url = "https://graph.qq.com/user/get_user_info";
        String json = httpClient(url, params);
        Map<String, Object> tmpMap = null;
        if(mapper==null){
            mapper=new ObjectMapper();
        }
        try {
            tmpMap = mapper.readValue(json, Map.class);
        }catch(Exception e){
            log.info("Error");
        }
        log.info("UserInfo={}",tmpMap);
        return tmpMap;
    }
    //直接一次获取到个人信息
    public Map getUserInfoPlus(String code){
        try{
            String acode = getAccessToken(code).get("access_token").toString();
            String openid = getOpenId(acode).get("openid").toString();
            return getUserInfo(acode,openid);
        }catch (Exception ex){
            System.out.println("出问题了");
            return new HashMap();
        }
    }
}

package com.bazinga.util;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.Map;

@Slf4j
public class HttpUtil {
    private static final ObjectMapper mapper = new ObjectMapper();
    /**
     *  发送get请求
     * @param url
     * @param params 无参数传递null
     * @return
     */
    public  static String sendHttpGet(String url, Map<String,String> params) throws Exception {
        HttpRequest request = HttpRequest.get(url);
        if(!CollectionUtils.isEmpty(params)){
            request.query(params);
        }
        HttpResponse response = request.send();
        String bodyText = response.bodyText();
        bodyText = transFormToUtf8(bodyText);
        return bodyText;
    }

    /**
     *
     * @param url
     * @param params 发送json格式的参数  无参传递null
     * @return
     */
    public static String sendHttpPostJosn(String url, Map<String,Object> params){
        HttpRequest request = HttpRequest.post(url);
        request.contentType("application/json");
        request.charset("utf-8");
        if(!CollectionUtils.isEmpty(params)){
            try {
                request.body(mapper.writeValueAsString(params));
            } catch (JsonProcessingException e) {
                log.error("write paramter [" + params.toString() + "] to josn fail",e.getMessage(),e);
            }
        }
        HttpResponse response = request.send();
        return response.bodyText();
    }

    /**
     *
     * @param url
     * @param params 发送文本参数 无参传递null
     * @return
     */
    public static String sendHttpPost(String url, Map<String,Object> params){
        HttpRequest request = HttpRequest.post(url);
        if(!CollectionUtils.isEmpty(params)) {
            request.form(params);
        }
        HttpResponse response = request.send();
        return response.bodyText();
    }


    /**
     * 字符串转换为utf-8输出
     * @param source
     * @return
     */
    private static String transFormToUtf8(String source) throws Exception{
        return new String(source.getBytes(),"utf-8");
    }

    public static void main(String[] args) {
        String url = "http://120.26.85.183:8080/wave/command/centerResult";
        String result = null;
        try {
            result = HttpUtil.sendHttpGet(url, null);
        } catch (Exception e) {

        }
        JSONObject jsonObject = JSONObject.parseObject(result);
        Integer data = (Integer)jsonObject.get("data");

        System.out.println(result);
    }
}

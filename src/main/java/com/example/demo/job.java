package com.example.demo;


import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * PACKAGE     : com.example.demo
 * CREATE DATE : 2018-05-21
 * AUTHOR      : shouhan
 * 文件描述     :
 */
@Component
public class job {

    //每天早上8：50提醒
    @Scheduled(cron="0 50 8 * * ? ")
    public void work(){
        System.out.println("定时器执行。。。");
        sendRobotMsg("小伙该打卡了...");
        System.out.println("定时器执行成功。。。");
    }

    /**
     * 直接发送钉钉消息
     */
    public static void sendRobotMsg(String msg) {
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("https://oapi.dingtalk.com/robot/send?access_token=14b84c6a4330af0ac3e03ccbe25a79ac21751a70d64e5f854bb7b390538a7a51");
        httppost.addHeader("Content-Type", "application/json; charset=utf-8");
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("msgtype", "text");
        JSONObject jsonParamContent = new JSONObject();
        jsonParamContent.put("content", msg);
        jsonParam.put("text", jsonParamContent);
        String textMsg = jsonParam.toString();
        StringEntity se = new StringEntity(textMsg, "utf-8");
        httppost.setEntity(se);
        HttpResponse response = null;
        try {
            response = httpclient.execute(httppost);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(response.getEntity(), "utf-8");
                System.out.println(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

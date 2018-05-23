package com.sdyk.ai.crawler.specific.clouderwork.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.exception.ChromeDriverException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrawlerAction {

    /**
     * 获取页面数据
     * @param url
     * @return
     * @throws IOException
     * @throws URISyntaxException
     * @throws ChromeDriverException.IllegalStatusException
     */
    public static JsonNode crawlerAction(String url,ChromeDriverAgent agent) throws IOException, URISyntaxException, ChromeDriverException.IllegalStatusException {

        Task task = new Task(url);
        task.setBuildDom();
        agent.submit(task);
        String a = task.getResponse().getDoc().text().replace("/U",",U ");
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true) ;
        JsonNode node = mapper.readTree(a);
        return  node;
    }

    /**
     * 判断是否为最大页
     * @param nextUrl
     * @return
     * @throws ChromeDriverException.IllegalStatusException
     * @throws IOException
     * @throws URISyntaxException
     */
    public static boolean judjeMaxPage(String nextUrl, ChromeDriverAgent agent, ArrayList<String> signs) throws ChromeDriverException.IllegalStatusException, IOException, URISyntaxException {

        boolean judge = true;
        JsonNode nodes = crawlerAction(nextUrl,agent);
        for(String sign : signs){
            nodes = nodes.get(sign);
        }

        if(nodes.size()>0){
            judge = false;
        }
        return  judge;
    }

    /**
     * 获取数字
     * @param content
     * @return
     */
    public static String getNumbers(String content) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            return matcher.group(0);
        }
        return "";
    }

    /**
     * 获取文字
     * @param content
     * @return
     */
    public static String splitNotNumber(String content) {
        Pattern pattern = Pattern.compile("\\D+");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            return matcher.group(0);
        }
        return "";
    }

}

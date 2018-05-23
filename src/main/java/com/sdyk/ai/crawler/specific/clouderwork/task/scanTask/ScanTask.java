package com.sdyk.ai.crawler.specific.clouderwork.task.scanTask;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdyk.ai.crawler.specific.zbj.task.Task;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

public class ScanTask extends Task {
    public ScanTask(String url) throws MalformedURLException, URISyntaxException {
        super(url);
    }

    public ScanTask(String url, String post_data) throws MalformedURLException, URISyntaxException {
        super(url, post_data);
    }

    public ScanTask(String url, String post_data, String cookies, String ref) throws MalformedURLException, URISyntaxException {
        super(url, post_data, cookies, ref);
    }

    public ScanTask(String url, HashMap<String, String> headers, String post_data, String cookies, String ref) throws MalformedURLException, URISyntaxException {
        super(url, headers, post_data, cookies, ref);
    }

    /**
     * 判断当前页是否为最大页
     * @param page
     * @return
     * @throws IOException
     */
    public boolean judjeMaxPage(int page, String sign,String sUrl) throws IOException {
        String[] sUrlSplit = sUrl.split("pagenum=");
        StringBuffer url = new StringBuffer(sUrlSplit[0]);
        url.append("pagenum=");
        int nextPage = page+1;
        url.append(nextPage);
        boolean flag = true;
        String src = getResponse().getDoc().text().replace("/U",",U ");
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        JsonNode node = mapper.readTree(src).get(sign);
        if(node.size()>0){flag = false;}
        return flag;
    }

}

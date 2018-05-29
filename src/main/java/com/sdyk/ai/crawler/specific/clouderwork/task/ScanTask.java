package com.sdyk.ai.crawler.specific.clouderwork.task;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

public class ScanTask extends com.sdyk.ai.crawler.task.ScanTask {

    public ScanTask(String url) throws MalformedURLException, URISyntaxException {
        super(url);
    }

    public boolean judgeMaxPage(int page, String sign, String url){

        boolean maxPageFlag = false;
        return  maxPageFlag;
    }

    /**
     * 判断是否为最大页数
     *
     * @param path
     * @param page
     * @return
     */
    @Override
    public boolean pageTurning(String path, int page) {
        int maxPage;
        if(path.equals("service")){
            maxPage = 3;
        }else{
            maxPage = 201;
        }
        return page<=maxPage;
    }

    /**
     * 获取ScanTask 标识
     *
     * @return
     */
    @Override
    public TaskTrace getTaskTrace() {
        return null;
    }

    @Override
    public one.rewind.io.requester.Task validate() throws ProxyException.Failed, AccountException.Failed, AccountException.Frozen {
        return null;
    }
}

package com.sdyk.ai.crawler.specific.clouderwork.task.scanTask;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.txt.DateFormatUtil;
import one.rewind.util.FileUtil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class ScanTask extends com.sdyk.ai.crawler.task.ScanTask {

    public boolean backtrace = true;

    public ScanTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

    	super(url);

	    this.addDoneCallback((t) -> {
		    FileUtil.appendLineToFile(
				    url + "\t" + DateFormatUtil.dff.print(System.currentTimeMillis()),
				    "scantask.txt");
	    });
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

}

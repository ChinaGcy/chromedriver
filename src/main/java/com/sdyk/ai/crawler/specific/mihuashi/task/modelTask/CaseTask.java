package com.sdyk.ai.crawler.specific.mihuashi.task.modelTask;

import com.sdyk.ai.crawler.specific.zbj.task.Task;
import one.rewind.io.requester.exception.ProxyException;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class CaseTask extends Task {

    public CaseTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {
        super(url);
    }
}

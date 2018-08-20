package com.sdyk.ai.crawler.specific.clouderwork.task.modelTask;


import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.specific.clouderwork.task.Task;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.model.witkey.Project;
import com.sdyk.ai.crawler.util.LocationParser;
import com.sdyk.ai.crawler.util.StringUtil;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskFactory;
import one.rewind.io.requester.task.TaskHolder;
import one.rewind.io.requester.task.ScheduledChromeTask;
import one.rewind.util.FileUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectTask extends Task {

	public static long MIN_INTERVAL = 12 * 60 * 60 * 1000L;

	public static List<String> crons = Arrays.asList("* * */1 * *");

	static {
		registerBuilder(
				ProjectTask.class,
				"https://www.clouderwork.com/jobs/{{project_id}}",
				ImmutableMap.of("project_id", String.class, "flage", String.class),
				ImmutableMap.of("project_id", "", "flage", "1")
		);
	}

    public ProjectTask(String url) throws MalformedURLException, URISyntaxException {

    	super(url);

        // 设置优先级
        this.setPriority(Priority.HIGH);

	    //this.setNoFetchImages();

	    // 判断是否发生异常
	    this.setValidator((a, t) -> {

	    	String src = getResponse().getText();
	    	if( src.contains("帐号登录")
				    && src.contains("登录开启云工作") ){
	    		throw new AccountException.Failed(a.accounts.get(t.getDomain()));
		    }

	    });

	    // 回调处理
        this.addDoneCallback((t) -> {

	        // 获取页面信息
	        Document doc = getResponse().getDoc();
	        String src = getResponse().getText();

	        // 下载页面
	        FileUtil.writeBytesToFile(src.getBytes(), "project.html");

	        // 判断页面是否正确
	        if (src.contains("失败")||src.contains("错误")) {
		        return;
	        }

	        boolean status = crawler(doc);

	        ScheduledChromeTask st = t.getScheduledChromeTask();

	        // 第一次抓取生成定时任务
	        if(st == null) {

		        st = new ScheduledChromeTask(t.getHolder(), crons);
		        st.start();
	        }
	        else {

		        if( !status ){
			        st.degenerate();
		        }
	        }

        });

    }

    public boolean crawler(Document doc) throws Exception {

	    // 解析页面
	    String authorUrl = null;

	    Project project = new Project(getUrl());

	    project.domain_id = Integer.valueOf(2);

	    project.origin_id = getUrl().split("jobs/")[1];

	    //工作地点
	    LocationParser parser = LocationParser.getInstance();
	    String lo = doc.select("#project-detail > div > div.main-top > div.job-main > div.project-info > p.loc").text();
	    if( lo != null && lo.length() > 1 ){
		    project.location = parser.matchLocation(lo).get(0).toString();
	    }


	    //项目描述
	    project.content = StringUtil.cleanContent(doc.select("#project-detail > div > div.main-detail > section > div").html(),
			    new HashSet<>());

	    //项目状态
	    project.status = doc.select("#project-detail > div > div.main-top > div.job-main > h3 > span").text();

	    //项目名称
	    project.title = doc.select("h3.project-title").text()
			    .replaceAll(" ","")
			    .replaceAll(project.status,"");

	    //项目类别
	    project.category = doc.getElementsByClass("scope").text().replace(" >", ",");
	    if( project.category.equals("") ){
		    project.category = "驻场";
	    }

	    // 标签
	    Elements elements = doc.select("span.skill");
		List<String> tags = new ArrayList<>();
	    for(Element element : elements){
		    tags.add(element.text());
	    }
	    if(tags.size() > 0){
		    project.tags = tags;
	    }
	    else {
		    String tags_ = doc.select("div.offer").text();

		    StringBuffer tag = new StringBuffer();

		    Pattern pattern_1 = Pattern.compile(",'(?<t>.+?)'\\)");
		    Matcher matcher_1 = pattern_1.matcher(tags_);

		    if( matcher_1.find() ){
			    tag.append(matcher_1.group("t"));
		    }

		    Pattern pattern_2 = Pattern.compile("getLevel\\((?<t>.+?),'");
		    Matcher matcher_2 = pattern_2.matcher(tags_);

		    String lv = "1";

		    if( matcher_2.find() ){

			    switch (matcher_2.group("t")){
				    case "1" : lv = "初级"; break;
				    case "2" : lv = "中极"; break;
				    case "3" : lv = "高级";
			    }
		    }

		    if( lv.length() > 1 ){
		    	tag.append("-");
		    	tag.append(lv);
		    }

		    // 判断 tag 是否为 “”
		    if( tag.length() > 1 ){
			    project.tags = new ArrayList<>();
			    project.tags.add(tag.toString());
		    }
	    }

	    //项目工期
	    String timeLimit = doc.select("#project-detail > div > div.main-top > div.op-main > div.detail-row > div.budgets.workload > p.budget").text();

	    //项目预算
	    String budget = doc.select("#project-detail > div > div.main-top > div.op-main > div.detail-row > div:nth-child(1) > p.budget").text().replaceAll("￥","");
	    Double budget_lb = Double.valueOf(0);
	    Double budget_up = Double.valueOf(0);

	    //当数据为  人/月
	    if(budget.contains("/")){

		    int multiple = Integer.valueOf(CrawlerAction.getNumbers(timeLimit));


		    //以万元为单位时
		    if(budget.contains("万")){
			    budget = budget.substring(0,budget.length()-5);
			    if(budget.contains(",")){
				    budget = budget.replace(",","");
			    }

			    //捕获String to Double 异常
			    try {
				    budget_lb = Double.valueOf(budget) * 10000 * multiple;
			    } catch (Exception e) {
				    logger.error("error on String"+budget+"To Double", e);
			    }

		    }
		    //不以万元为单位
		    else {
			    budget = budget.substring(0,budget.length()-4);
			    if(budget.contains(",")){
				    budget = budget.replace(",","");
			    }
			    try {
				    budget_lb = Double.valueOf(budget) * multiple;
			    } catch (Exception e) {
				    logger.error("error on String"+budget+"To Double", e);
			    }
		    }
		    budget_up = budget_lb;
	    }
	    //当数据为预算区间时
	    else if (budget.contains("～")){
		    String[] budgetArray = budget.split("～");

		    //最低价以万元为单位
		    if(budgetArray[0].contains("万")){
			    if(budgetArray[0].contains(",")){
				    budgetArray[0] = budgetArray[0].replace(",","");
			    }
			    budgetArray[0] = budgetArray[0].substring(0,budgetArray[0].length()-1);
			    try {
				    budget_lb = Double.valueOf(budgetArray[0])*10000;
			    } catch (Exception e) {
				    logger.error("error on String"+budgetArray[0] +"To Double", e);
			    }
		    }
		    //最低价不以万元为单位
		    else {
			    if(budgetArray[0].contains(",")){
				    budgetArray[0] = budgetArray[0].replace(",","");
			    }
			    try {
				    budget_lb = Double.valueOf(budgetArray[0]);
			    } catch (Exception e) {
				    logger.error("error on String"+budgetArray[0] +"To Double", e);
			    }
		    }

		    //最高价以万元为单位
		    if(budgetArray[1].contains("万")){
			    if(budgetArray[1].contains(",")){
				    budgetArray[1] = budgetArray[1].replace(",","");
			    }
			    budgetArray[1] = budgetArray[1].substring(0,budgetArray[1].length()-1);
			    try {
				    budget_up = Double.valueOf(budgetArray[1])*10000;
			    } catch (Exception e) {
				    logger.error("error on String"+budgetArray[1] +"To Double", e);
			    }
		    }
		    //最高价不以万元为单位
		    else {
			    if(budgetArray[1].contains(",")){
				    budgetArray[1] = budgetArray[1].replace(",","");
			    }
			    try {
				    budget_up = Double.valueOf(budgetArray[1]);
			    } catch (Exception e) {
				    logger.error("error on String"+budgetArray[1] +"To Double", e);
			    }
		    }
	    }
	    //只有整体预算
	    else {
		    //以万元为单位
		    if( budget.contains("万") ) {
			    budget_lb = Double.valueOf(CrawlerAction.getNumbers(budget)) * 10000;
		    }
		    //不以万元为单位
		    else {
			    String s = budget.replace(",","");
			    if( s != null && s.length() > 0 ){
				    budget_lb = Double.valueOf(s);
			    }
		    }
		    budget_up = budget_lb;
	    }
	    project.budget_lb = budget_lb;
	    project.budget_ub = budget_up;

	    // 工期处理，以月为单位
	    if( timeLimit.contains("月") ){
		    timeLimit = CrawlerAction.getNumbers(timeLimit);
		    if( timeLimit!=null ){
			    try {
				    project.time_limit = Integer.valueOf(timeLimit) * 30;
			    } catch (Exception e) {
				    logger.error("error on String"+timeLimit +"To Integer", e);
			    }
		    }
	    }
	    // 以天为单位
	    else {
		    timeLimit = CrawlerAction.getNumbers(timeLimit);
		    if( timeLimit!=null && !"".equals(timeLimit) ){
			    try {
				    project.time_limit = Integer.valueOf(timeLimit);
			    } catch (Exception e) {
				    logger.error("error on String"+timeLimit +"To Integer", e);
			    }
		    }
	    }

	    //招标人名称
	    project.tenderer_name = doc.select("#project-detail > div > div.main-top > div.job-main > div.client > div > a").text();

	    String da = doc.getElementsByClass("time").text().replaceAll("发布于","");
	    DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
	    Date pubdate= null;
	    try {
		    pubdate = format1.parse(da);
	    } catch (ParseException e) {
		    logger.info("error on String"+ da +"to Data url : " + getUrl(), e);
	    }

	    //项目发布时间
	    project.pubdate = pubdate;

	    // 查看人数
	    String bidder = doc.select("#project-detail > div > div.main-top > div.op-main > div.row > span").text();
	    String bidderNum = CrawlerAction.getNumbers(bidder);
	    if( bidder.contains("投标") ){
		    try {
			    project.bids_num = Integer.valueOf(bidderNum);
		    } catch (Exception e) {
			    logger.error("error on String"+bidderNum +"To Integer", e);
		    }
	    }
	    else {
		    try {
			    bidderNum = CrawlerAction.getNumbers(bidderNum);
			    if( bidderNum.length() > 0 ){
				    project.view_num = Integer.valueOf(bidderNum);
			    }
		    } catch (Exception e) {
			    logger.error("error on String"+bidderNum +"To Integer", e);
		    }
	    }

	    //招商人ID
	    String tendererId =doc.select("#project-detail > div > div.main-top > div.job-main > div.client > div > a").attr("href");
	    if( tendererId!=null && !"".equals(tendererId) ){
		    project.tenderer_id = one.rewind.txt.StringUtil.byteArrayToHex(
				    one.rewind.txt.StringUtil.uuid("https://www.clouderwork.com" + tendererId));
	    }

	    //采集招标人信息
	    String flage =  this.getStringFromVars ("flage");
	    if( flage.equals("1") ){

		    if( tendererId!=null && !"".equals(tendererId) ){

			    try {

				    //设置参数
				    Map<String, Object> init_map = new HashMap<>();
				    init_map.put("tenderer_id", tendererId);

				    Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.TendererTask");

				    //生成holder
				    TaskHolder holder =  ChromeTaskFactory.getInstance().newHolder(clazz, init_map);

				    //提交任务
				    ((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

			    } catch ( Exception e) {
				    logger.error("error for submit TendererTask.class", e);
			    }
	        }
	    }

	    if( project.category != null ){
		    project.category.replace(" ", "");
	    }
	    project.insert();

	    if( project.status.contains("已完成") ){
	    	return false;
	    }

	    return true;

    }

	public static void registerBuilder(Class<? extends ChromeTask> clazz, String url_template, Map<String, Class> init_map_class, Map<String, Object> init_map_defaults){
		ChromeTask.registerBuilder( clazz, url_template, init_map_class, init_map_defaults );
	}
}
package com.sdyk.ai.crawler.es;

import com.sdyk.ai.crawler.nlp.NumberUtil;
import com.sdyk.ai.crawler.util.RouteUtil;
import com.sdyk.ai.crawler.util.StringUtil;
import one.rewind.txt.DateFormatUtil;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.index.query.functionscore.ScriptScoreFunctionBuilder;
import org.elasticsearch.search.SearchHit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

public class ProjectAdapter {

	/** 查询project列表
	 * @authoe gcy116149@gmail.com
	 * @version 2018/7/25 21:30
	 * @param map
	 * @param page
	 * @param length
	 * @return
	 */
	public static Map<String, Object> projectRecommend(Map map, String query, int page, int length) throws Exception {

		int from = (page - 1) * length;
		if (from < 0) from = 0;
		if (length > 30) length = 20;

		SearchResponse response =
				ESTransportClientAdapter.getClient()
						.prepareSearch("Project")
						.setTypes("project")
						.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
						.setQuery((QueryBuilder) newQueryByProject(map, query))
						.setFrom(from)
						.setSize(length)
						.setExplain(false)
						.get();
		SearchHit[] hits = response.getHits().getHits();
		long total = response.getHits().getTotalHits();

		return RouteUtil.getResult(newContent(hits), total, length, page);

	}
	/**
	 * 处理命中数据: 添加tag,处理budget_range,添加score
	 *
	 * @param hits
	 * @return
	 */
	private static List<Map<String, Object>> newContent(SearchHit[] hits) {
		List<Map<String, Object>> content = new ArrayList<>();
		for (SearchHit hit : hits) {

			Map<String, Object> map = hit.getSourceAsMap();
			// 处理预算范围
			Map<String, Object> budget_range = (Map<String, Object>) map.get("budget_range");
			double v1 = (double) budget_range.get("gte");
			double v2 = (double) budget_range.get("lte");

			map.put("budget_range", NumberUtil.doubleRangeToString(v1, v2));
			map.put("score", hit.getScore());
			content.add(map);
		}
		return content;
	}

	private static Object newQueryByProject(Map<String ,String> map, String query) throws Exception {

		while (map.entrySet().iterator().hasNext()) {
			Map.Entry entry = map.entrySet().iterator().next();

			if (entry.getValue() == null || ((String)entry.getValue()).length() == 0) {
				map.remove(entry.getKey());
			}
		}

		BoolQueryBuilder newBoolquery = new BoolQueryBuilder();

		while (map.entrySet().iterator().hasNext()) {

			Map.Entry entry = map.entrySet().iterator().next();

			if (entry.getKey().equals("begin_time")) {
				//剩余时间 > 开始时间
				newBoolquery.must(QueryBuilders.rangeQuery("due_time")
						.gte(DateFormatUtil.parseTime((String) entry.getValue())));

			}else if (entry.getKey().equals("end_time")) {
				// 剩余时间 < 结束时间
				newBoolquery.must(QueryBuilders.rangeQuery("due_time")
						.lte(DateFormatUtil.parseTime((String) entry.getValue())));

				// 比较价格
			}else if (entry.getKey().equals("budget_max")) {
				newBoolquery.must(QueryBuilders.rangeQuery("budget")
						.lte(entry.getValue()));

			}else if (entry.getKey().equals("budget_min")) {
				newBoolquery.must(QueryBuilders.rangeQuery("budget")
						.gte(entry.getValue()));

			}else {
				newBoolquery.must(QueryBuilders.termsQuery((String) entry.getKey(), entry.getValue()));
			}
		}
		if (query != null) {
			newBoolquery.should(
					multiMatchQuery(
							query,
							"title",
							"content"
					));
		}
		// search project from ELS
		/*BoolQueryBuilder newBoolquery =
				boolQuery()
						.must(QueryBuilders.termsQuery("status", "6"))
						.must(QueryBuilders.termsQuery("is_public_demand", "0"))
						.must(QueryBuilders.rangeQuery("end_time").gte(new Date().getTime()))
						.should(
								multiMatchQuery(
										map.get("_q"),
										"title",
										"description",
										*//*"skills",*//*
										"industry",
										"company_name",
										"company_description",
										"ceo_description",
										"financing",
										"city"));*/

		FunctionScoreQueryBuilder newQuery = new FunctionScoreQueryBuilder(newBoolquery/*, scoreFunction*/);

		/*if (customerId != null) {
			newQuery =
					new FunctionScoreQueryBuilder(
							newBoolquery.mustNot(termQuery("customer_id", customerId)),
							scoreFunction);
		}*/

		return newQuery;
	}
}

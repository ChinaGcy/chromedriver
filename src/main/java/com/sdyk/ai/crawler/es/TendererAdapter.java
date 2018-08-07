package com.sdyk.ai.crawler.es;

import com.sdyk.ai.crawler.nlp.NumberUtil;
import com.sdyk.ai.crawler.util.RouteUtil;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.search.SearchHit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;

public class TendererAdapter {
	/**
	 *
	 * @param map
	 * @param query
	 * @param page
	 * @param length
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> tendererRecommend(Map map, String query, int page, int length) throws Exception {

		int from = (page - 1) * length;
		if (from < 0) from = 0;
		if (length > 30) length = 20;

		SearchResponse response =
				ESTransportClientAdapter.getClient()
						.prepareSearch("Tenderer")
						.setTypes("tenderer")
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

			newBoolquery.must(QueryBuilders.termsQuery((String) entry.getKey(), entry.getValue()));

		}

		if (query != null) {
			newBoolquery.should(
					multiMatchQuery(
							query,
							"name",
							"content",
							"category",
							"req_forecast",
							"location"
					));
		}

		FunctionScoreQueryBuilder newQuery = new FunctionScoreQueryBuilder(newBoolquery/*, scoreFunction*/);
		return newQuery;
	}
}

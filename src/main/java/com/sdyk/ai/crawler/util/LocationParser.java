package com.sdyk.ai.crawler.util;

import one.rewind.json.JSON;
import one.rewind.json.JSONable;
import one.rewind.util.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class LocationParser implements Serializable {

	static final long serialVersionUID = 1L;

	public static final Logger logger = LogManager.getLogger(LocationParser.class.getName());

	public static String SerPath = "data/location-parser.ser";

	public static LocationParser instance;

	/**
	 * 单例方法
	 * @return
	 */
	public static LocationParser getInstance() throws Exception {

		if (instance == null) {

			synchronized(LocationParser.class) {

				if (instance == null) {

					instance = new LocationParser();

					try {

						long t1 = System.currentTimeMillis();
						FileInputStream fileIn = new FileInputStream(SerPath);
						ObjectInputStream in = new ObjectInputStream(fileIn);
						instance = (LocationParser) in.readObject();
						in.close();
						fileIn.close();
						float t2 = (float) (System.currentTimeMillis() - t1) / 1000;
						logger.info("Load LocationParser in {} s", t2);

					} catch (Exception e) {

						logger.error("LocationParser deserialization failed. ", e);
						logger.info("Try to build from csv.");
						instance = new LocationParser();
						instance.serialize();
					}


				}
			}
		}

		return instance;
	}

	Map<String, Province> provinces = new HashMap<>();
	Map<String, Province> province_ids = new HashMap<>();
	Map<String, List<City>> cities = new HashMap<>();
	Map<String, City> city_ids = new HashMap<>();
	Map<String, List<Area>> areas = new HashMap<>();

	/**
	 *
	 */
	public abstract static class Location implements JSONable<Province> {

		String id;
		String name;
		String alias;

		@Override
		public String toJSON() {
			return JSON.toJson(this);
		}

		public boolean match(String src) {
			if(src.contains(name)) return true;
			if(src.contains(alias)) return true;
			return false;
		}

		public abstract String toString();
	}

	/**
	 *
	 */
	public static class Province extends Location {

		static String reg = "市|省|(壮族|回族|维吾尔)?自治区|特别行政区";

		public Province() {}

		public Province(String id, String name) {
			this.id = id;
			this.name = name;
			this.alias = this.name.replaceAll(reg, "");
		}

		@Override
		public String toString() {
			return this.name;
		}
	}

	/**
	 *
	 */
	public static class City extends Location {

		Province province;

		static String reg_1 = "((景颇|土|撒拉|保安|哈萨克|藏|裕固|傈僳|普米|白|怒|独龙|布朗|佤|拉祜|纳西|哈尼|傣|水|布依|彝|羌|朝鲜|满|蒙古|回|畲|土家|苗|瑶|侗|壮|各|仡佬|仫佬|毛南|黎)族)*(哈萨克|塔吉克|锡伯|蒙古|柯尔克孜)?(自治)?州";

		static String reg_2 = "地区|盟|市";

		public City() {}

		public City(String id, String name, Province province) {
			this.id = id;
			this.name = name;
			this.alias = this.name.replaceAll(reg_1, "").replaceAll(reg_2,"");

			this.province = province;
		}

		@Override
		public String toString() {
			return province.name + "," + name;
		}
	}

	/**
	 *
	 */
	public static class Area extends Location {

		Province province;
		City city;

		static String no_reg = "市辖区|上城区|下城区|东西湖区";
		static String reg_1 = "((达斡尔|回)族)?(新|林|矿)?(区|市)";
		static String reg_2 = "(达斡尔族)?(自治)?((左|右)翼)?(左|右|前|中|后|联合)?旗";
		static String reg_3 = "((左|右)翼)?((土|撒拉|保安|哈萨克|裕固|傈僳|普米|白|怒|独龙|布朗|佤|拉祜|纳西|哈尼|傣|水|布依|藏|彝|羌|朝鲜|满|蒙古|回|畲|苗|瑶|侗|壮|各|仡佬|仫佬|毛南|黎|土家)族)*(哈萨克|塔吉克|锡伯|蒙古)?(自治)?县";

		public Area() {}

		public Area(String id, String name, City c) {

			this.id = id;
			this.name = name;

			if(this.name.length() > 2 && !this.name.matches(no_reg)) {
				String alias = this.name.replaceAll(reg_1, "").replaceAll(reg_2, "").replaceAll(reg_3, "");
				if(alias.length() > 1) {
					this.alias = alias;
				} else {
					this.alias = this.name;
				}

			} else {
				this.alias = this.name;
			}

			this.city = c;
			this.province = c.province;

			if(city.name.matches("市辖区|县|省直辖县级行政区划")) {
				this.city = null;
			}
		}

		public String toString() {
			return province.name + "," + ((city != null) ? city.name + "," : "") + name;
		}
	}

	public LocationParser() {
		init();
	}

	/**
	 * 初始化字典
	 */
	public void init() {

		// 省份信息
		List<String> province_list = getSrc("data/province.csv");

		for(String province_src : province_list){

			String src[] = province_src.split(",");

			Province p = new Province(src[2], src[1]);

			provinces.put(p.name, p);
			province_ids.put(p.id, p);
		}

		// 城市信息
		List<String> city_list = getSrc("data/city.csv");

		for(String city_src : city_list){

			String src[] = city_src.split(",");

			Province p = province_ids.get(src[3]);
			City c = new City(src[1], src[2], p);

			if(!c.name.matches("市辖区|县|省直辖县级行政区划")) {
				if(cities.get(c.name) == null) cities.put(c.name, new ArrayList<>());
				cities.get(c.name).add(c);
			}

			city_ids.put(c.id, c);
		}

		// 城市信息
		List<String> area_list = getSrc("data/area.csv");

		for(String area_src : area_list){

			String src[] = area_src.split(",");

			City c = city_ids.get(src[3]);
			Area a = new Area(src[2], src[1], c);

			if(areas.get(a.name) == null) areas.put(a.name, new ArrayList<>());
			areas.get(a.name).add(a);
		}
	}

	/**
	 *
	 */
	public void serialize() {

		try {

			FileOutputStream fileOut = new FileOutputStream(SerPath);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this);
			out.close();
			fileOut.close();
			logger.info("Serialized into {}.", SerPath);

		} catch (IOException e) {
			logger.error("Serialize error. ", e);
		}
	}

	/**
	 * 从csv中读取每一行，返回List
	 * @param fileName
	 * @return
	 */
	public static List<String> getSrc(String fileName) {

		List<String> list = new ArrayList<>();
		String src = FileUtil.readFileByLines(fileName);

		int i = 0;
		for(String line : src.split("\\n|\\r\\n")){

			if(i!=0) list.add(line);
			i++;
		}

		return list;
	}

	/**
	 *
	 * @param src
	 * @return
	 */
	public List<? extends Location> matchLocation(String src) {

		// 精确匹配区
		List<? extends Location> locations = matchArea(src, false);
		if( locations.size() == 1 ){
			return locations;
		}
		else if( locations.size() > 1 ){
			// 判断是否精确匹配城市
			List<? extends Location> locations_city = matchCity(src, false);
			if( locations_city.size() > 0 ){
				for(Location location : locations){
					if( locations_city.contains(((Area)location).city)  ){
						return Arrays.asList(location);
					}
				}
			}
			else {
				// 判断是否精确匹配省
				List<Province> locations_provinces = matchProvince(src, false);
				if( locations_provinces.size() > 0){
					for(Location location : locations){
						if( locations_provinces.contains(((Area)location).province)  ){
							return Arrays.asList(location);
						}
					}
				}
				else {
					// 模糊匹配市
					List<City> locations_city_ = matchCity(src, true);
					if( locations_city_.size() > 0 ){
						for(Location location : locations){
							if( locations_city_.contains(((Area)location).city)  ){
								return Arrays.asList(location);
							}
						}
					}
					// 模糊匹配省
					List<Province> locations_provinces_ = matchProvince(src, true);
					if(locations_provinces_.size() > 0){
						for(Location location : locations){
							if( locations_provinces_.contains(((Area)location).province)  ){
								return Arrays.asList(location);
							}
						}
					}
					// 只包含精确区字段
					return locations;
				}
			}
		}

		// 精确匹配市
		if(locations.size() == 0) locations = matchCity(src, false);

		List<Province> provinces = new ArrayList<>();
		if(locations.size() == 0) {
			// 精确匹配省
			provinces = matchProvince(src, false);
			if(provinces.size()>0 ) {
				locations = provinces;
			}
		}

		// 返回精确匹配结果
		if(locations.size() > 0) return locations;

		// 模糊匹配省
		List<Province> provinces_ = matchProvince(src, true);

		logger.info(provinces_);

		// 模糊匹配市
		List<City> cities_ = matchCity(src, true).stream()
			.map(c -> {
				logger.info(c);
				return c;
			})
			.filter(c -> {
				if(provinces_ .size() > 0) {
					// 找出被模糊匹配的省包含的模糊匹配的市
					return provinces_.contains(c.province);
				}
				return true;
			})
			.collect(Collectors.toList());

		// 模糊匹配区
		List<Area> areas_ = matchArea(src, true).stream()
			.map(a -> {
				logger.info(a);
				return a;
			})
			.filter(a -> {
				if(provinces_.size() > 0) {
					// 找出被模糊匹配的省包含的模糊匹配的区
					return provinces_.contains(a.province);
				}
				else if(cities_.size() > 0){
					// 找去被模糊匹配并过滤的市包含的区
					boolean city_match = false;
					for(City c: cities_) {
						if(a.city == c) city_match = true;
					}
					return city_match;
				}
				return true;
			})
			.collect(Collectors.toList());

		return areas_.size() > 0 ? areas_ : cities_.size() > 0 ? cities_ : provinces_;
	}


	/**
	 *
	 * @param src
	 * @return
	 */
	private List<Province> matchProvince(String src, boolean matchAlias) {

		List<Province> locations = new ArrayList<>();

		for(String name : provinces.keySet()) {

			if(matchAlias && provinces.get(name).match(src)) {
				locations.add(provinces.get(name));
				//return provinces.get(name);
			} else if(src.contains(name)) {
				locations.add(provinces.get(name));
				//return provinces.get(name);
			}
		}

		return locations;
	}

	/**
	 *
	 * @param src
	 * @return
	 */
	private List<City> matchCity(String src, boolean matchAlias) {

		List<City> locations = new ArrayList<>();

		for(String name : cities.keySet()) {

			if(matchAlias) {

				for(City city : cities.get(name)) {

					if(city.match(src)) {
						locations.add(city);
					}
				}
			}
			else if(src.contains(name)) {
				return cities.get(name);
			}
		}

		return locations;
	}

	/**
	 *
	 * @param src
	 * @param matchAlias
	 * @return
	 */
	private List<Area> matchArea(String src, boolean matchAlias) {

		List<Area> locations = new ArrayList<>();

		for(String name : areas.keySet()) {

			if(matchAlias) {

				for(Area area : areas.get(name)) {
					if(area.match(src)) {
						locations.add(area);
					}
				}
			}
			else if(src.contains(name)) {
				return areas.get(name);
			}
		}

		return locations;
	}
}

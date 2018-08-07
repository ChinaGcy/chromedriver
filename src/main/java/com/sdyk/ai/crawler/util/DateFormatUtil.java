package com.sdyk.ai.crawler.util;

import com.mysql.jdbc.jdbc2.optional.SuspendableXAConnection;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateFormatUtil extends one.rewind.txt.DateFormatUtil {

	public static Date parseTime(String in) {
		if (in == null) {
			return new Date();
		} else {
			in = in.trim();
			String prefix = null;
			Pattern p = Pattern.compile("今天|昨天|前天|\\d+(个星期|天|分钟|小时)前");
			Pattern p1 = Pattern.compile("\\d+(个月|年)前");
			Matcher m = p.matcher(in);
			Matcher m1 = p1.matcher(in);
			if (m.find()) {
				prefix = m.group();
				in = in.replaceAll(prefix, "");
			}
			if (m1.find()) {
				prefix = m1.group();
				Date date = new Date();
				date.setTime(System.currentTimeMillis() + getShiftValue_1(prefix));
				System.err.println(System.currentTimeMillis());
				System.err.println(date.getTime());
				return date;
			}

			in = in.trim();
			Date date = new Date();

			String yyyyMMdd = Calendar.getInstance().get(1) + "-" + (Calendar.getInstance().get(2) + 1) + "-" + Calendar.getInstance().get(5);
			in = in.replaceAll("日", "").replaceAll("年|月", "-").replaceAll("/", "-").replaceAll("\\.", "-").replaceAll("T", " ").replaceAll("Z", "");
			if (in.matches("\\d{9,10}")) {
				return new Date(Long.parseLong(in + "000"));
			} else if (in.matches("\\d{12,13}")) {
				return new Date(Long.parseLong(in));
			} else if (in.matches("\\d{1,2}-\\d{1,2}-\\d+")) {
				return dfn1.parseDateTime(in).toDate();
			} else if (in.matches("[A-Za-z]{3,4} \\d{1,2}, \\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2} (AM|PM)")) {
				return new Date(in);
			} else if (in.matches("\\d{2,4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}")) {
				return dff.parseDateTime(in).toDate();
			} else if (in.matches("\\d{2,4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}")) {
				return dfm.parseDateTime(in).toDate();
			} else if (in.matches("\\d{2,4}-\\d{1,2}-\\d{1,2}")) {
				return dfd.parseDateTime(in).toDate();
			} else if (in.matches("\\d{1,2}-\\d{1,2}")) {
				return dfd.parseDateTime(String.valueOf(Calendar.getInstance().get(1)) + '-' + in).toDate();
			} else if (in.matches("\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{2}")) {
				return dfm.parseDateTime(String.valueOf(Calendar.getInstance().get(1)) + '-' + in).toDate();
			} else if (in.matches("\\d{1,2}:\\d{2}:\\d{2}")) {
				return new Date(dff.parseDateTime(yyyyMMdd + " " + in).toDate().getTime() + getShiftValue(prefix));
			} else if (in.matches("\\d{1,2}:\\d{2}")) {
				return new Date(dfm.parseDateTime(yyyyMMdd + " " + in).toDate().getTime() + getShiftValue(prefix));
			} else if (in.matches("\\d{8}")) {
				return dfn.parseDateTime(in).toDate();
			} else if (in.matches("\\w+ +\\d{1,2} *, +\\d{4}")) {
				in = in.replaceAll(" +,", ",").replaceAll(" +", " ");
				return dfd_en_1.parseDateTime(in).toDate();
			} else if (in.matches("\\w+ +\\d{1,2} *, +\\d{2}")) {
				in = in.replaceAll(" +,", ",").replaceAll(" +", " ");
				return dfd_en_2.parseDateTime(in).toDate();
			} else if (in.matches("\\d{1,2} +\\w+ *, +\\d{4}")) {
				in = in.replaceAll(" +,", ",").replaceAll(" +", " ");
				return dfd_en_11.parseDateTime(in).toDate();
			} else if (in.matches("\\d{1,2} +\\w+ *, +\\d{2}")) {
				in = in.replaceAll(" +,", ",").replaceAll(" +", " ");
				return dfd_en_21.parseDateTime(in).toDate();
			} else if (in.matches("\\d{1,2} +\\w+ +\\d{4}")) {
				in = in.replaceAll(" +", " ");
				return dfd_en_12.parseDateTime(in).toDate();
			} else if (in.matches("\\d{1,2} +\\w+ +\\d{2}")) {
				in = in.replaceAll(" +", " ");
				return dfd_en_22.parseDateTime(in).toDate();
			} else if (in.matches("\\w+ +\\d{1,2} +\\d{4}")) {
				in = in.replaceAll(" +,", ",").replaceAll(" +", " ");
				return dfd_en_3.parseDateTime(in).toDate();
			} else if (in.matches("\\w+ +\\d{1,2} +\\d{2}")) {
				in = in.replaceAll(" +,", ",").replaceAll(" +", " ");
				return dfd_en_4.parseDateTime(in).toDate();
			} else if (in.matches("\\d{1,2}-\\w+-\\d{2}")) {
				return dfd_en_5.parseDateTime(in).toDate();
			} else if (in.matches("\\d{1,2}-\\w+-\\d{4}")) {
				return dfd_en_51.parseDateTime(in).toDate();
			} else if (in.matches("\\d{1,2}/-\\d{4}")) {
				return dfd_en_6.parseDateTime(in).toDate();
			} else if (in.matches("\\w+ \\d{4}")) {
				return dfd_en_61.parseDateTime(in).toDate();
			} else if (in.matches("\\d{4} \\w+")) {
				return dfd_en_62.parseDateTime(in).toDate();
			} else if (in.matches("\\d{1,2} \\w+")) {
				return dfd_en_12.parseDateTime(in + " " + Calendar.getInstance().get(1)).toDate();
			} else if (in.matches("\\w+ \\d{1,2}")) {
				return dfd_en_3.parseDateTime(in + " " + Calendar.getInstance().get(1)).toDate();
			} else {
				return prefix != null ? new Date((new Date()).getTime() + getShiftValue(prefix)) : date;
			}
		}
	}


	private static long getShiftValue(String prefix) {
		long v = 0L;
		if (prefix != null && !prefix.equals("今天")) {
			if (prefix.equals("昨天")) {
				v = -86400000L;
			} else if (prefix.equals("前天")) {
				v = -172800000L;
			} else {
				int n;
				if (prefix.matches("\\d+天前")) {
					n = Integer.parseInt(prefix.replaceAll("天前", ""));
					v = (long)(-n * 24 * 60 * 60 * 1000);
				} else if (prefix.matches("\\d+小时前")) {
					n = Integer.parseInt(prefix.replaceAll("小时前", ""));
					v = (long)(-n * 60 * 60 * 1000);
				} else if (prefix.matches("\\d+分钟前")) {
					n = Integer.parseInt(prefix.replaceAll("分钟前", ""));
					v = (long)(-n * 60 * 1000);
				} else if (prefix.matches("\\d+个星期前")) {
					n = Integer.parseInt(prefix.replaceAll("个星期前", ""));
					v = (long)(-n * 7 * 24 * 60 * 60 * 1000);
				}
			}
		}

		return v;
	}

	private static long getShiftValue_1(String prefix) {

		long v = 0L;
		long n;
		if (prefix.matches("\\d+个月前")) {
			n = Integer.parseInt(prefix.replaceAll("个月前", ""));
			v = (long)(-n* 30L * 24L * 60L * 60L * 1000L);
		} else if (prefix.matches("\\d+年前")) {
			n = Integer.parseInt(prefix.replaceAll("年前", ""));
			v = (long)(-n * 365L * 24L * 60L * 60L * 1000L);

		}

		return v;
	}

}

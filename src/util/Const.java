package util;

import java.util.HashMap;
import java.util.Map;

/**
* @PackageName:util
* @ClassName: Const
* @author: mblank
* @date: 2012-2-20 上午10:12:57
* @Description: const parameters of the system
* @Marks: TODO
*/
public class Const implements IConst{
	
	public static String WEB_SITES_PATH = rootdir+"Config/WebSites.xml";
	public static String SYS_CONFIG_PATH = rootdir+"Config/SysConfig.xml";
	public static String HIBERNATE_CFG_PATH = rootdir+"Config/hibernate.cfg.xml";
	public static String LOG4J_CONFIG_PATH = rootdir+"Config/log4j.xml";
	
	
	
	public static String CrawlerUserAgent = "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31";
	
	public static String Split_To_Sentences_Sign = "。";
	
	public static int WebSiteUrlTitleFilerLength = 5;
	public static int PublistTimeLengthPlus = 8;
	public static int SiteLogoLineCount = 10;
	public static int MeaningFulStrLen = 10;
	public static int NoMeaningSigns = 3;
	public static int SummaryTitleWordsWeighs = 3;
	public static double MaxEventSimNum = 0.7;
	public static double MaxTopicSimNum = 0.5;
	public static double AVGIDF = 8;
	public static long AVGIDFTF = 8;
	
	public static int UrlToArticlePerTimeNum = 500;
	public static int MysqlToIndexMaxItemNum = 20;
	public static int NEIGHBORHORDSIZE = 100;
	public static int ReleventEvents = 50;
	public static int ReleventArticles = 50;
	
	public static int TitleWordsWeight = 2;
	public static int NameEntityWeight = 1;
	public static int LocationWeight = 1;
	
	
	public static int[] SUBTOPICID =new int[]{1,2,4,8,16,32,64,128,256,512,1024,2048,4096,8192};

	
	
	public static long WebSiteSleepTime = 30*60*1000;
	public static long AritcleSleepTime = 5*60*1000;
	public static long ClusterToTopicSleepTime = 6*60*1000;

	public static long ArticleIndexSleepTime = 120*60*1000;

	
	public static int TopicNearDayNum = 7;
	
	public static int TopicToIndex = 2;
	public static int TopicToWebIndex = 3;
	public static int EventToTopic = 1;
	public static int EventToIndex = 2;
	public static int EventToWebIndex = 2;
	public static int NotEventToTopic = 0;
	public static int MaxEventToTopicNum = 20;
	
	public static Map<String,Integer> TASKID =null;
	
	/// for china provinces code e.g. beijing 1
	public static Map<String,Integer> ZH_PS = null;
	
	public static Map<String,Integer> loadTaskid(){
		if(TASKID==null){
			TASKID = new HashMap<String,Integer>();
			TASKID.put("urlToMysql", 0);
			TASKID.put("UrlToArticle", 1);
			TASKID.put("ArticleToTopic", 2);
			TASKID.put("TopicToIndex", 3);
			TASKID.put("HtmlFromHbaseToMysql", 4);
			TASKID.put("HtmlFromSHbaseToTHbase", 8);
			TASKID.put("MysqlToIDF", 5);
			TASKID.put("ArticleToEvent", 6);
			TASKID.put("UseMysqlArticleToIndex", 7);
			TASKID.put("ArticleToWebIndex", 8);
			TASKID.put("EventToWebIndex", 8);
		}
		return TASKID;		
	}
	
	public static void loadChinaProvince(){
		if(ZH_PS == null){
			ZH_PS = new HashMap<String,Integer>();
			ZH_PS.put("河北", 1);
			ZH_PS.put("北京", 2);
			ZH_PS.put("上海", 3);
			ZH_PS.put("重庆", 4);
			ZH_PS.put("天津", 5);
			ZH_PS.put("河南", 6);
			ZH_PS.put("云南", 7);
			ZH_PS.put("辽宁", 8);
			ZH_PS.put("黑龙江", 9);
			ZH_PS.put("湖南", 10);
			ZH_PS.put("安徽", 11);
			ZH_PS.put("山东", 12);
			ZH_PS.put("新疆", 13);
			ZH_PS.put("江苏", 14);
			ZH_PS.put("浙江", 15);
			ZH_PS.put("江西", 16);
			ZH_PS.put("湖北", 17);
			ZH_PS.put("广西", 18);
			ZH_PS.put("甘肃", 19);
			ZH_PS.put("山西", 20);
			ZH_PS.put("内蒙古", 21);
			ZH_PS.put("山西", 22);
			ZH_PS.put("吉林", 23);
			ZH_PS.put("福建", 24);
			ZH_PS.put("贵州", 25);
			ZH_PS.put("广东", 26);
			ZH_PS.put("青海", 27);
			ZH_PS.put("西藏", 28);
			ZH_PS.put("四川", 29);
			ZH_PS.put("宁夏", 30);
			ZH_PS.put("海南", 31);
			ZH_PS.put("台湾", 32);
			ZH_PS.put("澳门", 33);
			ZH_PS.put("香港", 34);
		}
	}
	
	
	public Const(){
		loadTaskid();
	}
	

}
package cn.ruc.mblank.crawler.html;

import java.io.*;

import cn.ruc.mblank.db.hbn.HSession;
import cn.ruc.mblank.db.hbn.model.Url;
import cn.ruc.mblank.db.hbn.model.UrlStatus;
import cn.ruc.mblank.util.db.Hbn;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.hibernate.Session;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


import cn.ruc.mblank.config.JsonConfigModel;
import cn.ruc.mblank.util.Const;
import cn.ruc.mblank.util.Log;

import javax.jms.*;

public class Downloader {
	
	private String SaveFolderPath;
    private int BatchSize = 100;

    private int SuccessNumber = 0;
    private int FailNumber = 0;
    private Session DSession;



	public Downloader(){
		//get save path from the Config xml
		Log.getLogger().info("Start HtmlDownloader!");
		//read Bloom filter file path from json config file
		JsonConfigModel jcm = JsonConfigModel.getConfig();
		SaveFolderPath = jcm.HtmlSavePath;
        DSession = HSession.getSession();
	}


    private void writeHtml2Disk(Url url,String html){
        String date = cn.ruc.mblank.util.TimeUtil.getDateStr(url.getCrawltime());
        File folder = new File(SaveFolderPath + date);
        if(!folder.exists()){
            folder.mkdirs();
        }
        try {
            BufferedWriter bw = null;
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(SaveFolderPath + date + File.separator + url.getId()), Const.HtmlSaveEncode));
            bw.write(html);
            bw.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runTask(){
        // ConnectionFactory ：连接工厂，JMS 用它创建连接
        ConnectionFactory connectionFactory;
        // Connection ：JMS 客户端到JMS Provider 的连接
        Connection connection = null;
        // Session： 一个发送或接收消息的线程
        javax.jms.Session session;
        // Destination ：消息的目的地;消息发送给谁.
        Destination destination;
        // 消费者，消息接收者
        MessageConsumer consumer;
        connectionFactory = new ActiveMQConnectionFactory(
                ActiveMQConnection.DEFAULT_USER,
                ActiveMQConnection.DEFAULT_PASSWORD,
                Const.MQUrl);
        try {
            // 构造从工厂得到连接对象
            connection = connectionFactory.createConnection();
            // 启动
            connection.start();
            // 获取操作连接
            session = connection.createSession(Boolean.FALSE,
                    javax.jms.Session.AUTO_ACKNOWLEDGE);
            // 获取session注意参数值xingbo.xu-queue是一个服务器的queue，须在在ActiveMq的console配置
            destination = session.createQueue(Const.URLQueueName);
            consumer = session.createConsumer(destination);
            while (true) {
                //设置接收者接收消息的时间，为了便于测试，这里谁定为100s
                ObjectMessage message = (ObjectMessage) consumer.receive();
                Url url = (Url)message.getObject();
                if (url != null) {
                    //download
                    execute(url);
                }else{
                    continue;
                }
                if(SuccessNumber % BatchSize == 0){
                    //update db
                    Hbn.updateDB(DSession);
                    DSession.clear();
                    System.out.println("download htmls " + SuccessNumber + "\t" + FailNumber + "\t" + url.getId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != connection)
                    connection.close();
            } catch (Throwable ignore) {
            }
        }
    }

	public void execute(Url url){
        UrlStatus us = Hbn.getElementFromDB(DSession,UrlStatus.class,url.getId());
        if(url == null || us == null){
            FailNumber++;
            return;
        }
        try {
            Document doc = Jsoup.connect(url.getUrl()).userAgent(Const.CrawlerUserAgent).timeout(2000).get();
            String html = doc.html();
            writeHtml2Disk(url,html);
            us.setStatus((short)Const.TaskId.DownloadUrlToHtml.ordinal());
            SuccessNumber++;
        } catch (Exception e) {
            //can't download this url.. will update the taskStatus
            us.setStatus((short)(us.getStatus() - 1));
            FailNumber++;
        }
	}
	
	public static void main(String[] args) {
        Downloader dw = new Downloader();
        dw.runTask();
	}
	
	
	

}
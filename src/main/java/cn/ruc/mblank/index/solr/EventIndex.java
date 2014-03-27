package cn.ruc.mblank.index.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import cn.ruc.mblank.db.hbn.model.Event;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import cn.ruc.mblank.util.Const;

import com.alibaba.fastjson.JSON;

import cn.ruc.mblank.config.JsonConfigModel;
import cn.ruc.mblank.config.LocalJsonConfigReader;

public class EventIndex {
	
	
	private static String solrUrl;
	
	public EventIndex(){
		String fileContent = LocalJsonConfigReader.readJsonFile(Const.SYS_JSON_CONFIG_PATH);
		JsonConfigModel jcm = JSON.parseObject(fileContent,JsonConfigModel.class);
		solrUrl = jcm.SolrIndexURI;
	}
	
	
	public void update(List<Event> events){
		try{
			SolrServer server = new HttpSolrServer(solrUrl);
			List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
			for(Event et : events){
				SolrInputDocument doc = new SolrInputDocument();
                doc.addField("id", et.getId());
                doc.addField("et_title", et.getTitle());
                doc.addField("et_summary", et.getContent());
                doc.addField("et_pubTime", et.getPubtime());
                doc.addField("et_number", et.getNumber());
                doc.addField("et_topic",et.getTopic());
                doc.addField("et_imgs",et.getImgs());
                doc.addField("et_day", et.getDay());
                docs.add(doc);
			}
			server.add(docs);
			server.commit();
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("can't update to solr...");
		}
	}
	
	public int update(Set<Event> events){
		if(events == null || events.size() == 0){
			return 0;
		}
		try{
			SolrServer server = new HttpSolrServer(solrUrl);
			List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
			for(Event et : events){
				SolrInputDocument doc = new SolrInputDocument();
				doc.addField("id", et.getId());
				doc.addField("et_title", et.getTitle());
				doc.addField("et_summary", et.getContent());
				doc.addField("et_pubTime", et.getPubtime());
				doc.addField("et_number", et.getNumber());
                doc.addField("et_topic",et.getTopic());
                doc.addField("et_imgs",et.getImgs());
                doc.addField("et_day",et.getDay());
				docs.add(doc);
			}
			server.add(docs);
            server.optimize();
			server.commit();
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("can't update to solr...");
		}
        return 1;
	}
	
	public void update(Event et){
		try{
			SolrServer server = new HttpSolrServer(solrUrl);
			List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
			SolrInputDocument doc = new SolrInputDocument();
            doc.addField("id", et.getId());
            doc.addField("et_title", et.getTitle());
            doc.addField("et_summary", et.getContent());
            doc.addField("et_pubTime", et.getPubtime());
            doc.addField("et_number", et.getNumber());
            doc.addField("et_topic",et.getTopic());
            doc.addField("et_imgs",et.getImgs());
            doc.addField("et_day",et.getDay());
			docs.add(doc);
			server.add(docs);
			server.commit();
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("can't update to solr...");
		}
	}
	
	public List<Integer> queryIds(String queryStr,int start, int num,String sort,String order){
		if(queryStr != null && queryStr.indexOf("et_title") < 0){
			queryStr  = "et_title:" + queryStr;
		}
		List<Integer> res = new ArrayList<Integer>();
		SolrServer server = new HttpSolrServer(solrUrl);
		SolrQuery query =new SolrQuery();  
        query.setQuery(queryStr);
		query.setStart(start);
		query.setRows(num);
		if(sort != null){
			if(order.equals("asc")){
				query.setSort(sort, SolrQuery.ORDER.asc);		
			}else{
				query.setSort(sort, SolrQuery.ORDER.desc);
			}
		}
		try {
			QueryResponse response = server.query(query);
			SolrDocumentList docs = response.getResults();
			for (SolrDocument doc : docs) { 
				int id = Integer.parseInt(doc.getFieldValue("id").toString());
				res.add(id);
			}
		}catch(Exception e){
			return res;
		}
		return res;
	}
	
	@SuppressWarnings("deprecation")
	public List<Event> queryEvents(String queryStr,int start, int num,String sort,String order){
		List<Event> res = new ArrayList<Event>();
		SolrServer server = new HttpSolrServer(solrUrl);
		SolrQuery query =new SolrQuery();  
        query.setQuery(queryStr);
		query.setStart(start);
		query.setRows(num);
		if(sort != null){
			if(order.equals("asc")){
				query.setSort(sort, SolrQuery.ORDER.asc);		
			}else{
				query.setSort(sort, SolrQuery.ORDER.desc);
			}
		}
		try {
			QueryResponse response = server.query(query);
			SolrDocumentList docs = response.getResults();
			for (SolrDocument doc : docs) { 
				int id = Integer.parseInt(doc.getFieldValue("id").toString());
				String title = doc.getFieldValue("et_title").toString();
				String time = doc.getFieldValue("et_pubTime").toString();
				String summary = doc.getFieldValue("et_summary").toString();
				String number = doc.getFieldValue("et_number").toString();
                String topic = doc.getFieldValue("et_topic").toString();
                String day = doc.getFieldValue("et_day").toString();
                String imgs = doc.getFieldValue("et_imgs").toString();
				Event et = new Event();
				et.setId(id);
				et.setTitle(title);
				et.setPubtime(new Date(time));
				et.setContent(summary);
                et.setImgs(imgs);
                try{
                    et.setNumber(Integer.parseInt(number));
                    et.setDay(Integer.parseInt(day));
                    et.setTopic(Integer.parseInt(topic));
                }catch (Exception e){

                }
				res.add(et);
			}
		}catch(Exception e){
			return res;
		}
		return res;
	}
	
	public void deleteItem(String id){
		SolrServer server = new HttpSolrServer(solrUrl);
		try {
			server.deleteById(id);
			server.commit();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(IOException e){
            e.printStackTrace();
        }
	}
	
	public void deleteAll(){
		SolrServer server = new HttpSolrServer(solrUrl);
		String queryStr = "*:*";
		try {
			server.deleteByQuery(queryStr);
			server.commit();
		} catch (SolrServerException e) {
			e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
	}

}

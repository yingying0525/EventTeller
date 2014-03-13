package index.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import util.Const;

import com.alibaba.fastjson.JSON;

import config.JsonConfigModel;
import config.LocalJsonConfigReader;
import db.hbn.model.Event;

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
				doc.addField("et_summary", et.getContent().substring(0, Math.min(et.getContent().length(), 100)).replace("!##!", "\n"));
				doc.addField("et_pubTime", et.getPubTime());
				docs.add(doc);
			}
			server.add(docs);
			server.commit();
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("can't update to solr...");
		}
	}
	
	public void update(Event et){
		try{
			SolrServer server = new HttpSolrServer(solrUrl);
			List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("id", et.getId());
			doc.addField("et_title", et.getTitle());
			doc.addField("et_summary", et.getContent().substring(0, Math.min(et.getContent().length(), 100)));
			doc.addField("et_pubTime", et.getPubTime());
			docs.add(doc);
			server.add(docs);
			server.commit();
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("can't update to solr...");
		}
	}
	
	public List<Integer> queryIds(String queryStr,int start, int num,String sort){
		List<Integer> res = new ArrayList<Integer>();
		SolrServer server = new HttpSolrServer(solrUrl);
		SolrQuery query =new SolrQuery();  
        query.setQuery(queryStr);
		query.setStart(start);
		query.setRows(num);
		if(sort != null){
			query.setFacetSort(sort);		
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
	
	public void deleteItem(String id){
		SolrServer server = new HttpSolrServer(solrUrl);
		try {
			server.deleteById(id);
			server.commit();
		} catch (SolrServerException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void deleteAll(){
		SolrServer server = new HttpSolrServer(solrUrl);
		String queryStr = "*:*";
		try {
			server.deleteByQuery(queryStr);
			server.commit();
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}
	}

}

package cn.ruc.mblank.index.solr;

import cn.ruc.mblank.db.hbn.model.Topic;
import cn.ruc.mblank.index.solr.model.WebTopic;
import cn.ruc.mblank.index.solr.model.WebWord;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mblank on 2014/5/9.
 */
public class TopicIndex {
    private static String solrUrl = "http://222.29.197.239:8080/solrTopics";

    public void update(List<WebTopic> wts){
        try{
            SolrServer server = new HttpSolrServer(solrUrl);
            List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
            for(WebTopic wt : wts){
                SolrInputDocument doc = new SolrInputDocument();
                doc.addField("id", wt.tp.getId());
                doc.addField("et_keyWords", wt.tp.getKeyWords());
                doc.addField("et_number", wt.tp.getNumber());
                doc.addField("et_startTime",wt.tp.getStartTime());
                doc.addField("et_endTime",wt.tp.getEndTime());
                doc.addField("et_summary",wt.tp.getSummary());
                doc.addField("et_main",wt.tp.getMain());
                doc.addField("et_object",wt.tp.getObject());
                doc.addField("et_events",wt.ids);
                docs.add(doc);
            }
            server.add(docs);
            server.commit();
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("can't update to solr...");
        }
    }


    public void update(WebTopic wt){
        try{
            SolrServer server = new HttpSolrServer(solrUrl);
            List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
            SolrInputDocument doc = new SolrInputDocument();
            doc.addField("id", wt.tp.getId());
            doc.addField("et_keyWords", wt.tp.getKeyWords());
            doc.addField("et_number", wt.tp.getNumber());
            doc.addField("et_startTime",wt.tp.getStartTime());
            doc.addField("et_endTime",wt.tp.getEndTime());
            doc.addField("et_summary",wt.tp.getSummary());
            doc.addField("et_main",wt.tp.getMain());
            doc.addField("et_object",wt.tp.getObject());
            doc.addField("et_events",wt.ids);
            docs.add(doc);
            server.add(docs);
            server.commit();
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("can't update to solr...");
        }
    }

    public WebTopic queryIds(String word){
        WebTopic wt = new WebTopic();
        Topic tp = new Topic();
        SolrServer server = new HttpSolrServer(solrUrl);
        SolrQuery query =new SolrQuery();
        query.setQuery("id:" + word);
        try {
            QueryResponse response = server.query(query);
            SolrDocumentList docs = response.getResults();
            if(docs.size() == 1){
                String id = docs.get(0).getFieldValue("id").toString();
                String keyWords = docs.get(0).getFieldValue("et_keyWords").toString();
                String summary = docs.get(0).getFieldValue("et_summary").toString();
                String number = docs.get(0).getFieldValue("et_number").toString();
                String main = docs.get(0).getFieldValue("et_main").toString();
                String object = docs.get(0).getFieldValue("et_object").toString();
                String startTime = docs.get(0).getFieldValue("et_startTime").toString();
                String endTime = docs.get(0).getFieldValue("et_endTime").toString();
                String events = docs.get(0).getFieldValue("et_events").toString();
                tp.setId(Integer.parseInt(id));
                tp.setKeyWords(keyWords);
                tp.setNumber(Integer.parseInt(number));
                tp.setMain(main);
                tp.setObject(object);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                tp.setStartTime(sdf.parse(startTime));
                tp.setEndTime(sdf.parse(endTime));
                tp.setSummary(summary);
                wt.tp = tp;
                wt.ids = events;
            }
        }catch(Exception e){
            return null;
        }
        return wt;
    }


    public void deleteItem(String id){
        SolrServer server = new HttpSolrServer(solrUrl);
        try {
            server.deleteById(id);
            server.commit();
        } catch (SolrServerException e) {
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

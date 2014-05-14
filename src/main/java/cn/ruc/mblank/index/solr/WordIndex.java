package cn.ruc.mblank.index.solr;

import cn.ruc.mblank.config.JsonConfigModel;
import cn.ruc.mblank.config.LocalJsonConfigReader;
import cn.ruc.mblank.index.solr.model.WebWord;
import cn.ruc.mblank.util.Const;
import com.alibaba.fastjson.JSON;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by mblank on 14-4-25.
 */
public class WordIndex {
    private static String solrUrl;

    public WordIndex(){
//        String fileContent = LocalJsonConfigReader.readJsonFile(Const.SYS_JSON_CONFIG_PATH);
//        JsonConfigModel jcm = JSON.parseObject(fileContent, JsonConfigModel.class);
//        solrUrl = jcm.SolrWordIndexURI;
        solrUrl = "http://222.29.197.239:8080/solrRelations";
    }




    public void update(List<WebWord> wwords){
        try{
            SolrServer server = new HttpSolrServer(solrUrl);
            List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
            for(WebWord wword : wwords){
                SolrInputDocument doc = new SolrInputDocument();
                doc.addField("id", wword.getName());
                doc.addField("web_wordTime", wword.getWordTimeNumber());
                doc.addField("web_relatedWord", wword.getRelatedWords());
                doc.addField("web_graph",wword.getPersonGraph());
                doc.addField("web_topicRelatedWords",wword.getTopicRelatedWords());
                docs.add(doc);
            }
            server.add(docs);
            server.commit();
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("can't update to solr...");
        }
    }


    public void update(WebWord wword){
        try{
            SolrServer server = new HttpSolrServer(solrUrl);
            List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
            SolrInputDocument doc = new SolrInputDocument();
            doc.addField("id", wword.getName());
            doc.addField("web_wordTime", wword.getWordTimeNumber());
            doc.addField("web_relatedWord", wword.getRelatedWords());
            doc.addField("web_graph",wword.getPersonGraph());
            doc.addField("web_topicRelatedWords",wword.getTopicRelatedWords());
            docs.add(doc);
            server.add(docs);
            server.commit();
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("can't update to solr...");
        }
    }

    public WebWord queryIds(String word){

        WebWord res = new WebWord();
        SolrServer server = new HttpSolrServer(solrUrl);
        SolrQuery query =new SolrQuery();
        query.setQuery("id:" + word);
        try {
            QueryResponse response = server.query(query);
            SolrDocumentList docs = response.getResults();
            if(docs.size() == 1){
                String name = docs.get(0).getFieldValue("id").toString();
                String timeLine = docs.get(0).getFieldValue("web_wordTime").toString();
                String related = docs.get(0).getFieldValue("web_relatedWord").toString();
                res.setPersonGraph(docs.get(0).getFieldValue("web_graph").toString());
                res.setName(name);
                res.setWordTimeNumber(timeLine);
                res.setRelatedWords(related);
            }
        }catch(Exception e){
            return null;
        }
        return res;
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

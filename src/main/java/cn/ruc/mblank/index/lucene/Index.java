package cn.ruc.mblank.index.lucene;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.ruc.mblank.db.hbn.model.Article;
import org.ansj.lucene4.AnsjAnalysis;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

@Deprecated
public abstract class Index {
	
		
	public String IndexPath;
	
	public IndexWriter CreateWriter(String IndexPath){
        try {
            Analyzer analyzer = new AnsjAnalysis();
            Directory dir = FSDirectory.open(new File(IndexPath));
            // 建立索引
            IndexWriterConfig ic = new IndexWriterConfig(Version.LUCENE_47, analyzer);
            ic.setOpenMode(OpenMode.CREATE_OR_APPEND);
            IndexWriter iwriter = new IndexWriter(dir, ic);
            return iwriter;
		} catch (IOException e) {
			e.printStackTrace();
		} 
       return null;
	}
	
	 /** 
     * 查询 
     * @throws Exception 
     */       
	public List<Article> search(String text,boolean single, int maxsize,Date atime){

    	Directory dir;
        List<Article> results = new ArrayList<Article>();
        ///check if there is the index
		File file = new File(IndexPath);
		if(!file.exists()){
			return results;
		}
		try {
			dir = FSDirectory.open(new File(IndexPath));
			IndexReader reader=DirectoryReader.open(dir);  
	        IndexSearcher searcher=new IndexSearcher(reader);  
	        TopDocs topdocs = null;
	        if(single){
		        Term term=new Term("title", text);  
		        TermQuery query=new TermQuery(term);
		        topdocs=searcher.search(query, 0);  
	        }else{
	        	AnsjAnalysis aas = new AnsjAnalysis();
	  			QueryParser parser = new QueryParser(Version.LUCENE_47, "title", aas);
	  			Query query = parser.parse(text);         
	            topdocs=searcher.search(query, maxsize);
	        }    
	        ScoreDoc[] scoreDocs=topdocs.scoreDocs;       
	        for(int i=0; i < scoreDocs.length; i++) {  
	            int doc = scoreDocs[i].doc;  
	            Document document = searcher.doc(doc);
	            Article at = new Article();
	            String id = document.get("id");
	            String title = document.get("title");
	            String content = document.get("content");
	            String topicId = document.get("topicId");
	            String date = document.get("publishtime");
	            try{
	        		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        		Date dt = sdf.parse(date);
	            	at.setPublishtime(dt);
	            }catch(Exception e){
	            	at.setPublishtime(new Date());
	            	e.printStackTrace();
	            }
	            at.setId(Integer.valueOf(id));
	            at.setTitle(title);
	            at.setContent(content);
	            at.setTopicid(Integer.valueOf(topicId));
	            if(at.getPublishtime().compareTo(atime) <= 0){
	            	results.add(at);
	            }
	        }  
	        reader.close();  
		} catch (IOException e) {
			return results;
		} catch (ParseException e) {
			return results;
		} 
		return results;
    }
	
}

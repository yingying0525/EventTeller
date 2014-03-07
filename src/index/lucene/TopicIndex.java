package index.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

import db.hbn.model.Article;

public class TopicIndex extends Index{
	
	
	public TopicIndex(String index){
		IndexPath = index;
	}
	
	@SuppressWarnings("deprecation")
	public void addDocument(List<Article> scrs){
		File file = new File(IndexPath);
		if(!file.exists()){
			file.mkdir();
		}
		IndexWriter iwriter = CreateWriter(IndexPath);
		for(Article tp : scrs){
			try {
	        	Document doc = new Document();
	   		 	doc.add(new StringField("id", tp.getId().toString(), Store.YES));  
	            doc.add(new TextField("title", tp.getTitle(), Store.YES));  
	            doc.add(new StringField("content",tp.getContent(),Store.YES));
	            doc.add(new StringField("topicId",tp.getTopicid().toString(),Store.YES));
	            doc.add(new StringField("publishtime",tp.getPublishtime().toLocaleString(),Store.YES));
				iwriter.addDocument(doc);					
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			iwriter.commit();
			iwriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void addDocument(Article instance){
		List<Article> topics = new ArrayList<Article>();
		topics.add(instance);
		addDocument(topics);
	}
	
	public void update(Article instance){
		List<Article> instances = new ArrayList<Article>();
		instances.add(instance);
		update(instances);
	}
	
	@SuppressWarnings("deprecation")
	public void update(Collection<Article> instances){
		File file = new File(IndexPath);
		if(!file.exists()){
			file.mkdir();
		}
		IndexWriter iwriter = CreateWriter(IndexPath);
		for(Article instance : instances){
			Term term=new Term("id", String.valueOf(instance.getId()));
			Document doc = new Document();
			doc.add(new StringField("id", String.valueOf(instance.getId()), Store.YES));  
			doc.add(new TextField("title", instance.getTitle(), Store.YES));  
            doc.add(new StringField("content",instance.getContent(),Store.YES));
            doc.add(new StringField("topicId",instance.getTopicid().toString(),Store.YES));
            doc.add(new StringField("publishtime",instance.getPublishtime().toLocaleString(),Store.YES));
			try {
				iwriter.updateDocument(term, doc);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			iwriter.commit();
			iwriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

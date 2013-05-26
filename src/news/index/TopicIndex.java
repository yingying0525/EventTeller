package news.index;

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

import db.data.Topic;

public class TopicIndex extends Index{
	
	
	public TopicIndex(String index){
		IndexPath = index;
	}
	
	public void addDocument(List<Topic> scrs){
		File file = new File(IndexPath);
		if(!file.exists()){
			file.mkdir();
		}
		IndexWriter iwriter = CreateWriter(IndexPath);
		for(Topic tp : scrs){
			try {
	        	Document doc = new Document();
	   		 	doc.add(new StringField("id", tp.getId().toString(), Store.YES));  
	            doc.add(new TextField("title", tp.getTitle(), Store.YES));  
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
	
	public void addDocument(Topic instance){
		List<Topic> topics = new ArrayList<Topic>();
		topics.add(instance);
		addDocument(topics);
	}
	
	public void update(Topic instance){
		List<Topic> instances = new ArrayList<Topic>();
		instances.add(instance);
		update(instances);
	}
	
	public void update(Collection<Topic> instances){
		File file = new File(IndexPath);
		if(!file.exists()){
			file.mkdir();
		}
		IndexWriter iwriter = CreateWriter(IndexPath);
		for(Topic instance : instances){
			Term term=new Term("id", String.valueOf(instance.getId()));
			Document doc = new Document();
			doc.add(new StringField("id", String.valueOf(instance.getId()), Store.YES));  
			doc.add(new TextField("title", instance.getTitle(), Store.YES));  
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

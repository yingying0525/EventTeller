package news.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;

import db.data.Topic;

public class TopicIndex extends Index{
	
	private String IndexPath ;
	
	public TopicIndex(String index){
		IndexPath = index;
	}
	
	public void update(List<Topic> scrs){
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
		scrs = null;
	}
	
	public void update(Topic instance){
		List<Topic> topics = new ArrayList<Topic>();
		topics.add(instance);
		update(topics);
	}
}

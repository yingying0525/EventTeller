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
import org.apache.lucene.index.Term;

import util.ChineseSplit;


import db.data.Article;
import db.data.Word;

public class ArticleTitleIndex extends Index{
	
	
	public ArticleTitleIndex(String path){
		this.IndexPath = path;
	}
	
	public Article checkSameInIndex(Article at){
		
		Article result = null;
		double score = -1;
		List<Word> words = ChineseSplit.SplitStrWithPos(at.getTitle());
		StringBuilder tmpQuery = new StringBuilder();
		for(Word wd : words){
			tmpQuery.append(wd.getName() +" ");				
		}		
		//no useful information of title
		if(tmpQuery.length() == 0){
			return null;
		}
		ArticleTitleIndex ati = new ArticleTitleIndex(IndexPath);
		List<Article> sims = ati.search(tmpQuery.toString(), false,500);	
		for(Article tmpat : sims){
			if(tmpat.getId() == at.getId())
				continue;
			if(tmpat != null){
				if(tmpat.getTitle().equals(at.getTitle())){
					result = tmpat;
					break;
				}		
				score = util.Similarity.ContentOverlap(tmpat.getContent(),at.getContent());
				if(score > 1.85){
					tmpat.setContent(at.getContent());
					tmpat.setTitle(at.getTitle());
				}
				if(score > util.Const.MaxEventSimNum && score <= 1.0 || score > 1.0 + util.Const.MaxEventSimNum){
					result = tmpat;
					break;
				}
			}
		}
		return result;
	}
	
	
	public void addDocument(List<Article> scrs){
		File file = new File(IndexPath);
		if(!file.exists()){
			file.mkdir();
		}
		IndexWriter iwriter = CreateWriter(IndexPath);
		for(Article at : scrs){
			try {
	        	Document doc = new Document();
	   		 	doc.add(new StringField("id", at.getId().toString(), Store.YES));  
	            doc.add(new TextField("title", at.getTitle(), Store.YES));  
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
	
	public void addDocument(Article at){
		List<Article> ats = new ArrayList<Article>();
		ats.add(at);
		addDocument(ats);
	}
	
	public void update(Article instance){
		List<Article> instances = new ArrayList<Article>();
		instances.add(instance);
		update(instances);
	}
	
	public void update(List<Article> instances){
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

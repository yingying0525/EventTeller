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
		List<Integer> sims = ati.search(tmpQuery.toString(), false,500);	
		for(Integer tmpat : sims){
			if(tmpat == at.getId())
				continue;
			///article index only contains id and title.
			/// use id to get article from db
			Article tmpatdb = util.Util.getArticleById(tmpat.toString());
			if(tmpatdb != null){
				if(tmpatdb.getTitle().equals(at.getTitle())){
					result = tmpatdb;
					break;
				}		
				score = util.Similarity.ContentOverlap(tmpatdb.getContent(),at.getContent());
				if(score > 1.85){
					tmpatdb.setContent(at.getContent());
					tmpatdb.setTitle(at.getTitle());
				}
				if(score > util.Const.MaxEventSimNum && score <= 1.0 || score > 1.0 + util.Const.MaxEventSimNum){
					result = tmpatdb;
					break;
				}
			}
		}
		return result;
	}
	
	
	public void update(List<Article> scrs){
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
	
	public void update(Article at){
		List<Article> ats = new ArrayList<Article>();
		ats.add(at);
		update(ats);
	}
	
}

package news.index;

import java.io.File;
import java.io.IOException;

import org.ansj.lucene4.AnsjAnalysis;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Index {
	
		
	public IndexWriter CreateWriter(String IndexPath){
        try {
       	Analyzer analyzer = new AnsjAnalysis();
       	Directory dir = FSDirectory.open(new File(IndexPath));  
   		// 建立索引
   		IndexWriterConfig ic = new IndexWriterConfig(Version.LUCENE_40, analyzer);
   		ic.setOpenMode(OpenMode.CREATE_OR_APPEND);
   		IndexWriter iwriter = new IndexWriter(dir, ic);
   		return iwriter;
		} catch (IOException e) {
			e.printStackTrace();
		} 
       return null;
	}

}

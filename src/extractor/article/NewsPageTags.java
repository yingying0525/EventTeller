package extractor.article;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;





public class NewsPageTags {
	
	private String   SiteName;
	private NewsPage Title;
	private NewsPage Time;
	private NewsPage Source;
	private NewsPage Content;
	private NewsPage Imgs;
	private NewsPage NewsTags;
	
	
	private static String ConfigFilePath = "Config/NewsSitePage.xml"; 
	private  Node root = null;
	
	public NewsPageTags(String sitename){
		SiteName = sitename;
		if(root == null){
			SAXReader reader = new SAXReader();
			try {
				Document doc = reader.read(new File(ConfigFilePath));
				root = doc.selectSingleNode("/Sites/Site[@name='"+SiteName + "']");
			} catch (DocumentException e) {
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static List<String> getSiteName(){
		List<String> result = new ArrayList<String>();
		SAXReader reader = new SAXReader();
		try {
			Document doc = reader.read(new File(ConfigFilePath));
			List<Element> nodes = doc.selectNodes("/Sites/Site");
			for(Element node : nodes){
				String name = node.attributeValue("name");
				if(name != null && name.length() > 0){
					result.add(name);
				}
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private NewsPage getNode(String name){
		NewsPage result = new NewsPage();
		Node sub_root = root.selectSingleNode(name);
		if(sub_root == null)
			return null;
		Node id = sub_root.selectSingleNode("Id");
		Node tag = sub_root.selectSingleNode("Tag");
		Node node_class = sub_root.selectSingleNode("Class");
		Element subtag = (Element)sub_root.selectSingleNode("SubTag");
		Node format = sub_root.selectSingleNode("Format");
		Node linkatt = sub_root.selectSingleNode("LinkAtt");
		Node textatt = sub_root.selectSingleNode("TextAtt");
		Node baseurl = sub_root.selectSingleNode("BaseUrl");
		int subtagindex = -1;
		if(subtag != null){
			String strsubtagindex = subtag.attributeValue("index");
			subtagindex = Integer.valueOf(strsubtagindex);
		}
		if(tag != null){
			result.setTag(tag.getText());
		}
		if(id != null){
			result.setId(id.getText());
		}
		if(node_class != null){
			result.setTagClass(node_class.getText());
		}
		if(subtag != null){
			result.setSubTag(subtag.getText());
		}
		if(format != null){
			result.setFormat(format.getText());
		}
		if(linkatt != null){
			result.setLinkAtt(linkatt.getText());
		}
		if(textatt != null){
			result.setTextAtt(textatt.getText());
		}
		if(baseurl != null){
			result.setBaseUrl(baseurl.getText());
		}
		if(subtagindex > 0){
			result.setSubTagIndex(subtagindex);
		}		
		return result;
		
	}
	
	
	public NewsPage getTitle(){
		Title = getNode("Title");
		return Title;
	}
	
	public NewsPage getTime(){
		Time = getNode("Time");
		return Time;
	}
	
	public NewsPage getSource(){
		
		Source = getNode("Source");		
		return Source;
	}
	
	public NewsPage getContent(){
		
		Content = getNode("Content");		
		return Content;
	}
	
	public NewsPage getImgs(){
		
		Imgs = getNode("NewsImg");		
		return Imgs;
	}
	
	public NewsPage getNewsTags(){
		
		NewsTags = getNode("NewsTag");		
		return NewsTags;
	}
	
	
	
	
	

}

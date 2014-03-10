package util;

import java.io.File;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
* @PackageName:util
* @ClassName: Config
* @author: mblank
* @date: 2012-2-20 下午4:59:07
* @Description: read xmls
* @Marks: TODO
*/
public class Config {
	
	private  Document document = null;
	
	public Config(String configPath){
		if(document==null){
			SAXReader docreader = new SAXReader();
			try {
				document = docreader.read(new File(configPath));
			} catch (DocumentException e) {
			//LOG
				e.printStackTrace();
			}
		}
	}
	

	/**
	 * @param XPath
	 * @return
	 * @Description:get the text of selected node by xpath
	 */
	public String getText(String XPath){
		return selectNode(XPath).getText(); 
	}
	
	/**
	 * @param xpath
	 * @return
	 * @Description:get the node of selected by xpath
	 */
	public  Node selectNode(String xpath) {
		return  document.selectSingleNode(xpath);
	}
	
	/**
	 * @param xpath
	 * @return
	 * @Description:get the nodes of selected by xpath
	 */
	@SuppressWarnings("rawtypes")
	public List selectNodes(String xpath){
		return document.selectNodes(xpath);
	}
	
}

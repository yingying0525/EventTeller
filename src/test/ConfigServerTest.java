package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import love.cq.util.StringUtil;


import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;


class MyHttpHandler implements HttpHandler{

	public static String FILE_ENCODING = "utf-8";
	
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		// TODO Auto-generated method stub
		
		 String requestMethod = exchange.getRequestMethod();  
	        if (requestMethod.equalsIgnoreCase("GET")) {  
	            Headers responseHeaders = exchange.getResponseHeaders();  
	            responseHeaders.set("Content-Type", "text/plain");  
	            exchange.sendResponseHeaders(200, 0);  
	            parseParamers(exchange);
	            OutputStream responseBody = exchange.getResponseBody();  
	            Headers requestHeaders = exchange.getRequestHeaders();  
	            exchange.getRequestBody();
	            Set<String> keySet = requestHeaders.keySet();  
	            Iterator<String> iter = keySet.iterator();  
	            while (iter.hasNext()) {  
	                String key = iter.next();  
	                List<String> values = requestHeaders.get(key);  
	                String s = key + " = " + values.toString() + "\n";  
	                responseBody.write(s.getBytes());  
	            }  
	            responseBody.close();  
	        }  
	}
	
	/**
	 * 从get请求中解析参数
	 * 
	 * @param query
	 * @param parameters
	 */
	private void parseQuery(String query, Map<String, String> parameters) {
		// TODO Auto-generated method stub
		if (StringUtil.isBlank(query)) {
			return;
		}
		String split[] = query.split("\\?");
		query = split[split.length - 1];
		split = query.split("&");
		String[] param = null;
		String key = null;
		String value = null;
		for (String kv : split) {
			try {
				param = kv.split("=");
				if (param.length == 2) {
					key = URLDecoder.decode(param[0], FILE_ENCODING);
					value = URLDecoder.decode(param[1], FILE_ENCODING);
					parameters.put(key, value);
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	
	private Map<String, String> parseParamers(HttpExchange httpExchange) throws UnsupportedEncodingException, IOException {
		// TODO Auto-generated method stub
		BufferedReader reader = null;
		try {
			Map<String, String> parameters = new HashMap<String, String>();
			URI requestedUri = httpExchange.getRequestURI();
			String query = requestedUri.getRawQuery();			
			// get 请求解析
			parseQuery(query, parameters);
//			// post 请求解析
//			reader = IOUtil.getReader(httpExchange.getRequestBody(), FILE_ENCODING);
//			query = IOUtil.getContent(reader).trim();
//			parseQuery(query, parameters);
			httpExchange.setAttribute("parameters", parameters);
			return parameters;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
	
}


public class ConfigServerTest {
	


	
	public static void startServer(int port) throws IOException{
		
		HttpServerProvider provider = HttpServerProvider.provider();
		HttpServer httpserver = provider.createHttpServer(new InetSocketAddress(8888), 100);// 监听端口6666,能同时接
																									// 受100个请求
		httpserver.createContext("/", new MyHttpHandler());
		httpserver.setExecutor(null);
		httpserver.start();
		System.out.println("server started");
		
	}
	
	
	public static void main(String[] args) throws IOException{
		new ConfigServerTest();
		ConfigServerTest.startServer(9);
	}

}

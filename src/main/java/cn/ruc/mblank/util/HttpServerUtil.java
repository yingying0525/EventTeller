package cn.ruc.mblank.util;

import com.sun.net.httpserver.HttpExchange;
import love.cq.util.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mblank on 14-4-25.
 */
public class HttpServerUtil {

    private static String FILE_ENCODING = "utf-8";

    public static Map<String, String> parseParamers(HttpExchange httpExchange) throws UnsupportedEncodingException, IOException {
        BufferedReader reader = null;
        try {
            Map<String, String> parameters = new HashMap<String, String>();
            URI requestedUri = httpExchange.getRequestURI();
            String query = requestedUri.getRawQuery();
            query = URLDecoder.decode(query, "utf-8");
            // get 请求解析
            parseQuery(query, parameters);
//            // post 请求解析
//            reader = IOUtil.getReader(httpExchange.getRequestBody(), FILE_ENCODING);
//            query = IOUtil.getContent(reader).trim();
//            parseQuery(query, parameters);
            httpExchange.setAttribute("parameters", parameters);
            return parameters;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * 从get请求中解析参数
     *
     * @param query
     * @param parameters
     */
    public static void parseQuery(String query, Map<String, String> parameters) {
        if (love.cq.util.StringUtil.isBlank(query)) {
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
                e.printStackTrace();
            }
        }
    }


    public static void writeToClient(HttpExchange httpExchange, String responseMsg) throws IOException {
        byte[] bytes = responseMsg.getBytes();
        httpExchange.sendResponseHeaders(200, bytes.length); // 设置响应头属性及响应信息的长度
        OutputStream out = httpExchange.getResponseBody(); // 获得输出流
        out.write(bytes);
        out.flush();
    }
}

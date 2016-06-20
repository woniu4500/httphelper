package com.li.httphelper;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class HttpMultiConReqHelper {
	private static Integer socketTimeout = 5000;
	private static Integer connectTimeout = 60000;
	private static Integer connectionRequestTimeout = 5000;
	
	private static HttpClientWrapper hw = new HttpClientWrapper(connectionRequestTimeout,
			connectTimeout, socketTimeout);;
	
	public HttpMultiConReqHelper(){
		super();
	}
	
	public static ResponseContent getUrlRespContent(String url) {
		ResponseContent response = null;
		try {
			IdentityHashMap<String,Object> header = new IdentityHashMap<String,Object>();
			header.put(new String("Cookie"), hw.getCookieString()); 
			response = hw.getResponse(url, header);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
	
	/**
	 * 将参数拼装在url中，进行post请求。
	 * 
	 * @param url
	 * @return
	 */
	public static ResponseContent postUrl(String url,Map<String, String> paramMap) {
		IdentityHashMap<String,Object> header = new IdentityHashMap<String,Object>();
		header.put(new String("Cookie"), hw.getCookieString()); 
		ResponseContent ret = null;
		try {
			Set<String> keys = paramMap.keySet();
			for(String key :keys){
				hw.addNV(key, paramMap.get(key));
			}
			ret = hw.postNV(url, null, header, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public static void testGet() {
		String url = "http://58.220.193.178:8880/yw/login.asp?username=100068875&password=123";
		ResponseContent responseContent = HttpMultiConReqHelper.getUrlRespContent(url);
		responseContent.setEncoding("GBK");
		try {
			System.out.println(responseContent.getContent());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println(hw.getCookieString());
		
		String url1 = "http://58.220.193.178:8880/yw/detail_p.asp";
		ResponseContent responseContent1 = HttpMultiConReqHelper.getUrlRespContent(url1);
		responseContent1.setEncoding("GBK");
		try {
			System.out.println(responseContent1.getContent());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testPost() {
		
//		String urlLogin = "http://220.178.98.86/hfgjj/jsp/web/public/search/grloginAct.jsp?lb=b&hm=340123197412145792&mm=123456&yzm=3591";
//		IdentityHashMap<String,Object> header = new IdentityHashMap<String,Object>();
//		header.put(new String("Cookie"), "JSESSIONID=3C9759CABA6814F6E3BFDEADA75C55B3.tomcat1"); 
//		ResponseContent responseContent = HttpMultiConReqHelper.postUrl(urlLogin,null);
//		responseContent.setEncoding("GBK");
//		try {
//			System.out.println(responseContent.getContent());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		System.out.println(hw.getCookieString());
//
//		String url = "http://220.178.98.86/hfgjj/jsp/web/public/search/grCenter.jsp?url=3&dkzh=";
//		
////		IdentityHashMap<String,Object> header = new IdentityHashMap<String,Object>();
////		header.put(new String("Cookie"), "JSESSIONID=BF43296DC1D935161EA212CBEE20334C.tomcat1"); 
//		responseContent = HttpMultiConReqHelper.postUrl(url,null);
//		try {
//			System.out.println(responseContent.getContent());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		System.out.println(hw.getCookieString());
		
	}
	
	public static void main(String [] args){
		testGet();
	}
	
}

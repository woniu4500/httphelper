package com.li.httphelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.IdentityHashMap;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

public class HttpHelper {
	private static Integer socketTimeout = 5000;
	private static Integer connectTimeout = 60000;
	private static Integer connectionRequestTimeout = 5000;

	/**
	 * 使用Get方式 根据URL地址，获取ResponseContent对象
	 * 
	 * @param url
	 *            完整的URL地址
	 * @return ResponseContent 如果发生异常则返回null，否则返回ResponseContent对象
	 */
	public static ResponseContent getUrlRespContent(String url,
			IdentityHashMap<String, Object> headers) {
		HttpClientWrapper hw = new HttpClientWrapper(connectionRequestTimeout,
				connectTimeout, socketTimeout);
		ResponseContent response = null;
		try {
			response = hw.getResponse(url, headers);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 使用Get方式 根据URL地址，获取ResponseContent对象
	 * 
	 * @param url
	 *            完整的URL地址
	 * @param urlEncoding
	 *            编码，可以为null
	 * @return ResponseContent 如果发生异常则返回null，否则返回ResponseContent对象
	 */
	public static ResponseContent getUrlRespContent(String url,
			String urlEncoding, IdentityHashMap<String, Object> headers) {
		HttpClientWrapper hw = new HttpClientWrapper(connectionRequestTimeout,
				connectTimeout, socketTimeout);
		ResponseContent response = null;
		try {
			response = hw.getResponse(url, urlEncoding, headers);
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
	public static ResponseContent postUrl(String url,
			IdentityHashMap<String, Object> headers) {
		HttpClientWrapper hw = new HttpClientWrapper(connectionRequestTimeout,
				connectTimeout, socketTimeout);
		ResponseContent ret = null;
		try {
			setParams(url, hw);
			ret = hw.postNV(url, headers);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	private static void setParams(String url, HttpClientWrapper hw) {
		String[] paramStr = url.split("[?]", 2);
		if (paramStr == null || paramStr.length != 2) {
			return;
		}
		String[] paramArray = paramStr[1].split("[&]");
		if (paramArray == null) {
			return;
		}
		for (String param : paramArray) {
			if (param == null || "".equals(param.trim())) {
				continue;
			}
			String[] keyValue = param.split("[=]", 2);
			if (keyValue == null || keyValue.length != 2) {
				continue;
			}
			hw.addNV(keyValue[0], keyValue[1]);
		}
	}

	/**
	 * 上传文件（包括图片）
	 * 
	 * @param url
	 *            请求URL
	 * @param paramsMap
	 *            参数和值
	 * @return
	 */
	public static ResponseContent postEntity(String url,
			IdentityHashMap<String, Object> paramsMap,
			IdentityHashMap<String, Object> paramsMapHeader) {
		HttpClientWrapper hw = new HttpClientWrapper(connectionRequestTimeout,
				connectTimeout, socketTimeout);
		ResponseContent ret = null;
		try {
			setParams(url, hw);
			Iterator<String> iterator = paramsMap.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				Object value = paramsMap.get(key);
				if (value instanceof File) {
					FileBody fileBody = new FileBody((File) value);
					hw.getContentBodies().add(fileBody);
				} else if (value instanceof byte[]) {
					byte[] byteVlue = (byte[]) value;
					ByteArrayBody byteArrayBody = new ByteArrayBody(byteVlue,
							key);
					hw.getContentBodies().add(byteArrayBody);
				} else {
					if (value != null && !"".equals(value)) {
						hw.addNV(key, String.valueOf(value));
					} else {
						hw.addNV(key, "");
					}
				}
			}
			ret = hw.postEntity(url, paramsMapHeader);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * 使用post方式，发布对象转成的json给Rest服务。
	 * 
	 * @param url
	 * @param jsonBody
	 * @return
	 */
	public static ResponseContent postJsonEntity(String url, String jsonBody,
			IdentityHashMap<String, Object> headers, boolean modifyurl) {
		return postEntity(url, jsonBody, "application/json", headers, modifyurl);
	}

	/**
	 * 使用post方式，发布对象转成的xml给Rest服务
	 * 
	 * @param url
	 *            URL地址
	 * @param xmlBody
	 *            xml文本字符串
	 * @return ResponseContent 如果发生异常则返回空，否则返回ResponseContent对象
	 */
	public static ResponseContent postXmlEntity(String url, String xmlBody,
			IdentityHashMap<String, Object> headers, boolean modifyurl) {
		return postEntity(url, xmlBody, "application/xml", headers, modifyurl);
	}

	private static ResponseContent postEntity(String url, String body,
			String contentType, IdentityHashMap<String, Object> headers,
			boolean modifyurl) {
		HttpClientWrapper hw = new HttpClientWrapper(connectionRequestTimeout,
				connectTimeout, socketTimeout);
		ResponseContent ret = null;
		try {
			hw.addNV("body", body);
			ret = hw.postNV(url, contentType, headers, modifyurl);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	// test
	public static void testGet() {
//		String url = "http://172.16.128.157:8080/hhc.gate/doSendRegVaild.action?mobile=18682265052";
//		String url = "http://58.220.193.178:8880/yw/login.asp?username=100068875&password=123";
//		ResponseContent responseContent = getUrlRespContent(url, null);
//		String url = "http://58.220.193.178:8880/yw/login.asp?username=100068875&password=123";
////		IdentityHashMap<String,Object> header = new IdentityHashMap<String,Object>();
////		header.put(new String("Cookie"), "ASPSESSIONIDQSCRSRAS=PDFICLHDKMMHOODOHOEAJNCF"); 
//		ResponseContent responseContent = HttpHelper.getUrlRespContent(url, null);
//		responseContent.setEncoding("GBK");
//		try {
//			System.out.println(responseContent.getContent());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
//		year=2014&PageNo=2
		String url1 = "http://58.220.193.178:8880/yw/detail_p.asp?year=2016&PageNo=2";
		IdentityHashMap<String,Object> header = new IdentityHashMap<String,Object>();
//		ASPSESSIONIDQQCRSRAS=BJPICLHDPGMEEKFHPGDEAEHJ
		header.put(new String("Cookie"), "ASPSESSIONIDQQCRSRAS=BJPICLHDPGMEEKFHPGDEAEHJ"); 
		ResponseContent responseContent1 = HttpHelper.getUrlRespContent(url1, header);
		responseContent1.setEncoding("GBK");
		try {
			System.out.println(responseContent1.getContent());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	// test
	public static void testLogin() {
		 String url =
		 "http://localhost:8180/hhc.gate/app_login_check?j_username=18682265052&j_password=654321&remember-me=on";
//		String url = "http://localhost:8180/hhc.gate/app_login_check?j_username=18682265052&j_password=123456";
//		String url = "http://172.16.128.120:8180/fenqiconsole/applogin.action?mobile=15618532505&password=2d1a623e6490b38cb138f0930c254b6d";
		
//		String url = "http://58.220.193.178:8880/yw/login.asp?username=100068875&password=123";
		ResponseContent responseContent = postUrl(url, null);

		try {
			System.out.println(responseContent.getContent());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// test
	public static void testUploadFile() {
		// String url =
		// "http://localhost:8180/hhc.gate/account/findAllMerchantList.action";
		// IdentityHashMap<String,Object> header = new
		// IdentityHashMap<String,Object>();
		// header.put("Cookie", rememberMe);
		// ResponseContent responseContent = postUrl(url,header);
		// try {
		// System.out.println(responseContent.getContent());
		// } catch (UnsupportedEncodingException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		try {
			// String url =
			// "http://172.16.128.102:8080/hhc.gate/account/doUploadMhtAttach.action";
			String url = "http://localhost:8180/hhc.gate/account/doUploadMhtAttach.action";

			// String url =
			// "http://localhost:8080/loan/webUser/uploadAttachUserFile.action";
			IdentityHashMap<String, Object> paramsMap = new IdentityHashMap<String, Object>();
			paramsMap.put("mchntCode", "41105");
			paramsMap.put("attachType", "ARTIF_CERTIFID");
			File uploadFile = new File("D:\\shande2.png");
			paramsMap.put("uploadFile", uploadFile);

			String jessionid = "JSESSIONID=14auk587m7jnj1p919n5v2vmle";
			IdentityHashMap<String, Object> header = new IdentityHashMap<String, Object>();
			header.put("Cookie", jessionid);
			ResponseContent responseContent = HttpHelper.postEntity(url,
					paramsMap, header);
			System.out.println(responseContent.getContent());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	public static String postJson(String url, String json) {
//		HttpClient client = new DefaultHttpClient();
//		HttpPost post = new HttpPost(url);
//		String response = null;
//		try {
//			StringEntity s = new StringEntity(json, "utf-8");
//			post.setHeader("Content-Type",
//					"application/x-www-form-urlencoded; charset=utf-8");
//			post.setEntity(s);
//			HttpResponse res = client.execute(post);
//			if (res.getStatusLine().getStatusCode() == 200) {
//				HttpEntity entity = res.getEntity();
//				response = getStrFromInputSteam(entity.getContent(), "UTF-8");
//			}
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//		return response;
//
//	}
	
	public static String postJson(String url, String json) {
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(url);
		String response = null;
		try {
			post.setHeader("Content-Type",
					"application/x-www-form-urlencoded; charset=utf-8");
			post.setEntity(new ByteArrayEntity(
					json.getBytes("UTF-8")));
			HttpResponse res = client.execute(post);
			if (res.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = res.getEntity();
				response = getStrFromInputSteam(entity.getContent(), "UTF-8");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return response;
	}

//	public static String postJson(String url, String json) {
//		String response = null;
//		try {
//			HttpClient client = new DefaultHttpClient();
//			HttpPost post = new HttpPost(url);
//			post.setHeader("Content-Type",
//					"application/x-www-form-urlencoded; charset=utf-8");
//			NameValuePair nameValuePair = new BasicNameValuePair("body", json);
//			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
//			nameValuePairs.add(nameValuePair);
//			UrlEncodedFormEntity encodedHE = new UrlEncodedFormEntity(
//					nameValuePairs, HTTP.UTF_8);
//			
//			encodedHE.setContentType("application/json");
//			post.setEntity(encodedHE);
//			post.setHeader("Accept", "application/json");
//			HttpResponse res = client.execute(post);
//			if (res.getStatusLine().getStatusCode() == 200) {
//				HttpEntity entity = res.getEntity();
//				response = getStrFromInputSteam(entity.getContent(), "UTF-8");
//			}
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//		return response;
//
//	}

	public static String getStrFromInputSteam(InputStream in, String charset)
			throws IOException {
		BufferedReader bf = new BufferedReader(new InputStreamReader(in,
				charset));
		// 最好在将字节流转换为字符流的时候 进行转码
		StringBuffer buffer = new StringBuffer();
		String line = "";
		while ((line = bf.readLine()) != null) {
			buffer.append(line);
		}
		return buffer.toString();
	}

	public static void main(String[] args) {
		 testGet();
//		 testLogin();
		// String rememberMe = "JSESSIONID=1v1w4lrkscim01cj59pe4yjzx5";
		// String rememberMe = "SPRING_SECURITY_REMEMBER_ME_COOKIE=null";
		// testUploadFile();
//		String url = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=r5FOf-cPiNKt6l_ErcvTIhdN3UOKsxm0bDVcSNwpyJ2Wrp7gp3eB36I84HT_ahMAv-3UV3J81OsJ6YlTgtYehWrIc-bX4tYiyjhyfLYZpGIOPHbAFATUM";
//
////		 String jsonMessage =
////		 "{'msgtype':'text','text':{'content':'欢迎查看优贷通报表\n20151116--目前    新增会员124人,贷款申请5位\n资料已提交1位\n资料录已审核1位\n审核已驳回3位\n\n\n20151116--目前    杉德新增会员1人,贷款申请0位\n'},'touser':'oTZ_ysn90OXB_FhTGZAeTIZPwElw'}";
//		String jsonMessage = "{\"touser\": \"oTZ_ysn90OXB_FhTGZAeTIZPwElw\",\"msgtype\": \"text\",\"text\": {\"content\": \"你好\"}}";
//		try {
//			// String str = postJsonEntity(url, jsonMessage, null, false)
//			// .getContent();
//			String str = postJson(url, jsonMessage);
//			System.out.println(str);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	
	
}
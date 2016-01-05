package com.li.httphelper;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.CodingErrorAction;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.CharsetUtils;
import org.apache.http.util.EntityUtils;

public class HttpClientWrapper {
	private enum VERBTYPE {
		GET, POST
	}

	private Integer socketTimeout = 5000;
	private Integer connectTimeout = 60000;
	private Integer connectionRequestTimeout = 5000;

	private static CloseableHttpClient client;
	private RequestConfig requestConfig;
	private List<ContentBody> contentBodies;
	private List<NameValuePair> nameValuePostBodies;
	private static PoolingHttpClientConnectionManager connManager = null;
	private static CookieStore cookieStore = null;

	static {
		try {
			SSLContext sslContext = SSLContexts.custom().useTLS().build();
			sslContext.init(null, new TrustManager[] { new X509TrustManager() {

				public void checkClientTrusted(
						java.security.cert.X509Certificate[] arg0, String arg1)
						throws CertificateException {
				}

				public void checkServerTrusted(
						java.security.cert.X509Certificate[] arg0, String arg1)
						throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			} }, null);
			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
					.<ConnectionSocketFactory> create()
					.register("http", PlainConnectionSocketFactory.INSTANCE)
					.register("https",
							new SSLConnectionSocketFactory(sslContext)).build();
			connManager = new PoolingHttpClientConnectionManager(
					socketFactoryRegistry);
			SocketConfig socketConfig = SocketConfig.custom()
					.setTcpNoDelay(true).build();
			connManager.setDefaultSocketConfig(socketConfig);
			MessageConstraints messageConstraints = MessageConstraints.custom()
					.setMaxHeaderCount(200).setMaxLineLength(2000).build();
			ConnectionConfig connectionConfig = ConnectionConfig.custom()
					.setMalformedInputAction(CodingErrorAction.IGNORE)
					.setUnmappableInputAction(CodingErrorAction.IGNORE)
					.setCharset(Consts.UTF_8)
					.setMessageConstraints(messageConstraints).build();
			connManager.setDefaultConnectionConfig(connectionConfig);
			connManager.setMaxTotal(200);
			connManager.setDefaultMaxPerRoute(20);
		} catch (KeyManagementException e) {

		} catch (NoSuchAlgorithmException e) {

		}
	}

	public HttpClientWrapper() {
		super();
		// client = HttpClientBuilder.create().build();//不使用连接池
		client = HttpClients.custom().setConnectionManager(connManager).build();
		this.contentBodies = new ArrayList<ContentBody>();
		this.nameValuePostBodies = new LinkedList<NameValuePair>();
		this.requestConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(this.connectionRequestTimeout)
				.setConnectTimeout(this.connectTimeout)
				.setSocketTimeout(this.socketTimeout).build();
		this.requestConfig = RequestConfig.custom()
				.setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY).build();
	}

	public HttpClientWrapper(Integer connectionRequestTimeout,
			Integer connectTimeout, Integer socketTimeout) {
		super();
		this.socketTimeout = socketTimeout;
		this.connectTimeout = connectTimeout;
		this.connectionRequestTimeout = connectionRequestTimeout;
		this.contentBodies = new ArrayList<ContentBody>();
		this.nameValuePostBodies = new LinkedList<NameValuePair>();
		// client = HttpClientBuilder.create().build();//不使用连接池
		client = HttpClients.custom().setConnectionManager(connManager).build();
		this.requestConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(this.connectionRequestTimeout)
				.setConnectTimeout(this.connectTimeout)
				.setSocketTimeout(this.socketTimeout).build();
		this.requestConfig = RequestConfig.custom()
				.setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY).build();
	}

	/**
	 * Get方式访问URL
	 * 
	 * @param url
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	public ResponseContent getResponse(String url, IdentityHashMap<String, Object> headers)
			throws HttpException, IOException {
		return this.getResponse(url, "UTF-8", VERBTYPE.GET, null, headers,false);
	}

	/**
	 * Get方式访问URL
	 * 
	 * @param url
	 * @param urlEncoding
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	public ResponseContent getResponse(String url, String urlEncoding,
			IdentityHashMap<String, Object> headers) throws HttpException, IOException {
		return this.getResponse(url, urlEncoding, VERBTYPE.GET, null, headers,false);
	}

	/**
	 * POST方式发送名值对请求URL
	 * 
	 * @param url
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	public ResponseContent postNV(String url, IdentityHashMap<String, Object> headers)
			throws HttpException, IOException {
		return this.getResponse(url, "UTF-8", VERBTYPE.POST, null, headers,true);
	}

	public ResponseContent postNV(String url, String contentType,
			IdentityHashMap<String, Object> headers,boolean modifyurl) throws HttpException, IOException {
		return getResponse(url, "UTF-8", VERBTYPE.POST, contentType, headers,modifyurl);
	}

	/**
	 * 根据url编码，请求方式，请求URL
	 * 
	 * @param urlstr
	 * @param urlEncoding
	 * @param bodyType
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	@SuppressWarnings("null")
	public ResponseContent getResponse(String urlstr, String urlEncoding,
			VERBTYPE bodyType, String contentType, IdentityHashMap<String, Object> headers,boolean modifyurl)
			throws HttpException, IOException {

		if (urlstr == null)
			return null;

		String url = urlstr;
		if (urlEncoding != null)
			url = HttpClientWrapper.encodeURL(url.trim(), urlEncoding);

		HttpEntity entity = null;
		HttpRequestBase request = null;
		CloseableHttpResponse response = null;
		try {
			if (VERBTYPE.GET == bodyType) {
				request = new HttpGet(url);
			} else if (VERBTYPE.POST == bodyType) {
				this.parseUrl(url);
				HttpPost httpPost = null;
				if(modifyurl){
					httpPost = new HttpPost(toUrl());
				}else{
					httpPost = new HttpPost(url);
				}

				List<NameValuePair> nvBodyList = this.getNVBodies();
				httpPost.setEntity(new UrlEncodedFormEntity(nvBodyList,
						urlEncoding));
				request = httpPost;
			}

			if (contentType != null) {
				request.addHeader(HttpHeaders.CONTENT_TYPE, contentType);
			}

			// 设置 header 为外围传入cookie值准备
			Header headerss[] = buildHeader(headers);
			if (headerss != null && headerss.length > 0) {
				request.setHeaders(headerss);
			}

			request.setConfig(requestConfig);
			request.addHeader(HttpHeaders.USER_AGENT,
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322)");

			response = client.execute(request);
			printResponse(response);
			CookieStore cookieStore = setCookieStore(response);
			
			if(null!=response.getFirstHeader("Location")){
				String location = response.getFirstHeader("Location").getValue();
				if (location.contains("login")) {
					// 执行get请求
					HttpGet httpGet = new HttpGet(location);
					response = client.execute(httpGet);
					printResponse(response);
				}
			}

			entity = response.getEntity(); // 获取响应实体
			StatusLine statusLine = response.getStatusLine();

			ResponseContent ret = new ResponseContent();
			ret.setStatusCode(statusLine.getStatusCode());
			ret.setCookieStore(cookieStore);
			getResponseContent(entity, ret);
			return ret;
		} finally {
			close(entity, request, response);
		}
	}

	private void getResponseContent(HttpEntity entity, ResponseContent ret)
			throws IOException {
		Header enHeader = entity.getContentEncoding();
		if (enHeader != null) {
			String charset = enHeader.getValue().toLowerCase();
			ret.setEncoding(charset);
		}
		String contenttype = this.getResponseContentType(entity);
		ret.setContentType(contenttype);
		ret.setContentTypeString(this.getResponseContentTypeString(entity));
		ret.setContentBytes(EntityUtils.toByteArray(entity));
	}

	public ResponseContent postEntity(String url,IdentityHashMap<String, Object> paramsMapHeader) throws HttpException,
			IOException {
		return this.postEntity(url, "UTF-8", paramsMapHeader);
	}

	/**
	 * POST方式发送名值对请求URL,上传文件（包括图片）
	 * 
	 * @param url
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	public ResponseContent postEntity(String url, String urlEncoding,IdentityHashMap<String, Object> paramsMapHeader)
			throws HttpException, IOException {
		if (url == null)
			return null;

		HttpEntity entity = null;
		HttpRequestBase request = null;
		CloseableHttpResponse response = null;
		try {
			this.parseUrl(url);
			HttpPost httpPost = new HttpPost(toUrl());

			// 对请求的表单域进行填充
			MultipartEntityBuilder entityBuilder = MultipartEntityBuilder
					.create();
			entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			for (NameValuePair nameValuePair : this.getNVBodies()) {
				entityBuilder.addPart(
						nameValuePair.getName(),
						new StringBody(nameValuePair.getValue(), ContentType
								.create("text/plain", urlEncoding)));
			}
			for (ContentBody contentBody : getContentBodies()) {
				entityBuilder.addPart("uploadFile", contentBody);
			}
			entityBuilder.setCharset(CharsetUtils.get(urlEncoding));
			httpPost.setEntity(entityBuilder.build());
			request = httpPost;
			
			// 设置 header 为外围传入cookie值准备
			Header headerss[] = buildHeader(paramsMapHeader);
			if (headerss != null && headerss.length > 0) {
				request.setHeaders(headerss);
			}
			
			response = client.execute(request);
			CookieStore cookieStore = setCookieStore(response);
			// 响应状态
			StatusLine statusLine = response.getStatusLine();
			// 获取响应对象
			entity = response.getEntity();
			ResponseContent ret = new ResponseContent();
			ret.setStatusCode(statusLine.getStatusCode());
			ret.setCookieStore(cookieStore);
			getResponseContent(entity, ret);
			return ret;
		} finally {
			close(entity, request, response);
		}
	}

	private void close(HttpEntity entity, HttpRequestBase request,
			CloseableHttpResponse response) throws IOException {
		if (request != null)
			request.releaseConnection();
		if (entity != null)
			entity.getContent().close();
		if (response != null)
			response.close();
	}

	public NameValuePair[] getNVBodyArray() {
		List<NameValuePair> list = this.getNVBodies();
		if (list == null || list.isEmpty())
			return null;
		NameValuePair[] nvps = new NameValuePair[list.size()];
		Iterator<NameValuePair> it = list.iterator();
		int count = 0;
		while (it.hasNext()) {
			NameValuePair nvp = it.next();
			nvps[count++] = nvp;
		}
		return nvps;
	}

	public List<NameValuePair> getNVBodies() {
		return Collections.unmodifiableList(this.nameValuePostBodies);
	}

	private String getResponseContentType(HttpEntity method) {
		Header contenttype = method.getContentType();
		if (contenttype == null)
			return null;
		String ret = null;
		try {
			HeaderElement[] hes = contenttype.getElements();
			if (hes != null && hes.length > 0) {
				ret = hes[0].getName();
			}
		} catch (Exception e) {
		}
		return ret;
	}

	private String getResponseContentTypeString(HttpEntity method) {
		Header contenttype = method.getContentType();
		if (contenttype == null)
			return null;
		return contenttype.getValue();
	}

	static Set<Character> BEING_ESCAPED_CHARS = new HashSet<Character>();
	static {
		char[] signArray = { ' ', '\\', '‘', ']', '!', '^', '#', '`', '$', '{',
				'%', '|', '}', '(', '+', ')', '<', '>', ';', '[' };
		for (int i = 0; i < signArray.length; i++) {
			BEING_ESCAPED_CHARS.add(new Character(signArray[i]));
		}
	}

	public static String encodeURL(String url, String encoding) {
		if (url == null)
			return null;
		if (encoding == null)
			return url;

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < url.length(); i++) {
			char c = url.charAt(i);
			if (c == 10) {
				continue;
			} else if (BEING_ESCAPED_CHARS.contains(new Character(c))
					|| c == 13 || c > 126) {
				try {
					sb.append(URLEncoder.encode(String.valueOf(c), encoding));
				} catch (Exception e) {
					sb.append(c);
				}
			} else {
				sb.append(c);
			}
		}
		return sb.toString().replaceAll("\\+", "%20");
	}

	private String protocol;
	private String host;
	private int port;
	private String dir;
	private String uri;
	private final static int DefaultPort = 80;
	private final static String ProtocolSeparator = "://";
	private final static String PortSeparator = ":";
	private final static String HostSeparator = "/";
	private final static String DirSeparator = "/";

	private void parseUrl(String url) {
		this.protocol = null;
		this.host = null;
		this.port = DefaultPort;
		this.dir = "/";
		this.uri = dir;

		if (url == null || url.length() == 0)
			return;
		String u = url.trim();
		boolean MeetProtocol = false;
		int pos = u.indexOf(ProtocolSeparator);
		if (pos > 0) {
			MeetProtocol = true;
			this.protocol = u.substring(0, pos);
			pos += ProtocolSeparator.length();
		}
		int posStartDir = 0;
		if (MeetProtocol) {
			int pos2 = u.indexOf(PortSeparator, pos);
			int pos21 = u.indexOf(HostSeparator, pos);// 如果参数有日期格式（2015-04-20
														// 18:27:17）需要做此判断
			if (pos2 > 0 && pos21 > pos2) {
				this.host = u.substring(pos, pos2);
				pos2 = pos2 + PortSeparator.length();
				int pos3 = u.indexOf(HostSeparator, pos2);
				String PortStr = null;
				if (pos3 > 0) {
					PortStr = u.substring(pos2, pos3);
					posStartDir = pos3;
				} else {
					int pos4 = u.indexOf("?");
					if (pos4 > 0) {
						PortStr = u.substring(pos2, pos4);
						posStartDir = -1;
					} else {
						PortStr = u.substring(pos2);
						posStartDir = -1;
					}
				}
				try {
					this.port = Integer.parseInt(PortStr);
				} catch (Exception e) {
				}
			} else {
				pos2 = u.indexOf(HostSeparator, pos);
				if (pos2 > 0) {
					this.host = u.substring(pos, pos2);
					posStartDir = pos2;
				} else {
					this.host = u.substring(pos);
					posStartDir = -1;
				}
			}

			pos = u.indexOf(HostSeparator, pos);
			pos2 = u.indexOf("?");
			if (pos > 0 && pos2 > 0) {
				this.uri = u.substring(pos, pos2);
			} else if (pos > 0 && pos2 < 0) {
				this.uri = u.substring(pos);
			}
		}

		if (posStartDir >= 0) {
			int pos2 = u.lastIndexOf(DirSeparator, posStartDir);
			if (pos2 > 0) {
				this.dir = u.substring(posStartDir, pos2 + 1);
			}
		}

	}

	private String toUrl() {
		StringBuffer ret = new StringBuffer();
		if (this.protocol != null) {
			ret.append(this.protocol);
			ret.append(ProtocolSeparator);
			if (this.host != null)
				ret.append(this.host);
			if (this.port != DefaultPort) {
				ret.append(PortSeparator);
				ret.append(this.port);
			}
		}
		ret.append(this.uri);
		return ret.toString();
	}

	public void addNV(String name, String value) {
		BasicNameValuePair nvp = new BasicNameValuePair(name, value);
		this.nameValuePostBodies.add(nvp);
	}

	public void clearNVBodies() {
		this.nameValuePostBodies.clear();
	}

	public List<ContentBody> getContentBodies() {
		return contentBodies;
	}

	public void printResponse(HttpResponse httpResponse) throws ParseException,
			IOException {
//		// 获取响应消息实体
//		HttpEntity entity = httpResponse.getEntity();
//		// 响应状态
//		System.out.println("status:" + httpResponse.getStatusLine());
//		System.out.println("headers:");
//		HeaderIterator iterator = httpResponse.headerIterator();
//		while (iterator.hasNext()) {
//			System.out.println("\t" + iterator.next());
//		}
//		
//		
//		// 判断响应实体是否为空
//		// if (entity != null) {
//		// String responseString = EntityUtils.toString(entity);
//		// System.out.println("response length:" + responseString.length());
//		// System.out.println("response content:"
//		// + responseString.replace("\r\n", ""));
//		// }
	}

	/**
	 * 组装请求头
	 * 
	 * @param params
	 * @return
	 */
	public static Header[] buildHeader(IdentityHashMap<String, Object> params) {
		Header[] headers = null;
		if (params != null && params.size() > 0) {
			headers = new BasicHeader[params.size()];
			int i = 0;
			for (Entry<String, Object> entry : params.entrySet()) {
				headers[i] = new BasicHeader(entry.getKey(), (String) entry.getValue());
				i++;
			}
		}
		return headers;
	}

	public static CookieStore setCookieStore(HttpResponse httpResponse) {
		cookieStore = new BasicCookieStore();
		HeaderIterator it = httpResponse.headerIterator("Set-Cookie");
		String JSESSIONID = null;
		String SPRING_SECURITY_REMEMBER_ME_COOKIE = null;
		while (it.hasNext()) {
			Header cookieheader = (Header) it.next();
			String cookiestr = cookieheader.getValue();
			if(cookiestr.contains("JSESSIONID")){
				JSESSIONID = cookiestr.substring("JSESSIONID=".length(),
						cookiestr.indexOf(";"));
			}
			if(cookiestr.contains("SPRING_SECURITY_REMEMBER_ME_COOKIE")){
				SPRING_SECURITY_REMEMBER_ME_COOKIE = cookiestr.substring("SPRING_SECURITY_REMEMBER_ME_COOKIE=".length(),
						cookiestr.indexOf(";"));
			}
		}
		
//		System.out.println("JSESSIONID="+JSESSIONID);
//		System.out.println("SPRING_SECURITY_REMEMBER_ME_COOKIE="+SPRING_SECURITY_REMEMBER_ME_COOKIE);		
		// 新建一个Cookie
		BasicClientCookie cookie1 = new BasicClientCookie("JSESSIONID",
				JSESSIONID);
		BasicClientCookie cookie2 = new BasicClientCookie("SPRING_SECURITY_REMEMBER_ME_COOKIE",
				SPRING_SECURITY_REMEMBER_ME_COOKIE);

		cookieStore.addCookie(cookie1);
		cookieStore.addCookie(cookie2);
		return cookieStore;
	}
}
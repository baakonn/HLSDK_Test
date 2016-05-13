package com.ihuilian.volley;

import android.webkit.MimeTypeMap;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.ihuilian.util.FileUtil;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.HTTP;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 * @author linjinfa 331710168@qq.com
 * @version 创建时间：2014年8月11日 上午10:42:37
 */
public class VolleyHttpStack implements HttpStack {

	private final SSLSocketFactory mSslSocketFactory;

	/**
	 * @param sslSocketFactory
	 *            SSL factory to use for HTTPS connections
	 */
	public VolleyHttpStack(SSLSocketFactory sslSocketFactory) {
		mSslSocketFactory = sslSocketFactory;
	}

	public VolleyHttpStack() {
		this(null);
	}

	@Override
	public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders) throws IOException, AuthFailureError {
		if (!(request instanceof GsonRequest) || ((GsonRequest<?>) request).getUploadFileList() == null) {
			return new HurlStack().performRequest(request, additionalHeaders);
		}

		String url = request.getUrl();
		HashMap<String, String> map = new HashMap<String, String>();
		map.putAll(request.getHeaders());
		map.putAll(additionalHeaders);
		URL parsedUrl = new URL(url);
		HttpURLConnection connection = openConnection(parsedUrl, request);
		for (String headerName : map.keySet()) {
			connection.addRequestProperty(headerName, map.get(headerName));
		}

		connection.setRequestMethod("POST");
		setConnectionParametersForRequest(connection, request);

		// Initialize HttpResponse with data from the HttpURLConnection.
		ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
		int responseCode = connection.getResponseCode();
		if (responseCode == -1) {
			// -1 is returned by getResponseCode() if the response code could
			// not be retrieved.
			// Signal to the caller that something was wrong with the
			// connection.
			throw new IOException("Could not retrieve response code from HttpUrlConnection.");
		}
		StatusLine responseStatus = new BasicStatusLine(protocolVersion, connection.getResponseCode(), connection.getResponseMessage());
		BasicHttpResponse response = new BasicHttpResponse(responseStatus);
		response.setEntity(entityFromConnection(connection));
		for (Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
			if (header.getKey() != null) {
				Header h = new BasicHeader(header.getKey(), header.getValue().get(0));
				response.addHeader(h);
			}
		}
		return response;
	}

	/**
	 * Opens an {@link HttpURLConnection} with parameters.
	 * 
	 * @param url
	 * @return an open connection
	 * @throws IOException
	 */
	private HttpURLConnection openConnection(URL url, Request<?> request) throws IOException {
		HttpURLConnection connection = createConnection(url);

		int timeoutMs = request.getTimeoutMs();
		connection.setConnectTimeout(timeoutMs);
		connection.setReadTimeout(timeoutMs);
		connection.setUseCaches(false);
		connection.setDoInput(true);

		// use caller-provided custom SslSocketFactory, if any, for HTTPS
		if ("https".equals(url.getProtocol()) && mSslSocketFactory != null) {
			((HttpsURLConnection) connection).setSSLSocketFactory(mSslSocketFactory);
		}

		return connection;
	}

	/**
	 * Create an {@link HttpURLConnection} for the specified {@code url}.
	 */
	protected HttpURLConnection createConnection(URL url) throws IOException {
		return (HttpURLConnection) url.openConnection();
	}

	/**
	 * 设置参数
	 * 
	 * @param connection
	 * @param request
	 * @throws AuthFailureError
	 * @throws IOException
	 */
	private void setConnectionParametersForRequest(HttpURLConnection connection, Request<?> request) throws AuthFailureError, IOException {
		switch (request.getMethod()) {
		case Method.DEPRECATED_GET_OR_POST:
		case Method.POST:
			connection.setRequestMethod("POST");
			wirteParameter(connection, request);
			break;
		case Method.PUT:
			connection.setRequestMethod("PUT");
			wirteParameter(connection, request);
			break;
		case Method.PATCH:
			connection.setRequestMethod("PATCH");
			wirteParameter(connection, request);
			break;
		default:
			throw new IllegalStateException("上传文件不支持: " + request.getMethod());
		}
	}

	/**
	 * 写入参数及上传的文件
	 * 
	 * @param connection
	 * @param request
	 * @throws AuthFailureError
	 * @throws IOException
	 */
	private void wirteParameter(HttpURLConnection connection, Request<?> request) throws AuthFailureError, IOException {
		final String BOUNDARY = UUID.randomUUID().toString();
		final String PREFIX = "--";
		final String LINE_END = "\r\n";

		GsonRequest<?> gsonRequest = (GsonRequest<?>) request;
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false); // 不允许使用缓存

		connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
		OutputStream os = connection.getOutputStream();
		StringBuffer paramSb = new StringBuffer();
		if(gsonRequest.getParams()!=null){
			for(Entry<String, String> entry : gsonRequest.getParams().entrySet()){
				if(entry.getValue()!=null && !"".equals(entry.getValue())){
					paramSb.append(PREFIX).append(BOUNDARY).append(LINE_END);
					paramSb.append("Content-Disposition:form-data; name=\""+entry.getKey()+"\"");
					paramSb.append(LINE_END).append(LINE_END);
					paramSb.append(entry.getValue());
					paramSb.append(LINE_END);
				}
			}
		}
		os.write(paramSb.toString().getBytes());

		
		// 上传文件
		List<FormFile> uploadFileList = gsonRequest.getUploadFileList();
		for(FormFile formFile : uploadFileList){
			String mimeType = null;
			String extension = FileUtil.getExtensionName(formFile.getUploadFile().getName());
			mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
			if(mimeType == null){
				mimeType = "application/octet-stream";
			}
			StringBuffer fileParamSb = new StringBuffer();
			//写入文件参数
			fileParamSb.append(PREFIX).append(BOUNDARY).append(LINE_END);
			fileParamSb.append("Content-Disposition:form-data; name=\"" + URLEncoder.encode(formFile.getFileParamName(), HTTP.UTF_8) + "\"; filename=\""
					+ URLEncoder.encode(formFile.getUploadFile().getName(), HTTP.UTF_8) + "\"" + "; mime=\""+mimeType+"\"" +LINE_END);
			fileParamSb.append(LINE_END);
			os.write(fileParamSb.toString().getBytes());
			
			//写入文件
			InputStream is = new FileInputStream(formFile.getUploadFile());
			byte[] bytes = new byte[1024];
			int len = 0;
			while ((len = is.read(bytes)) != -1) {
				os.write(bytes, 0, len);
			}
			is.close();

			os.write(LINE_END.getBytes());
		}

		byte[] endData = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
		os.write(endData);

		os.flush();
		os.close();
	}

	/**
	 * Initializes an {@link HttpEntity} from the given
	 * {@link HttpURLConnection}.
	 * 
	 * @param connection
	 * @return an HttpEntity populated with data from <code>connection</code>.
	 */
	private HttpEntity entityFromConnection(HttpURLConnection connection) {
		BasicHttpEntity entity = new BasicHttpEntity();
		InputStream inputStream;
		try {
			inputStream = connection.getInputStream();
		} catch (IOException ioe) {
			inputStream = connection.getErrorStream();
		}
		entity.setContent(inputStream);
		entity.setContentLength(connection.getContentLength());
		entity.setContentEncoding(connection.getContentEncoding());
		entity.setContentType(connection.getContentType());
		return entity;
	}

}

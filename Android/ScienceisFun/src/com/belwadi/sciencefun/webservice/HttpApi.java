package com.belwadi.sciencefun.webservice;

import java.io.ByteArrayOutputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.util.Log;

public class HttpApi {
	private static final String LOG = "ScienceIsFun.HttpApi";
	private static CookieStore cookieStore = null;

	public static DefaultHttpClient defaultHttpClient() {
		BasicHttpParams basicHttpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(basicHttpParams, 35000);
		HttpConnectionParams.setSoTimeout(basicHttpParams, 35000);
		HttpConnectionParams.setSocketBufferSize(basicHttpParams, 8192);
		HttpClientParams.setRedirecting(basicHttpParams, true);
		return new DefaultHttpClient(basicHttpParams);
	}

	public static String sendXml(String xmldata, String url, String xml) {
		HttpParams params = null;
		if (xml != null) {
			params = new HttpParams();
			params.addParam(xmldata, xml);
		}

		return HttpApi.sendRequest(url, params, null);
	}

	public static String sendRequest(String serverUrl, HttpParams params,
			int[] statusCode) {
		HttpPost httpRequest = new HttpPost(serverUrl);
		int status = -1;

		try {
			/** Makes an HTTP request request */
			if (params != null) {
				httpRequest.setEntity(new UrlEncodedFormEntity(params
						.getParams(), HTTP.UTF_8));
			}

			/** Create an HTTP client */
			//DefaultHttpClient httpClient = new DefaultHttpClient();
			DefaultHttpClient httpClient = defaultHttpClient();

			/** Set Cookie information */
			if (cookieStore != null) {
				httpClient.setCookieStore(cookieStore);
			}

			/** Gets the HTTP response response */
			HttpResponse httpresponse = httpClient.execute(httpRequest);

			status = httpresponse.getStatusLine().getStatusCode();

			/** If the status code 200 response successfully */
			Log.v(LOG, "resStatus: " + Integer.toString(status) + " , URL : " + serverUrl);
			if (status == 200) {
				/** Remove the response string */
				String strResponse = EntityUtils.toString(
						httpresponse.getEntity(), HTTP.UTF_8);
				Log.v(LOG, strResponse);
				if (statusCode != null)
					statusCode[0] = status;

				cookieStore = httpClient.getCookieStore();
				return strResponse.trim();
			}
		} catch (Exception e) {
			Log.v(LOG, "send request error, URL : " + serverUrl);
			status = -1;
		}

		if (statusCode != null)
			statusCode[0] = status;
		return null;
	}

	@SuppressWarnings("deprecation")
	public static String sendRequestWithImage(String serverUrl, HttpParams params, Bitmap bm,
			int[] statusCode) {
		HttpPost httpRequest = new HttpPost(serverUrl);
		int status = -1;

		try {
			/** Makes an HTTP request request */
			//			if (params != null) {
			//				httpRequest.setEntity(new UrlEncodedFormEntity(params
			//						.getParams(), HTTP.UTF_8));
			//			}

			MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			if (params != null) {
				//				entity.addPart("name", new StringBody("testname"));
				for(int index=0; index < params.getParams().size(); index++) {
					// Normal string data
					entity.addPart(params.getParams().get(index).getName(), new StringBody(params.getParams().get(index).getValue()));
				}
			}
			if (bm != null) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				bm.compress(CompressFormat.JPEG, 75, bos);

				byte[] data = bos.toByteArray();
				ByteArrayBody bab = new ByteArrayBody(data, "image/jpeg", "photo.jpg");

				entity.addPart(Constants.PHOTO, bab);
			}

			httpRequest.setEntity(entity);

			/** Create an HTTP client */
			//DefaultHttpClient httpClient = new DefaultHttpClient();
			DefaultHttpClient httpClient = defaultHttpClient();

			/** Set Cookie information */
			if (cookieStore != null) {
				httpClient.setCookieStore(cookieStore);
			}

			/** Gets the HTTP response response */
			HttpResponse httpresponse = httpClient.execute(httpRequest);

			status = httpresponse.getStatusLine().getStatusCode();

			/** If the status code 200 response successfully */
			Log.v(LOG, "resStatus: " + Integer.toString(status) + " , URL : " + serverUrl);
			if (status == 200) {
				/** Remove the response string */
				String strResponse = EntityUtils.toString(
						httpresponse.getEntity(), HTTP.UTF_8);
				Log.v(LOG, strResponse);
				if (statusCode != null)
					statusCode[0] = status;

				cookieStore = httpClient.getCookieStore();
				return strResponse.trim();
			}
		} catch (Exception e) {
			Log.v(LOG, "send request error, URL : " + serverUrl);
			status = -1;
		}

		if (statusCode != null)
			statusCode[0] = status;
		return null;
	}

	public static String sendPostRequestWithDataHeader(String serverUrl, HttpHeaders headers, 
			int[] statusCode) {
		HttpPost httpRequest = new HttpPost(serverUrl);
		int status = -1;

		try {
			/** Makes an HTTP request request */
			if (headers != null) {
				for (NameValuePair header : headers.getHeaders())
				{
					String field = header.getName();
					String value = header.getValue();
					//					byte[] encodeValue = value.getBytes();
					//					httpRequest.addHeader(field, Base64.encodeToString(encodeValue, Base64.URL_SAFE));
					httpRequest.addHeader(field, value);
				}

				//				List<NameValuePair> params = new ArrayList<NameValuePair>();
				//				
				//				httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			}

			/** Create an HTTP client */
			//DefaultHttpClient httpClient = new DefaultHttpClient();
			DefaultHttpClient httpClient = defaultHttpClient();

			/** Set Cookie information */
			if (cookieStore != null) {
				httpClient.setCookieStore(cookieStore);
			}

			/** Gets the HTTP response response */
			HttpResponse httpresponse = httpClient.execute(httpRequest);

			status = httpresponse.getStatusLine().getStatusCode();

			/** If the status code 200 response successfully */
			Log.v(LOG, "resStatus: " + Integer.toString(status) + " , URL : " + serverUrl);
			if (status == 200) {
				/** Remove the response string */
				String strResponse = EntityUtils.toString(
						httpresponse.getEntity(), HTTP.UTF_8);
				Log.v(LOG, strResponse);
				if (statusCode != null)
					statusCode[0] = status;

				cookieStore = httpClient.getCookieStore();
				return strResponse.trim();
			}
		} catch (Exception e) {
			Log.v(LOG, "send request error, URL : " + serverUrl);
			status = -1;
		}

		if (statusCode != null)
			statusCode[0] = status;
		return null;
	}

	public static String sendGetRequest(String serverUrl, HttpParams params,
			int[] statusCode) {

		String url = serverUrl;
		if (params != null)
			url += URLEncodedUtils.format(params.getParams(), HTTP.UTF_8);
		HttpGet httpRequest = new HttpGet(url);
		int status = -1;

		try {

			/** Create an HTTP client */
			//DefaultHttpClient httpClient = new DefaultHttpClient();
			DefaultHttpClient httpClient = defaultHttpClient();

			/** Set Cookie information */
			if (cookieStore != null) {
				httpClient.setCookieStore(cookieStore);
			}

			/** Gets the HTTP response response */
			HttpResponse httpresponse = httpClient.execute(httpRequest);

			status = httpresponse.getStatusLine().getStatusCode();

			/** If the status code 200 response successfully */
			Log.v(LOG, "resStatus: " + Integer.toString(status) + " , URL : " + serverUrl);
			if (status == 200) {
				/** Remove the response string */
				String strResponse = EntityUtils.toString(
						httpresponse.getEntity(), HTTP.UTF_8);
				Log.v(LOG, strResponse);
				if (statusCode != null)
					statusCode[0] = status;

				cookieStore = httpClient.getCookieStore();
				return strResponse.trim();
			}
		} catch (Exception e) {
			Log.v(LOG, "send request error");
			status = -1;
		}

		if (statusCode != null)
			statusCode[0] = status;
		return null;
	}

	public static String sendHttpsGetRequest(String serverUrl, HttpParams params) {
		String url = serverUrl;
		if (params != null)
			url += URLEncodedUtils.format(params.getParams(), HTTP.UTF_8);
		HttpGet httpRequest = new HttpGet(url);
		int status = -1;

		try {

			/** Create an HTTP client */
			//DefaultHttpClient httpClient = new DefaultHttpClient();
			DefaultHttpClient httpClient = defaultHttpClient();
			DefaultHttpClient httpsClient = sslClient(httpClient);

			/** Set Cookie information */
			if (cookieStore != null) {
				httpsClient.setCookieStore(cookieStore);
			}

			/** Gets the HTTP response response */
			HttpResponse httpresponse = httpsClient.execute(httpRequest);

			status = httpresponse.getStatusLine().getStatusCode();

			/** If the status code 200 response successfully */
			Log.v(LOG, "resStatus: " + Integer.toString(status) + " , URL : " + serverUrl);
			if (status == 200) {
				/** Remove the response string */
				String strResponse = EntityUtils.toString(
						httpresponse.getEntity(), HTTP.UTF_8);
				Log.v(LOG, strResponse);

				cookieStore = httpsClient.getCookieStore();
				return strResponse.trim();
			}
		} catch (Exception e) {
			Log.v(LOG, "send request error, URL : " + serverUrl);
			status = -1;
		}

		return null;

	}


	private static DefaultHttpClient sslClient(DefaultHttpClient client) {
		try {
			X509TrustManager tm = new X509TrustManager() { 
				public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};
			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(null, new TrustManager[]{tm}, null);
			SSLSocketFactory ssf = new MySSLSocketFactory(ctx);
			ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			ClientConnectionManager ccm = client.getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
			sr.register(new Scheme("https", ssf, 443));
			return new DefaultHttpClient(ccm, client.getParams());
		} catch (Exception ex) {
			return null;
		}
	}



	public static void clearCookie() {
		if (cookieStore != null)
			cookieStore.clear();
		cookieStore = null;
	}	

}

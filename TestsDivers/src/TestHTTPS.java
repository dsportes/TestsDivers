import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class TestHTTPS {
	public static void initSSL() {
		TrustManager tm = new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
            public void checkClientTrusted(X509Certificate[] certs, String authType) { }
            public void checkServerTrusted(X509Certificate[] certs, String authType) { }
        };
		 // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { tm };
        // Install the all-trusting trust manager
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
		} catch (Exception e) {	}
        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	}

	private static final String url = "https://test.sportes.fr";
//	private static final String url = "https://test.sportes.fr:8443/ac/alterconsos/ping";

	private static final String ua = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.109 Safari/537.36";

	private static String get() throws Exception {
		HttpURLConnection connection;
		connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded"); 
		connection.setRequestProperty("User-Agent", ua); 
		connection.setDoOutput(false);
		connection.setDoInput(true);
		connection.setRequestMethod("GET");
		int status = connection.getResponseCode();
		if (status != 200)
			throw new Exception("HTTP status = " + status);
		InputStream is = connection.getInputStream();
		byte[] buf = new byte[4096];
		ByteArrayOutputStream os2 = new ByteArrayOutputStream(16192);
		int l = 0;
		while ((l = is.read(buf)) > 0)
			os2.write(buf, 0, l);
		is.close();
		byte[] res = os2.toByteArray();
		os2.close();
		return new String(res, "UTF-8");
	}
	
	public static void main(String[] args) {
		try {
			initSSL();
			String res = get();
			System.out.println(res);
		} catch(Throwable t){
			t.printStackTrace();
		}
	}
}

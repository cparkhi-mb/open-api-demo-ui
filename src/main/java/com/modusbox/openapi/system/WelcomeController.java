package com.modusbox.openapi.system;

import java.io.File;
import java.io.IOException;

import javax.net.ssl.SSLContext;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;


@Controller
class WelcomeController {

	@GetMapping("/")
	public String welcome() {
		return "welcome";
	}

	@RequestMapping("/redirect-to-acme")
	public String routeToLogin() throws IOException {
		System.out.println("Inside controller");
		return "redirect:https://localhost:8082/external/authorize?scope=READ&response_type=code&client_id=ab88dc6cacb1417cbdd63dec28842fdb&redirect_uri=http://localhost:8080/auth";
	}

	@RequestMapping("/auth")
	public ModelAndView auth(@RequestParam(name = "code") String code, ModelAndView mav) {
		System.out.println("Code received " + code);
		mav.addObject("authCode", code);
		mav.setViewName("api");
		return mav;
	}

	@RequestMapping("/getGadget")
	public @ResponseBody String getGadget(@RequestBody Object data, ModelMap m) {
		return "{\"access_token\":" + "\"" + getAccessToken(data.toString()) + "\"," + "\"method\":\"GET\", \"path\":\"" + "https://localhost:9999/gadget/1" + "\"}" ;
	}

	@RequestMapping("/fakeApi")
	public  @ResponseBody String fakeAPi(@RequestBody Object data) {
		return "{\"code\":\"value\"}" ;
	}
	
	@SuppressWarnings("deprecation")
	private String getAccessToken(String auth) {
		try {
			
			// Trust own CA and all self-signed certs
	        SSLContext sslcontext = SSLContexts.custom()
	                .loadTrustMaterial(new File("src/main/resources/keystore.jks"), "mule123".toCharArray(),
	                        new TrustSelfSignedStrategy())
	                .build();
	        // Allow TLSv1 protocol only
	        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
	                sslcontext,
	                new String[] { "TLSv1","TLSv1.1","TLSv1.2" },
	                null,
	                SSLConnectionSocketFactory.getDefaultHostnameVerifier());
			
	        CloseableHttpClient client = HttpClients.custom()
	                .setSSLSocketFactory(sslsf)
	                .build();
			HttpPost httpPost = new HttpPost("https://localhost:8082/external/access_token");

			httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
			httpPost.setHeader("Authorization", "Basic YWI4OGRjNmNhY2IxNDE3Y2JkZDYzZGVjMjg4NDJmZGI6NzVjZTA5ODgzZGYxNEYwRDhjQjM2Rjg4ZWYzRjk4QzQK");
			auth = java.net.URLEncoder.encode(auth);
			String payload = "{\"grant_type\":\"authorization_code\",\"redirect_uri\":\"http://localhost:8080/api\",\"code\":\"" + auth + "\"}";
			httpPost.setEntity(new StringEntity(payload));
			CloseableHttpResponse response = client.execute(httpPost);
			//assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
			client.close();
			
			//return response.getStatusLine().getReasonPhrase();
			return "access_token";

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "failure";
	}
}

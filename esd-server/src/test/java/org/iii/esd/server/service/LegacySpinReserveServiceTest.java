package org.iii.esd.server.service;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import org.iii.esd.api.RestConstants;
import org.iii.esd.api.request.taipower.SpinReserveNoticeResquest;
import org.iii.esd.api.request.taipower.SpinReserveResquest;
import org.iii.esd.server.AbstractTest;
import org.iii.esd.thirdparty.service.HttpService;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@RunWith(JUnitPlatform.class)
//@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = LegacySpinReserveServiceTest.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@EnableAutoConfiguration
@Log4j2
public class LegacySpinReserveServiceTest extends AbstractTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    @Autowired
    private HttpService httpService;
    @Value("${url}")
    private String url;

    @Test
    public void testAlert() throws Exception {
        //log.info("testAlert");
        SpinReserveResquest req = new SpinReserveResquest(1l, 1);
        mockExecute(RestConstants.REST_TAIPOWER_SPINRESERVE_ALERT, req);

        //		HttpHeaders headers = new HttpHeaders();
        //		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        //
        //		String url = url.concat(RestConstants.REST_TAIPOWER_SPINRESERVE_NOTICE);
        //		ApiResponse res= getTrustSSLTemplate().exchange(url, HttpMethod.POST, new HttpEntity<SpinReserveResquest>(req, headers), SuccessfulResponse.class).getBody();
        //		log.info(res);
        //		ApiResponse res2 = httpService.jsonPost(url, req, SuccessfulResponse.class);
        //		log.info(res2);
    }

    //@Test
    public void testEvent() {
        log.info("testEvent");
    }

    @Test
    public void testNotice() throws Exception {
        //		log.info("testNotice");
        mockExecute(RestConstants.REST_TAIPOWER_SPINRESERVE_NOTICE,
                new SpinReserveNoticeResquest(1l, 1590574507l, 1590574507l, 1590574507l, 60, 9900));
    }

    @BeforeEach
        //@BeforeAll
    void setupMockMvc() throws Exception {
        log.info("------");
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private <T> void mockExecute(String url, T req) throws Exception {
        log.info(url);
        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsString(req)))
               .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
               .andExpect(jsonPath("$.status", is("ok")));
    }

    private RestTemplate getTrustSSLTemplate() {

        //		TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
        //			public boolean isTrusted(X509Certificate[] certificate, String authType) {
        //				return true;
        //			}
        //		};
        //		SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        //		SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new HostnameVerifier() {
        //			public boolean verify(String hostname, SSLSession session) {
        //				return true;
        //			}
        //		});
        //		return new RestTemplate(new HttpComponentsClientHttpRequestFactory(HttpClients.custom().setSSLSocketFactory(csf).build()));

        try {
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, (certificate, authType) -> {return true;}).build();
            SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, (hostname, session) -> {return true;});
            return new RestTemplate(new HttpComponentsClientHttpRequestFactory(HttpClients.custom().setSSLSocketFactory(csf).build()));
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            log.error(e.toString());
            return new RestTemplate();
        }

        //		try {
        //            SSLContext sslContext = SSLContextBuilder.create().setProtocol(SSLConnectionSocketFactory.SSL).loadTrustMaterial((x, y) -> true).build();
        //            RequestConfig config = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(5000).build();
        //            CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).setSSLContext(sslContext).setSSLHostnameVerifier((x, y) -> true).build();
        //            return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
        //        } catch (Exception e) {
        //			log.error(e.toString());
        //			return new RestTemplate();
        //        }

    }

}
package quickfix.examples.executor;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

public class HttpClient {
    public void demoGetRESTAPI() throws Exception {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        try {
            //Define a HttpGet request; You can choose between HttpPost, HttpDelete or HttpPut also.
            //Choice depends on type of method you will be invoking.
            HttpGet getRequest = new HttpGet("http://127.0.0.1:5000/api/v1/trades");
            getRequest.addHeader("custom-key", "mkyong");
            getRequest.addHeader(HttpHeaders.USER_AGENT, "Googlebot");

//            CloseableHttpResponse response = httpClient.execute(request);
            //Set the API media type in http accept header
            getRequest.addHeader("accept", "application/json");

            //Send the request; It will immediately return the response in HttpResponse object
            HttpResponse response = httpClient.execute(getRequest);

            //verify the valid error code first
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new RuntimeException("Failed with HTTP error code : " + statusCode);
            }

            //Now pull back the response object
            HttpEntity httpEntity = response.getEntity();
            String apiOutput = EntityUtils.toString(httpEntity);

            //Lets see what we got from API
            System.out.println(apiOutput);
            Gson gson = new Gson();

            Trade[] mcArray = gson.fromJson(apiOutput, Trade[].class);
            List<Trade> mcList = Arrays.asList(mcArray);
            System.out.println(mcList);

        } finally {
            //Important: Close the connect
            httpClient.getConnectionManager().shutdown();
        }
    }

    public class Trade {
        private String type;
        private Integer id;
        private Attributes attributes;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Attributes getAttributes() {
            return attributes;
        }

        public void setAttributes(Attributes attributes) {
            this.attributes = attributes;
        }

        @Override
        public String toString() {
            return "Trade{" +
                    "type='" + type + '\'' +
                    ", id=" + id +
                    ", attributes=" + attributes +
                    '}';
        }
    }

    class Attributes{
        private String symbol;
        private String created_at;
        private Integer roster_id;

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getCreated_at() {
            return created_at;
        }

        public void setCreated_at(String created_at) {
            this.created_at = created_at;
        }

        public Integer getRoster_id() {
            return roster_id;
        }

        public void setRoster_id(Integer roster_id) {
            this.roster_id = roster_id;
        }

        @Override
        public String toString() {
            return "Attributes{" +
                    "symbol='" + symbol + '\'' +
                    ", created_at='" + created_at + '\'' +
                    ", roster_id=" + roster_id +
                    '}';
        }
    }

    public static void main(String[] args) throws Exception {
        HttpClient client = new HttpClient();
        client.demoGetRESTAPI();
    }
}

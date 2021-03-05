package quickfix.examples.executor;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.util.List;

public class HttpClient {
    public static List<LinkedTreeMap> demoGetRESTAPI() throws Exception {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        try {
            //Define a HttpGet request; You can choose between HttpPost, HttpDelete or HttpPut also.
            //Choice depends on type of method you will be invoking.
            HttpGet getRequest = new HttpGet("http://127.0.0.1:5000/dev-api/v1/trades");
//            getRequest.addHeader("custom-key", "mkyong");
//            getRequest.addHeader(HttpHeaders.USER_AGENT, "Googlebot");

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

            LinkedTreeMap result = gson.fromJson(apiOutput , LinkedTreeMap.class);
            List<LinkedTreeMap> list= (List<LinkedTreeMap>) result.get("trades");
            return  list;
//            for(LinkedTreeMap tree:list){
//                System.out.println(tree.get("id"));
//                System.out.println(tree.get("symbol"));
//                System.out.println(tree.get("strikePrice"));
//                System.out.println(tree.get("lowLimitPrice"));
//                System.out.println(tree.get("highLimitPrice"));
//                System.out.println(tree.get("tradingReferencePrice"));
//                System.out.println(tree.get("securityID"));
//                System.out.println(tree.get("cfiCode"));
//                System.out.println(tree.get("activationDate"));
//                System.out.println(tree.get("lastEligibleTradeDate"));
//            }
////            HashMap<String, Trade> fields = gson.fromJson(apiOutput, HashMap.class);
//            System.out.println(list);

//            Trade[] mcArray = gson.fromJson(apiOutput, Trade[].class);
//            List<Trade> mcList = Arrays.asList(mcArray);
//            System.out.println(mcList);

        } finally {
            //Important: Close the connect
            httpClient.getConnectionManager().shutdown();
        }
    }

    public class Trade {
        private Integer id;
        private String symbol ;
        private Float strikePrice ;
        private Float lowLimitPrice ;
        private Float highLimitPrice ;
        private Float tradingReferencePrice;
        private String securityID;
        private String cfiCode ;
        private String activationDate;
        private String lastEligibleTradeDate;

        @Override
        public String toString() {
            return "Trade{" +
                    ", id=" + id +
                    ", symbol='" + symbol + '\'' +
                    ", strikePrice=" + strikePrice +
                    ", lowLimitPrice=" + lowLimitPrice +
                    ", highLimitPrice=" + highLimitPrice +
                    ", tradingReferencePrice=" + tradingReferencePrice +
                    ", securityID='" + securityID + '\'' +
                    ", cfiCode='" + cfiCode + '\'' +
                    ", activationDate='" + activationDate + '\'' +
                    ", lastEligibleTradeDate='" + lastEligibleTradeDate + '\'' +
                    '}';
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public Float getStrikePrice() {
            return strikePrice;
        }

        public void setStrikePrice(Float strikePrice) {
            this.strikePrice = strikePrice;
        }

        public Float getLowLimitPrice() {
            return lowLimitPrice;
        }

        public void setLowLimitPrice(Float lowLimitPrice) {
            this.lowLimitPrice = lowLimitPrice;
        }

        public Float getHighLimitPrice() {
            return highLimitPrice;
        }

        public void setHighLimitPrice(Float highLimitPrice) {
            this.highLimitPrice = highLimitPrice;
        }

        public Float getTradingReferencePrice() {
            return tradingReferencePrice;
        }

        public void setTradingReferencePrice(Float tradingReferencePrice) {
            this.tradingReferencePrice = tradingReferencePrice;
        }

        public String getSecurityID() {
            return securityID;
        }

        public void setSecurityID(String securityID) {
            this.securityID = securityID;
        }

        public String getCfiCode() {
            return cfiCode;
        }

        public void setCfiCode(String cfiCode) {
            this.cfiCode = cfiCode;
        }

        public String getActivationDate() {
            return activationDate;
        }

        public void setActivationDate(String activationDate) {
            this.activationDate = activationDate;
        }

        public String getLastEligibleTradeDate() {
            return lastEligibleTradeDate;
        }

        public void setLastEligibleTradeDate(String lastEligibleTradeDate) {
            this.lastEligibleTradeDate = lastEligibleTradeDate;
        }
    }



    public static void main(String[] args) throws Exception {
        HttpClient client = new HttpClient();
        client.demoGetRESTAPI();
    }
}

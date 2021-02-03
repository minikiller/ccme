package quickfix.examples.executor;

import quickfix.ConfigError;
import quickfix.Message;
import quickfix.SessionSettings;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Util {
    private static final String INSTRUMENT_NAME = "InstrumentName";
    private static final String INSTRUMENT_DATE = "InstrumentDate";

    public static String get32UUID() {
        String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
        return uuid;
    }

    public static String generateID() {
        return Long.toString(System.currentTimeMillis());
    }

    public static void printMsg(Message msg) {
        char delimiter = 1;
        String str = msg.toString();
        str = str.replace(delimiter, '|');
        System.out.println("get message is " + str);
    }

    /**
     * 获得产品 instrumentName 配置列表
     * * @return
     *
     * @throws ConfigError
     * @throws IOException
     */
    public static List<String> getNameList() {
        InputStream inputStream = MarketDataServer.class.getResourceAsStream("executor.cfg");
        SessionSettings settings = null;
        List<String> instrumentNameList = null;
        try {
            settings = new SessionSettings(inputStream);
            instrumentNameList = Arrays
                    .asList(settings.getString(INSTRUMENT_NAME).trim().split(","));

            inputStream.close();
        } catch (ConfigError configError) {
            configError.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return instrumentNameList;
    }

    /**
     * 获得产品 instrumentDate 配置列表
     * * @return
     *
     * @throws ConfigError
     * @throws IOException
     */
    public static List<String> getDateList() {
        List<String> instrumentDateList = null;
        InputStream inputStream = MarketDataServer.class.getResourceAsStream("executor.cfg");
        SessionSettings settings = null;
        try {
            settings = new SessionSettings(inputStream);
            instrumentDateList = Arrays
                    .asList(settings.getString(INSTRUMENT_DATE).trim().split(","));

            inputStream.close();
        } catch (ConfigError configError) {
            configError.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return instrumentDateList;
    }
}
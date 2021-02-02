package quickfix.examples.executor;

/**
 * https://www.onixs.biz/fix-dictionary/4.4/compBlock_Instrument.html
 */
public class MyInstrument {
    private String symbol;//中石油
    private String securityIDSource;
    private String securityID; //编号
    private String cFICode;//

    public MyInstrument(String symbol, String securityID, String securityIDSource, String cFICode) {
        this.symbol = symbol;
        this.securityIDSource = securityIDSource;
        this.securityID = securityID;
        this.cFICode = cFICode;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getSecurityIDSource() {
        return securityIDSource;
    }

    public void setSecurityIDSource(String securityIDSource) {
        this.securityIDSource = securityIDSource;
    }

    public String getSecurityID() {
        return securityID;
    }

    public void setSecurityID(String securityID) {
        this.securityID = securityID;
    }

    public String getcFICode() {
        return cFICode;
    }

    public void setcFICode(String cFICode) {
        this.cFICode = cFICode;
    }
}

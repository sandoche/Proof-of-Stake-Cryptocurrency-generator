// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class SearchCurrenciesCall extends APICall.Builder<SearchCurrenciesCall> {
    private SearchCurrenciesCall() {
        super(ApiSpec.searchCurrencies);
    }

    public static SearchCurrenciesCall create() {
        return new SearchCurrenciesCall();
    }

    public SearchCurrenciesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public SearchCurrenciesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public SearchCurrenciesCall includeCounts(boolean includeCounts) {
        return param("includeCounts", includeCounts);
    }

    public SearchCurrenciesCall query(String query) {
        return param("query", query);
    }

    public SearchCurrenciesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public SearchCurrenciesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}

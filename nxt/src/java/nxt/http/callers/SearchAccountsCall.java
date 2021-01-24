// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class SearchAccountsCall extends APICall.Builder<SearchAccountsCall> {
    private SearchAccountsCall() {
        super(ApiSpec.searchAccounts);
    }

    public static SearchAccountsCall create() {
        return new SearchAccountsCall();
    }

    public SearchAccountsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public SearchAccountsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public SearchAccountsCall query(String query) {
        return param("query", query);
    }

    public SearchAccountsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public SearchAccountsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}

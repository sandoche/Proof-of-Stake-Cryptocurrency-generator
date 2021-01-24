// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class SearchTaggedDataCall extends APICall.Builder<SearchTaggedDataCall> {
    private SearchTaggedDataCall() {
        super(ApiSpec.searchTaggedData);
    }

    public static SearchTaggedDataCall create() {
        return new SearchTaggedDataCall();
    }

    public SearchTaggedDataCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public SearchTaggedDataCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public SearchTaggedDataCall query(String query) {
        return param("query", query);
    }

    public SearchTaggedDataCall channel(String channel) {
        return param("channel", channel);
    }

    public SearchTaggedDataCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public SearchTaggedDataCall tag(String tag) {
        return param("tag", tag);
    }

    public SearchTaggedDataCall includeData(boolean includeData) {
        return param("includeData", includeData);
    }

    public SearchTaggedDataCall account(String account) {
        return param("account", account);
    }

    public SearchTaggedDataCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public SearchTaggedDataCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}

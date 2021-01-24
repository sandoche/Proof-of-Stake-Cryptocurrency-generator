// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class SearchAssetsCall extends APICall.Builder<SearchAssetsCall> {
    private SearchAssetsCall() {
        super(ApiSpec.searchAssets);
    }

    public static SearchAssetsCall create() {
        return new SearchAssetsCall();
    }

    public SearchAssetsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public SearchAssetsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public SearchAssetsCall includeCounts(boolean includeCounts) {
        return param("includeCounts", includeCounts);
    }

    public SearchAssetsCall query(String query) {
        return param("query", query);
    }

    public SearchAssetsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public SearchAssetsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}

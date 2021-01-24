// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class SearchDGSGoodsCall extends APICall.Builder<SearchDGSGoodsCall> {
    private SearchDGSGoodsCall() {
        super(ApiSpec.searchDGSGoods);
    }

    public static SearchDGSGoodsCall create() {
        return new SearchDGSGoodsCall();
    }

    public SearchDGSGoodsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public SearchDGSGoodsCall seller(String seller) {
        return param("seller", seller);
    }

    public SearchDGSGoodsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public SearchDGSGoodsCall includeCounts(boolean includeCounts) {
        return param("includeCounts", includeCounts);
    }

    public SearchDGSGoodsCall hideDelisted(String hideDelisted) {
        return param("hideDelisted", hideDelisted);
    }

    public SearchDGSGoodsCall query(String query) {
        return param("query", query);
    }

    public SearchDGSGoodsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public SearchDGSGoodsCall tag(String tag) {
        return param("tag", tag);
    }

    public SearchDGSGoodsCall inStockOnly(String inStockOnly) {
        return param("inStockOnly", inStockOnly);
    }

    public SearchDGSGoodsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}

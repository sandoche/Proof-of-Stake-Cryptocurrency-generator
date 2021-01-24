// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetDGSTagsLikeCall extends APICall.Builder<GetDGSTagsLikeCall> {
    private GetDGSTagsLikeCall() {
        super(ApiSpec.getDGSTagsLike);
    }

    public static GetDGSTagsLikeCall create() {
        return new GetDGSTagsLikeCall();
    }

    public GetDGSTagsLikeCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetDGSTagsLikeCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetDGSTagsLikeCall tagPrefix(String tagPrefix) {
        return param("tagPrefix", tagPrefix);
    }

    public GetDGSTagsLikeCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetDGSTagsLikeCall inStockOnly(String inStockOnly) {
        return param("inStockOnly", inStockOnly);
    }

    public GetDGSTagsLikeCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}

// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetDataTagsLikeCall extends APICall.Builder<GetDataTagsLikeCall> {
    private GetDataTagsLikeCall() {
        super(ApiSpec.getDataTagsLike);
    }

    public static GetDataTagsLikeCall create() {
        return new GetDataTagsLikeCall();
    }

    public GetDataTagsLikeCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetDataTagsLikeCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetDataTagsLikeCall tagPrefix(String tagPrefix) {
        return param("tagPrefix", tagPrefix);
    }

    public GetDataTagsLikeCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetDataTagsLikeCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}

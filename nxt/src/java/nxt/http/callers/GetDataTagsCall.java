// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetDataTagsCall extends APICall.Builder<GetDataTagsCall> {
    private GetDataTagsCall() {
        super(ApiSpec.getDataTags);
    }

    public static GetDataTagsCall create() {
        return new GetDataTagsCall();
    }

    public GetDataTagsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetDataTagsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetDataTagsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetDataTagsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}

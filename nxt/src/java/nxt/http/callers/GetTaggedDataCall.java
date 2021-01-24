// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetTaggedDataCall extends APICall.Builder<GetTaggedDataCall> {
    private GetTaggedDataCall() {
        super(ApiSpec.getTaggedData);
    }

    public static GetTaggedDataCall create() {
        return new GetTaggedDataCall();
    }

    public GetTaggedDataCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetTaggedDataCall retrieve(boolean retrieve) {
        return param("retrieve", retrieve);
    }

    public GetTaggedDataCall includeData(boolean includeData) {
        return param("includeData", includeData);
    }

    public GetTaggedDataCall transaction(String transaction) {
        return param("transaction", transaction);
    }

    public GetTaggedDataCall transaction(long transaction) {
        return unsignedLongParam("transaction", transaction);
    }

    public GetTaggedDataCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}

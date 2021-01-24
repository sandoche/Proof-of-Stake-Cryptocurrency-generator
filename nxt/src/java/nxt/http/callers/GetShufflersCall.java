// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetShufflersCall extends APICall.Builder<GetShufflersCall> {
    private GetShufflersCall() {
        super(ApiSpec.getShufflers);
    }

    public static GetShufflersCall create() {
        return new GetShufflersCall();
    }

    public GetShufflersCall includeParticipantState(boolean includeParticipantState) {
        return param("includeParticipantState", includeParticipantState);
    }

    public GetShufflersCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public GetShufflersCall account(String account) {
        return param("account", account);
    }

    public GetShufflersCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetShufflersCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }

    public GetShufflersCall shufflingFullHash(String shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }

    public GetShufflersCall shufflingFullHash(byte[] shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }
}

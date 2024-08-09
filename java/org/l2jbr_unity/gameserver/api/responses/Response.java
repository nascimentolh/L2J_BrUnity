package org.l2jbr_unity.gameserver.api.responses;

public class Response {
    private int code;
    private String message;

    public Response(int code, String message){
        code = code;
        message = message;
    }
}

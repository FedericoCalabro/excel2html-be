package com.e2h.request;

import lombok.Data;

@Data
public class SendToServer {
    private String url;
    private Long port;
    private String protocol;
    private String absPath;
    private String username;
    private String password;
}

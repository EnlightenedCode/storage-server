package com.risevision.storage.api;

import javax.servlet.http.*;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import java.util.Arrays;
import java.util.List;

public class CheckThirdPartyCookieServlet extends HttpServlet {
    private final CreateThirdPartyCookieServlet servlet;
    //List of allowed origins
    private final List<String> incomingUrls;

    public CheckThirdPartyCookieServlet(){
        this.incomingUrls = Arrays.asList("http://storage.risevision.com","http://localhost:8000");
        this.servlet = new CreateThirdPartyCookieServlet(new Cookie("third_party_c_t","third_party_c_t"), incomingUrls);
    }

    public CheckThirdPartyCookieServlet(CreateThirdPartyCookieServlet serv, List<String> domains){
        this.incomingUrls = domains;
        this.servlet = serv;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        // Get client's origin
        String clientOrigin = request.getHeader("origin");
        int myIndex = incomingUrls.indexOf(clientOrigin);
        if(myIndex != -1) {
            response.setHeader("Access-Control-Allow-Origin", clientOrigin);
            response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
            response.setHeader("Access-Control-Max-Age", "3600");
            response.setHeader("Access-Control-Allow-Headers", "x-requested-with");
            response.setHeader("Access-Control-Allow-Credentials", "true");
        }
        Cookie[] cookies = request.getCookies();
        boolean cookieFound = false;

        if (cookies != null) {
            for (Cookie c : cookies) {
                if (servlet.getCookie().getName().equals(c.getName())) {
                    if (servlet.getCookie().getName().equals(c.getValue())) {
                        cookieFound = true;
                        c.setValue("");
                        break;
                    }
                }
            }
        }
        PrintWriter out = response.getWriter();
        String javascript = "{\"check\":\"" + cookieFound + "\"}";
        out.print(javascript);
        out.flush();
    }

}

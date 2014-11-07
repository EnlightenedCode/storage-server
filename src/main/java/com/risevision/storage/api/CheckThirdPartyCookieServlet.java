package com.risevision.storage.api;

import javax.servlet.http.*;
import java.io.*;
import java.util.Arrays;
import javax.servlet.http.HttpServlet;
import java.util.List;

public class CheckThirdPartyCookieServlet extends HttpServlet {
  private CreateThirdPartyCookieServlet servlet;

  //List of allowed origins
  private final List<String> incomingUrls;


  public CheckThirdPartyCookieServlet(){
      this.incomingUrls = Arrays.asList("http://storage.risevision.com","http://localhost:8000, http://192.254.220.35");
  }

  public CheckThirdPartyCookieServlet(CreateThirdPartyCookieServlet serv, List<String> domains){
      this.incomingUrls = domains;
      this.servlet = serv;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");

    String clientOrigin = request.getHeader("origin");
    int myIndex = incomingUrls.indexOf(clientOrigin);
    if(myIndex != -1) {
      response.setHeader("Access-Control-Allow-Origin", clientOrigin);
      response.setHeader("Access-Control-Allow-Methods", "GET");
      response.setHeader("Access-Control-Allow-Credentials", "true");
    }

    Cookie[] cookies = request.getCookies();
    boolean cookieFound = false;

    if (cookies != null) {
      for (Cookie c : cookies) {
        if (c.getName().equals("third_party_c_t")) {
          cookieFound = true;
          break;
        }
      }
    }

    String javascript = "{\"check\":\"" + cookieFound + "\"}";
    PrintWriter out = response.getWriter();
    out.print(javascript);
    out.flush();
  }
}


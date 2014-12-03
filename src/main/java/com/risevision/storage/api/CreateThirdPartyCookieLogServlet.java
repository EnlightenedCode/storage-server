package com.risevision.storage.api;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class CreateThirdPartyCookieLogServlet extends HttpServlet {
  //List of allowed origins
  private final List<String> incomingUrls;
  private final Cookie cookie;
  protected static final Logger log = Logger.getAnonymousLogger();

  public CreateThirdPartyCookieLogServlet(){
    this.incomingUrls = Arrays.asList("http://storage.risevision.com","http://localhost:8000", "http://192.254.220.35");
    this.cookie = new Cookie("third_party_c_t","third_party_c_t");
  }

  public CreateThirdPartyCookieLogServlet(Cookie c, List<String> domains){
    this.incomingUrls = domains;
    this.cookie = c;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("application/json");
    String clientOrigin, corsMethod, corsHeaders;

// Find out what the request is asking for
    clientOrigin = request.getHeader("Origin");
    corsMethod = request.getHeader("Access-Control-Request-Method");
    corsHeaders = request.getHeader("Access-Control-Request-Headers");
    // Get client's IP address
    String ipAddress = request.getHeader("x-forwarded-for");
    if (ipAddress == null) {
      ipAddress = request.getRemoteAddr();
    }
    log.info("ipAddress: " + ipAddress);
    log.info("clientOrigin: " + clientOrigin);
    log.info("corsMethod: " + corsMethod);
    log.info("corsHeaders: " + corsHeaders);
    int myIndex = incomingUrls.indexOf(clientOrigin);
    log.info("myIndex:" + myIndex);
    if(myIndex != -1) {
      response.addCookie(cookie);
      response.setHeader("Access-Control-Allow-Origin", clientOrigin);
      log.info("response contains Access-Control-Allow-Origin: " + response.containsHeader("Access-Control-Allow-Origin"));
      response.setHeader("Access-Control-Allow-Methods", "GET");
      log.info("response contains Access-Control-Allow-Methods: " + response.containsHeader("Access-Control-Allow-Methods"));
      response.setHeader("Access-Control-Allow-Credentials", "true");
      log.info("response contains Access-Control-Allow-Credentials: " + response.containsHeader("Access-Control-Allow-Credentials"));
    }

    PrintWriter out = response.getWriter();
    String javascript = "{\"completed\": \"true\"}";
    out.print(javascript);
    out.flush();
  }

  public Cookie getCookie(){
    return this.cookie;
  }
}
package com.risevision.storage.api;

import javax.servlet.http.*;
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
    // Get client's origin
    String clientOrigin = request.getHeader("Origin");
    log.info("clientOrigin: " + clientOrigin);
    int myIndex = incomingUrls.indexOf(clientOrigin);
    log.info("myIndex:" + myIndex);
    if(myIndex != -1) {
      response.addCookie(cookie);
      response.setHeader("Access-Control-Allow-Origin", clientOrigin);
      response.setHeader("Access-Control-Allow-Methods", "GET");
      response.setHeader("Access-Control-Allow-Credentials", "true");
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
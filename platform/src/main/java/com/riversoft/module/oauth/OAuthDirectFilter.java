package com.riversoft.module.oauth;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebFilter("/oauth/*")
public class OAuthDirectFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        String target = target(path);
        if (target != null) {
            RequestDispatcher dispatcher = request.getRequestDispatcher(target);
            dispatcher.forward(request, response);
            return;
        }
        httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    public void destroy() {
    }

    protected String target(String path) {
        if ("/oauth/authorize".equals(path)) {
            return "/oauth/OAuthAction/authorize.shtml";
        }
        if ("/oauth/token".equals(path)) {
            return "/oauth/OAuthAction/token.shtml";
        }
        if ("/oauth/userinfo".equals(path)) {
            return "/oauth/OAuthAction/userinfo.shtml";
        }
        return null;
    }
}

package com.riversoft.module.oauth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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

import com.riversoft.core.web.Actions;

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
            RequestDispatcher dispatcher = request.getRequestDispatcher(withPublicFullUrl(httpRequest, target));
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

    private String withPublicFullUrl(HttpServletRequest request, String target) {
        StringBuilder publicUrl = new StringBuilder(request.getRequestURL());
        if (request.getQueryString() != null) {
            publicUrl.append('?').append(request.getQueryString());
        }
        return target + "?" + Actions.Keys.FULL_URL.toString() + "=" + urlEncode(publicUrl.toString());
    }

    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 is unavailable.", e);
        }
    }
}

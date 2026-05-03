<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>OAuth 登录失败</title>
<style>
body {
    margin: 0;
    font-family: Arial, "Microsoft YaHei", sans-serif;
    background: #f5f7fa;
    color: #2f3542;
}
.oauth-error {
    max-width: 560px;
    margin: 96px auto;
    padding: 32px;
    background: #fff;
    border: 1px solid #dfe4ea;
}
.oauth-error h1 {
    margin: 0 0 16px;
    font-size: 22px;
}
.oauth-error p {
    margin: 8px 0;
    line-height: 1.7;
}
.request-id {
    color: #747d8c;
    font-size: 12px;
}
</style>
</head>
<body>
<div class="oauth-error">
    <h1>${empty oauthErrorTitle ? "OAuth 登录失败" : oauthErrorTitle}</h1>
    <p>${empty oauthErrorMessage ? "当前 OAuth 登录请求无法继续，请联系系统管理员确认外部系统配置。" : oauthErrorMessage}</p>
    <p class="request-id">Request ID: ${requestId}</p>
</div>
</body>
</html>

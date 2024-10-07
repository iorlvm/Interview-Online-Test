<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>艾斯博面試上機測驗-WeiEN</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="/css/main.css">
</head>
<body>

<div class="container mt-5">
    <h1 class="text-center">選擇您的水果</h1>
    <div id="product-list" class="row mt-4"></div>

    <div class="mt-5 text-center">
        <div class="d-flex align-items-center justify-content-center mb-3">
            <label for="customer-name" class="h5 mr-2">訂購者：</label>
            <input type="text" id="customer-name" class="form-control" style="width: 200px;" placeholder="請輸入姓名" required>
        </div>
        <div class="d-flex gap-2 justify-content-center">
            <button class="btn btn-secondary " id="clear-button">清空</button>
            <button class="btn btn-primary" id="order-button">送出</button>
        </div>
    </div>

    <div id="order-summary" class="mt-3"></div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="/js/main.js"></script>
</body>
</html>
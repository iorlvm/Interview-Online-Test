document.addEventListener("DOMContentLoaded", function () {
    const products = [
        { name: "蘋果", price: 10, imgSrc: "http://fakeimg.pl/150x150" },
        { name: "香蕉", price: 12, imgSrc: "http://fakeimg.pl/150x150" },
        { name: "西瓜", price: 20, imgSrc: "http://fakeimg.pl/150x150" },
    ];

    const productList = document.getElementById("product-list");

    // 水果卡片
    products.forEach((product, index) => {
        const card = document.createElement("div");
        card.className = "col-md-4 mb-4";
        card.innerHTML = `
            <div class="card text-center shadow">
                <img src="${product.imgSrc}" class="card-img-top" alt="${product.name}">
                <div class="card-body">
                    <h5 class="card-title">${product.name}</h5>
                    <p class="card-text">價格: ${product.price} 元</p>
                    <input type="number" id="product-${index}" class="form-control" min="0" max="10" value="0">
                </div>
            </div>
        `;
        card.querySelector('input')
        productList.appendChild(card);
    });

    // 提交列表
    document.getElementById("order-button").addEventListener("click", () => {
        // 現為純前端顯示，後續改為後端回應
        const customerName = document.getElementById("customer-name").value;
        let orderSummary = `${customerName} 你好，您總共購買了:\n`;
        let totalPrice = 0;

        products.forEach((product, index) => {
            const quantity = parseInt(document.getElementById(`product-${index}`).value);
            if (quantity > 0) {
                orderSummary += `${product.name} ${quantity} 顆\n`;
                totalPrice += product.price * quantity;
            }
        });

        // 顯示訂單內容
        const summaryElement = document.getElementById("order-summary");
        summaryElement.innerHTML = `
            <div class="order-summary-card">
                <h4>訂單摘要</h4>
                <pre>${orderSummary}總計: ${totalPrice} 元</pre>
            </div>
        `;
    });

    // 清空資訊
    document.getElementById("clear-button").addEventListener("click", () => {
        document.getElementById("customer-name").value = "";
        products.forEach((_, index) => {
            document.getElementById(`product-${index}`).value = 0;
        });
        document.getElementById("order-summary").innerHTML = "";
    });
});
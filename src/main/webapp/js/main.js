document.addEventListener("DOMContentLoaded", async function () {
    const res = await getProductListAPI(0, 20)

    let products;
    if (res.success) {
        products = res.data;

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
                    <p class="card-text">${product.price} 元 / ${product.unit}</p>
                    <input type="number" id="product-${index}" class="form-control" min="0" max="10" value="0">
                </div>
            </div>
        `;
            card.querySelector('input')
            productList.appendChild(card);
        });
    }


    // 提交列表
    document.getElementById("order-button").addEventListener("click", async () => {
        let orderProducts = [];
        products.forEach((product, index) => {
            const count = parseInt(document.getElementById(`product-${index}`).value);
            if (count > 0) {
                orderProducts.push({
                    productId: product.id,
                    count
                })
            }
        });

        const res = await creatOrderAPI({products: orderProducts});

        console.log(res);
        if (res.success) {
            const customerName = document.getElementById("customer-name").value;
            let orderSummary = `${customerName} 你好，您總共購買了:\n`;
            let totalPrice = res.data.totalPrice;

            res.data.products.forEach(product => {
                orderSummary += `${product.productName} ${product.count} ${product.productUnit}\n`;
            });

            // 顯示訂單內容
            const summaryElement = document.getElementById("order-summary");
            summaryElement.innerHTML = `
                <div class="order-summary-card">
                    <h4>訂單摘要</h4>
                    <pre>${orderSummary}總計: ${totalPrice} 元</pre>
                </div>
            `;
        } else {
            // 顯示失敗訊息
            const summaryElement = document.getElementById("order-summary");
            summaryElement.innerHTML = res.errorMsg;
        }

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
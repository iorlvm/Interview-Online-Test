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

    // 渲染訂單列表
    await renderOrderList();


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
        const customerName = document.getElementById("customer-name").value;

        const res = await createOrderAPI({customerName ,products: orderProducts});

        console.log(res);
        if (res.success) {
            let orderSummary = `${res.data.customer.name} 你好，您總共購買了:\n`;
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

// 渲染訂單列表的函數
async function renderOrderList() {
    try {
        const ordersRes = await getOrdersAPI();  // 獲取訂單 API
        const orderList = document.getElementById("order-list");
        orderList.innerHTML = ""; // 清空目前的訂單列表
        console.log(ordersRes); // 打印 API 返回的結果

        if (ordersRes.success && ordersRes.data.length > 0) {
            ordersRes.data.forEach(order => {
                const listItem = document.createElement("li");
                listItem.className = "list-group-item";

                // 顯示訂單的所有屬性
                listItem.innerHTML = `
                    <h5 class="mb-2">訂單號: ${order.orderId}</h5>
                    <p><strong>訂購者:</strong> ${order.customer.name}</p>
                    <p><strong>使用者編號:</strong> ${order.customerId}</p>
                    <p><strong>總價:</strong> ${order.totalPrice} 元</p>
                    <p><strong>成交時間:</strong> ${order.createDate}</p>
                    <p><strong>狀態:</strong> ${order.success ? '成交' : '未成交'}</p>
                    <strong>產品:</strong>
                    <ul class="list-unstyled">
                        ${order.products.map(product => `
                            <li class="border-bottom py-2">
                                <strong>${product.productName}</strong> - ${product.productPrice} 元 / ${product.productUnit} - 數量: ${product.count}
                            </li>
                        `).join('')}
                    </ul>
                `;
                orderList.appendChild(listItem); // 將每個訂單加入列表
            });
        } else {
            const emptyMessage = document.createElement("li");
            emptyMessage.className = "list-group-item text-center";
            emptyMessage.textContent = "目前沒有任何訂單。"; // 顯示空訂單提示
            orderList.appendChild(emptyMessage);
        }
    } catch (error) {
        console.error("無法加載訂單列表:", error); // 處理錯誤
    }
}

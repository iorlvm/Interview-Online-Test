document.addEventListener("DOMContentLoaded", async function () {

    const productList = document.getElementById("product-list");
    if (productList) {
        // 渲染商品列表
        await renderProductList(productList);
    }

    const orderList = document.getElementById("order-list");
    if (orderList) {
        // 渲染訂單列表
        await renderOrderList(orderList);
    }

    const apiTest = document.getElementById("api-test-result");
    if (apiTest) {
        // 測驗Api渲染
        await renderApiTest(apiTest);
    }
});

async function renderApiTest (apiTest){
    const res = await testAPI();
    console.log(res);

    if (res.code === 200) {
        let tableHTML = `
            <table class="table table-striped table-bordered mt-4">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>姓名</th>
                        <th>Email</th>
                        <th>性別</th>
                        <th>狀態</th>
                    </tr>
                </thead>
                <tbody>`;

        const users = res.data;
        users.forEach(user => {
            tableHTML += `
                <tr>
                    <td>${user.id}</td>
                    <td>${user.name}</td>
                    <td>${user.email}</td>
                    <td>${user.gender}</td>
                    <td>${user.status}</td>
                </tr>`;
        });
        tableHTML += `
                </tbody>
            </table>`;

        apiTest.innerHTML = tableHTML;
    }
}

async function renderProductList (productList) {
    const res = await getProductListAPI(0, 20);

    let products;
    if (res.success) {
        products = res.data;

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
        const customerName = document.getElementById("customer-name").value;

        const res = await createOrderAPI({customerName, products: orderProducts});

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
}

// 渲染訂單列表的函數
async function renderOrderList(orderList) {
    try {
        const ordersRes = await getOrdersAPI();  // 獲取訂單 API
        orderList.innerHTML = ""; // 清空目前的訂單列表

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
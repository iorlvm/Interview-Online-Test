const testAPI = () => {
    let url = `https://gorest.co.in/public-api/users`;
    return fetch(url, {
        method: 'GET'
    }).then(response => {
        if (!response.ok) {
            return Promise.reject(new Error(`HTTP error! Status: \${response.status}`));
        }
        return response.json();
    }).then(res => {
        return res;
    }).catch(error => {
        throw error;
    });
}


const getProductListAPI = (page = 0, size = 20) => {
    let url = `/api/products?page=${page}&size=${size}`;
    return fetch(url, {
        method: 'GET'
    }).then(response => {
        if (!response.ok) {
            return Promise.reject(new Error(`HTTP error! Status: \${response.status}`));
        }
        return response.json();
    }).then(res => {
        return res;
    }).catch(error => {
        throw error;
    });
}

const createOrderAPI = ({customerName ,products}) => {
    let url = `/api/orders`;
    return fetch(url, {
        headers: {
            'Content-Type': 'application/json'
        },
        method: 'POST',
        body: JSON.stringify({
            products,
            customer: {
                name: customerName
            }
        })
    }).then(response => {
        if (!response.ok) {
            return Promise.reject(new Error(`HTTP error! Status: \${response.status}`));
        }
        return response.json();
    }).then(res => {
        return res;
    }).catch(error => {
        throw error;
    });
}

const getOrdersAPI = (page = 0, size = 20) => {
    let url = `/api/orders?page=${page}&size=${size}`;
    return fetch(url, {
        method: 'GET'
    }).then(response => {
        if (!response.ok) {
            return Promise.reject(new Error(`HTTP error! Status: \${response.status}`));
        }
        return response.json();
    }).then(res => {
        return res;
    }).catch(error => {
        throw error;
    });
}
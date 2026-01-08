// src/main/resources/static/js/booking.js

let selectedTests = []; 
let currentSubTotal = 0; 
let patientId = 0; // Will be set from HTML

// Initialize
document.addEventListener("DOMContentLoaded", function() {
    // Read the Patient ID from the hidden input field
    patientId = parseInt(document.getElementById("patientIdHidden").value);
});

function addToCart(selectElement) {
    const option = selectElement.options[selectElement.selectedIndex];
    if (!option.value) return; 

    const testId = parseInt(option.value);
    const name = option.getAttribute("data-name");
    const priceStr = option.getAttribute("data-price");
    const price = parseFloat(priceStr);

    if (selectedTests.find(t => t.id === testId)) {
        alert("This test is already in the list!");
        selectElement.value = ""; 
        return;
    }

    selectedTests.push({ id: testId, name: name, price: price });
    selectElement.value = "";
    renderCart();
}

function removeFromCart(index) {
    selectedTests.splice(index, 1);
    renderCart();
}

function renderCart() {
    const list = document.getElementById("cartList");
    const emptyMsg = document.getElementById("emptyMsg");
    
    list.innerHTML = ""; 
    let total = 0;

    if (selectedTests.length === 0) {
        list.appendChild(emptyMsg);
    } else {
        selectedTests.forEach((test, index) => {
            total += test.price;
            const li = document.createElement("li");
            li.className = "list-group-item d-flex justify-content-between align-items-center cart-item small";
            li.innerHTML = `
                <span>${test.name}</span>
                <div>
                    <span class="fw-bold me-2">${test.price}</span>
                    <button onclick="removeFromCart(${index})" class="btn btn-sm text-danger p-0 border-0" title="Remove">✖</button>
                </div>
            `;
            list.appendChild(li);
        });
    }

    currentSubTotal = total;
    document.getElementById("subTotalDisplay").innerText = currentSubTotal;
    calculateFinal();
}

function calculateFinal() {
    const discountInput = document.getElementById('discountInput').value;
    const discount = parseFloat(discountInput) || 0;
    const net = currentSubTotal - discount;
    document.getElementById('netTotalDisplay').innerText = net;
    calculateBalance();
}

function calculateBalance() {
    const discount = parseFloat(document.getElementById('discountInput').value) || 0;
    const cash = parseFloat(document.getElementById('cashInput').value) || 0;
    const net = currentSubTotal - discount;
    const balance = net - cash;
    
    const balanceSpan = document.getElementById('balanceDisplay');
    balanceSpan.innerText = balance;
    
    if (balance > 0) {
        balanceSpan.className = "fw-bold text-danger";
        balanceSpan.innerText = balance + " (Due)";
    } else if (balance < 0) {
        balanceSpan.className = "fw-bold text-success";
        balanceSpan.innerText = Math.abs(balance) + " (Change)";
    } else {
        balanceSpan.className = "text-success";
        balanceSpan.innerText = "0 (Paid)";
    }
}

function submitOrder() {
    if (selectedTests.length === 0) {
        alert("Please select at least one test!");
        return;
    }
    
    const btn = document.querySelector('button[onclick="submitOrder()"]');
    btn.disabled = true;
    btn.innerText = "Processing...";

    const doctorId = document.getElementById('doctorSelect').value;
    const testIds = selectedTests.map(t => t.id);
    const discount = parseFloat(document.getElementById('discountInput').value) || 0;
    const cash = parseFloat(document.getElementById('cashInput').value) || 0;

    axios.post('/api/orders', {
        patientId: patientId,
        doctorId: doctorId ? parseInt(doctorId) : null,
        testIds: testIds,
        discount: discount,
        cashPaid: cash
    }).then(response => {
        const orderId = response.data.id;
        window.location.href = "/orders/receipt/" + orderId;
    }).catch(error => {
        console.error(error);
        alert("Error booking order.");
        btn.disabled = false;
        btn.innerText = "Confirm & Print";
    });
}
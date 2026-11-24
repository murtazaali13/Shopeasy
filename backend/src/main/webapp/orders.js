const ordersMessage = document.querySelector("#orders-message");
const ordersList = document.querySelector("#orders-list");

function formatOrderPrice(amount) {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR"
  }).format(Number(amount));
}

function createOrderCard(order) {
  const card = document.createElement("article");
  card.className = "order-card";

  const heading = document.createElement("h2");
  heading.textContent = `Order #${order.orderId}`;
  card.append(heading);

  const details = document.createElement("p");
  const paymentMethod = String(order.paymentMethod || "")
    .replace(/_/g, " ");

  details.className = "order-details";
  details.textContent =
    `Status: ${order.status} · Placed: ${order.createdAt} · Payment: ${paymentMethod}`;
  card.append(details);

  const itemList = document.createElement("ul");
  itemList.className = "order-items";

  for (const item of order.items || []) {
    const listItem = document.createElement("li");
    listItem.textContent =
      `${item.productName} × ${item.quantity} — ${formatOrderPrice(item.unitPrice)} each`;
    itemList.append(listItem);
  }

  card.append(itemList);

  const total = document.createElement("p");
  total.className = "order-total";
  total.textContent = `Order total: ${formatOrderPrice(order.total)}`;
  card.append(total);

  return card;
}

async function loadOrders() {
  try {
    const response = await fetch("./api/orders");
    const result = await response.json();

    if (!response.ok) {
      ordersMessage.textContent =
        result.message || "Could not load your orders.";

      if (response.status === 401) {
        const signInLink = document.createElement("a");
        signInLink.href = "login.html";
        signInLink.textContent = " Sign in";
        ordersMessage.append(signInLink);
      }

      return;
    }

    const orders = Array.isArray(result.orders) ? result.orders : [];
    ordersList.replaceChildren();

    if (orders.length === 0) {
      ordersMessage.textContent = "You have not placed any orders yet.";
      return;
    }

    ordersMessage.textContent =
      `You have ${orders.length} order${orders.length === 1 ? "" : "s"}.`;

    for (const order of orders) {
      ordersList.append(createOrderCard(order));
    }
  } catch (error) {
    console.error("Could not load order history:", error);
    ordersMessage.textContent =
      "Could not contact ShopEasy. Check that Tomcat is running, then try again.";
  }
}

loadOrders();
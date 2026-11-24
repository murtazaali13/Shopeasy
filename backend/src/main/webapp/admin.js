const adminMessage = document.querySelector("#admin-message");
const adminPanel = document.querySelector("#admin-panel");
const productForm = document.querySelector("#product-form");
const productList = document.querySelector("#admin-product-list");

function formatAdminPrice(price) {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR"
  }).format(Number(price));
}

function createProductRow(product) {
  const row = document.createElement("article");
  row.className = "product-row";

  const name = document.createElement("h3");
  name.textContent = product.name;
  row.append(name);

  const category = document.createElement("p");
  category.textContent = `Category: ${product.category}`;
  row.append(category);

  const description = document.createElement("p");
  description.textContent = product.description;
  row.append(description);

  const price = document.createElement("p");
  price.textContent = `Price: ${formatAdminPrice(product.price)}`;
  row.append(price);

  const stock = document.createElement("p");
  stock.textContent = `Stock: ${product.stockQuantity}`;
  row.append(stock);

  const status = document.createElement("p");
  status.textContent = product.active ? "Status: Active" : "Status: Inactive";
  row.append(status);

  return row;
}

async function loadProducts() {
  try {
    const response = await fetch("./api/admin/products");
    const result = await response.json();

    if (!response.ok) {
      adminPanel.hidden = true;
      adminMessage.textContent =
        result.message || "Could not open the admin dashboard.";

      if (response.status === 401) {
        const signInLink = document.createElement("a");
        signInLink.href = "login.html";
        signInLink.textContent = " Sign in";
        adminMessage.append(signInLink);
      }

      return false;
    }

    adminPanel.hidden = false;
    adminMessage.textContent = "Admin access confirmed.";
    productList.replaceChildren();

    const products = Array.isArray(result.products) ? result.products : [];

    if (products.length === 0) {
      productList.textContent = "There are no products yet.";
    } else {
      for (const product of products) {
        productList.append(createProductRow(product));
      }
    }

    return true;
  } catch (error) {
    console.error("Could not load admin products:", error);
    adminPanel.hidden = true;
    adminMessage.textContent =
      "Could not contact ShopEasy. Check that Tomcat is running, then try again.";
    return false;
  }
}

productForm.addEventListener("submit", async (event) => {
  event.preventDefault();

  const submitButton = productForm.querySelector('button[type="submit"]');
  const formData = new FormData(productForm);
  const product = Object.fromEntries(formData.entries());

  product.price = Number(product.price);
  product.stockQuantity = Number(product.stockQuantity);

  submitButton.disabled = true;
  adminMessage.textContent = "Adding product...";

  try {
    const response = await fetch("./api/admin/products", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(product)
    });

    const result = await response.json();

    if (!response.ok) {
      adminMessage.textContent =
        result.message || "Could not add the product.";
      return;
    }

    productForm.reset();
    await loadProducts();
    adminMessage.textContent = result.message;
  } catch (error) {
    console.error("Could not add admin product:", error);
    adminMessage.textContent =
      "Could not contact ShopEasy. Check that Tomcat is running, then try again.";
  } finally {
    submitButton.disabled = false;
  }
});

loadProducts();

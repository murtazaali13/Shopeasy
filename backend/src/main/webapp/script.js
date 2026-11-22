const searchForm = document.querySelector("#search-form");
const searchInput = document.querySelector("#search-input");
const filterButtons = document.querySelectorAll(".filter-button");
const sortSelect = document.querySelector("#sort-select");
const productGrid = document.querySelector("#product-grid");
const productCards = [...document.querySelectorAll(".product-card")];
const resultCount = document.querySelector("#result-count");
const emptyState = document.querySelector("#empty-state");

const openCartButton = document.querySelector("#open-cart");
const closeCartButton = document.querySelector("#close-cart");
const continueShoppingButton = document.querySelector("#continue-shopping");
const cartDialog = document.querySelector("#cart-dialog");
const cartCount = document.querySelector("#cart-count");
const cartHeadingCount = document.querySelector("#cart-heading-count");
const cartItemsContainer = document.querySelector("#cart-items");
const cartEmptyMessage = document.querySelector("#cart-empty");
const cartFooter = document.querySelector("#cart-footer");
const cartSubtotal = document.querySelector("#cart-subtotal");
const toast = document.querySelector("#toast");

let selectedCategory = "all";
let toastTimer;

const cart = new Map();

function formatPrice(amount) {
  const formattedAmount = new Intl.NumberFormat("en-IN", {
    maximumFractionDigits: 0
  }).format(amount);

  return `₹${formattedAmount}`;
}

function sortProducts() {
  const cardsToSort = [...productCards];
  const sortChoice = sortSelect.value;

  if (sortChoice === "price-low") {
    cardsToSort.sort((a, b) => Number(a.dataset.price) - Number(b.dataset.price));
  } else if (sortChoice === "price-high") {
    cardsToSort.sort((a, b) => Number(b.dataset.price) - Number(a.dataset.price));
  } else if (sortChoice === "name") {
    cardsToSort.sort((a, b) => a.dataset.name.localeCompare(b.dataset.name));
  }

  cardsToSort.forEach((card) => productGrid.append(card));
}

function filterProducts() {
  const searchTerm = searchInput.value.trim().toLowerCase();
  let visibleProducts = 0;

  productCards.forEach((card) => {
    const productCategory = card.dataset.category;
    const searchableText =
      `${card.dataset.name} ${card.dataset.search}`.toLowerCase();

    const matchesCategory =
      selectedCategory === "all" || productCategory === selectedCategory;
    const matchesSearch = searchableText.includes(searchTerm);
    const shouldShow = matchesCategory && matchesSearch;

    card.hidden = !shouldShow;

    if (shouldShow) {
      visibleProducts += 1;
    }
  });

  resultCount.textContent =
    `${visibleProducts} ${visibleProducts === 1 ? "product" : "products"}`;
  emptyState.hidden = visibleProducts !== 0;
}

function showToast(message) {
  toast.textContent = message;
  toast.classList.add("visible");

  window.clearTimeout(toastTimer);
  toastTimer = window.setTimeout(() => {
    toast.classList.remove("visible");
  }, 2200);
}

function createCartActionButton(label, action, productId, accessibleName) {
  const button = document.createElement("button");
  button.type = "button";
  button.textContent = label;
  button.dataset.cartAction = action;
  button.dataset.productId = productId;
  button.setAttribute("aria-label", accessibleName);
  return button;
}

function renderCart() {
  const totalItems = [...cart.values()].reduce(
    (total, item) => total + item.quantity,
    0
  );

  cartCount.textContent = totalItems;
  cartHeadingCount.textContent = `(${totalItems})`;
  cartItemsContainer.replaceChildren();

  cartEmptyMessage.hidden = cart.size !== 0;
  cartFooter.hidden = cart.size === 0;

  let subtotal = 0;

  cart.forEach((item) => {
    subtotal += item.price * item.quantity;

    const row = document.createElement("article");
    row.className = "cart-item";

    const image = document.createElement("img");
    image.className = "cart-item-image";
    image.src = item.image;
    image.alt = item.name;
    image.loading = "lazy";

    const details = document.createElement("div");
    details.className = "cart-item-details";

    const name = document.createElement("h3");
    name.textContent = item.name;

    const price = document.createElement("p");
    price.className = "cart-item-price";
    price.textContent = formatPrice(item.price);

    const quantityControls = document.createElement("div");
    quantityControls.className = "quantity-controls";

    const decreaseButton = createCartActionButton(
      "−",
      "decrease",
      item.id,
      `Remove one ${item.name}`
    );

    const quantity = document.createElement("span");
    quantity.textContent = item.quantity;
    quantity.setAttribute("aria-label", "Quantity");

    const increaseButton = createCartActionButton(
      "+",
      "increase",
      item.id,
      `Add one ${item.name}`
    );

    quantityControls.append(decreaseButton, quantity, increaseButton);
    details.append(name, price, quantityControls);

    const removeButton = createCartActionButton(
      "Remove",
      "remove",
      item.id,
      `Remove ${item.name} from your bag`
    );
    removeButton.className = "remove-item";

    row.append(image, details, removeButton);
    cartItemsContainer.append(row);
  });

  cartSubtotal.textContent = formatPrice(subtotal);
}

function addProductToCart(button) {
  const card = button.closest(".product-card");
  const productId = card.dataset.id;
  const existingItem = cart.get(productId);

  if (existingItem) {
    existingItem.quantity += 1;
  } else {
    cart.set(productId, {
      id: productId,
      name: card.querySelector("h3").textContent,
      price: Number(card.dataset.price),
      image: card.querySelector("img").src,
      quantity: 1
    });
  }

  renderCart();
  showToast(`${card.querySelector("h3").textContent} added to your bag.`);
}

filterButtons.forEach((button) => {
  button.addEventListener("click", () => {
    selectedCategory = button.dataset.filter;

    filterButtons.forEach((filterButton) => {
      const isActive = filterButton === button;
      filterButton.classList.toggle("active", isActive);
      filterButton.setAttribute("aria-pressed", String(isActive));
    });

    filterProducts();
  });
});

searchInput.addEventListener("input", filterProducts);

searchForm.addEventListener("submit", (event) => {
  event.preventDefault();
  filterProducts();
});

sortSelect.addEventListener("change", () => {
  sortProducts();
  filterProducts();
});

document.querySelectorAll(".add-button").forEach((button) => {
  button.addEventListener("click", () => addProductToCart(button));
});

openCartButton.addEventListener("click", () => {
  cartDialog.showModal();
});

closeCartButton.addEventListener("click", () => {
  cartDialog.close();
});

continueShoppingButton.addEventListener("click", () => {
  cartDialog.close();
});

cartDialog.addEventListener("click", (event) => {
  if (event.target === cartDialog) {
    cartDialog.close();
  }
});

cartItemsContainer.addEventListener("click", (event) => {
  const actionButton = event.target.closest("[data-cart-action]");

  if (!actionButton) {
    return;
  }

  const productId = actionButton.dataset.productId;
  const action = actionButton.dataset.cartAction;
  const item = cart.get(productId);

  if (!item) {
    return;
  }

  if (action === "increase") {
    item.quantity += 1;
  } else if (action === "decrease") {
    item.quantity -= 1;

    if (item.quantity <= 0) {
      cart.delete(productId);
    }
  } else if (action === "remove") {
    cart.delete(productId);
  }

  renderCart();
});

cartDialog.addEventListener("close", () => {
  openCartButton.focus();
});

sortProducts();
filterProducts();
renderCart();

async function loadDatabaseProducts() {
  const productList = document.querySelector("#database-product-list");
  const statusMessage = document.querySelector("#database-product-status");

  if (!productList || !statusMessage) return;

  try {
    const response = await fetch("./api/products");

    if (!response.ok) {
      throw new Error(`The server returned status ${response.status}`);
    }

    const products = await response.json();
    productList.replaceChildren();
    const originalImages = Array.from(
  document.querySelectorAll("#product-grid .product-card img")
);

    products.forEach((product) => {
      const card = document.createElement("article");
      card.className = "database-product-card product-card";
card.dataset.id = String(product.id);
card.dataset.price = String(product.price);

const image = document.createElement("img");
image.src =
  originalImages[Number(product.id) - 1]?.src ||
  originalImages[0]?.src ||
  "";
image.alt = product.name;
image.loading = "lazy";

      const category = document.createElement("p");
      category.className = "database-product-category";
      category.textContent = product.category;

      const name = document.createElement("h3");
      name.textContent = product.name;

      const description = document.createElement("p");
      description.className = "database-product-description";
      description.textContent = product.description;

      const price = document.createElement("p");
      price.className = "database-product-price";
      price.textContent = new Intl.NumberFormat("en-IN", {
        style: "currency",
        currency: "INR"
      }).format(Number(product.price));

      const stock = document.createElement("p");
      stock.className = "database-product-stock";
      stock.textContent = `In stock: ${product.stockQuantity}`;
      const addButton = document.createElement("button");
addButton.type = "button";
addButton.className = "add-button";
addButton.textContent = "Add to cart";
addButton.addEventListener("click", () => addProductToCart(addButton));

      card.append(image, category, name, description, price, stock, addButton);
      productList.append(card);
    });

    statusMessage.textContent = `Loaded ${products.length} products from MySQL.`;
  } catch (error) {
    statusMessage.textContent =
      "Could not load products. Check that Tomcat and MySQL are running.";
    console.error("Product API error:", error);
  }
}

loadDatabaseProducts();
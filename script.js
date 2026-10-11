const searchInput = document.querySelector("#search-input");
const filterButtons = document.querySelectorAll(".filter-btn");
const productCards = document.querySelectorAll(".product-card");
const productCount = document.querySelector("#product-count");
const emptyMessage = document.querySelector("#empty-message");
const cartCount = document.querySelector("#cart-count");
const toast = document.querySelector("#toast");

let selectedCategory = "all";
let toastTimer;

function updateProducts() {
  const searchTerm = searchInput.value.trim().toLowerCase();
  let visibleCount = 0;

  productCards.forEach((card) => {
    const category = card.dataset.category;
    const searchableText = `${card.dataset.name} ${card.dataset.search}`.toLowerCase();

    const matchesCategory =
      selectedCategory === "all" || category === selectedCategory;
    const matchesSearch = searchableText.includes(searchTerm);
    const isVisible = matchesCategory && matchesSearch;

    card.hidden = !isVisible;

    if (isVisible) {
      visibleCount += 1;
    }
  });

  productCount.textContent =
    `${visibleCount} ${visibleCount === 1 ? "product" : "products"}`;
  emptyMessage.hidden = visibleCount !== 0;
}

filterButtons.forEach((button) => {
  button.addEventListener("click", () => {
    selectedCategory = button.dataset.filter;

    filterButtons.forEach((filterButton) => {
      const isActive = filterButton === button;
      filterButton.classList.toggle("active", isActive);
      filterButton.setAttribute("aria-pressed", String(isActive));
    });

    updateProducts();
  });
});

searchInput.addEventListener("input", updateProducts);

document.querySelectorAll(".add-button").forEach((button) => {
  button.addEventListener("click", () => {
    const newCount = Number(cartCount.textContent) + 1;
    const productName = button
      .closest(".product-card")
      .querySelector("h3").textContent;

    cartCount.textContent = newCount;
    toast.textContent = `${productName} added to your cart.`;
    toast.classList.add("show");

    window.clearTimeout(toastTimer);
    toastTimer = window.setTimeout(() => {
      toast.classList.remove("show");
    }, 2200);
  });
});
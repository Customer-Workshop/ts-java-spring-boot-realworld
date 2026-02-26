export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  imageUrl: string;
}

export interface CartItem {
  id: number;
  product: Product;
  quantity: number;
}

export interface CartResponse {
  items: CartItem[];
  total: number;
  itemCount: number;
}

const API_BASE = "/api";

export async function fetchProducts(): Promise<Product[]> {
  const res = await fetch(`${API_BASE}/products`);
  if (!res.ok) throw new Error("Failed to fetch products");
  return res.json();
}

export async function fetchProduct(id: number): Promise<Product> {
  const res = await fetch(`${API_BASE}/products/${id}`);
  if (!res.ok) throw new Error("Failed to fetch product");
  return res.json();
}

export async function fetchCart(): Promise<CartResponse> {
  const res = await fetch(`${API_BASE}/cart`);
  if (!res.ok) throw new Error("Failed to fetch cart");
  return res.json();
}

export async function addToCart(
  productId: number,
  quantity: number = 1
): Promise<CartItem> {
  const res = await fetch(`${API_BASE}/cart/items`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ productId, quantity }),
  });
  if (!res.ok) throw new Error("Failed to add to cart");
  return res.json();
}

export async function updateCartItem(
  cartItemId: number,
  quantity: number
): Promise<CartResponse> {
  const res = await fetch(`${API_BASE}/cart/items/${cartItemId}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ quantity }),
  });
  if (!res.ok) throw new Error("Failed to update cart item");
  return res.json();
}

export async function removeFromCart(
  cartItemId: number
): Promise<CartResponse> {
  const res = await fetch(`${API_BASE}/cart/items/${cartItemId}`, {
    method: "DELETE",
  });
  if (!res.ok) throw new Error("Failed to remove from cart");
  return res.json();
}

export async function clearCart(): Promise<void> {
  const res = await fetch(`${API_BASE}/cart`, {
    method: "DELETE",
  });
  if (!res.ok) throw new Error("Failed to clear cart");
}

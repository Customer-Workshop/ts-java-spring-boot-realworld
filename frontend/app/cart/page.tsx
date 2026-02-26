"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import {
  CartResponse,
  fetchCart,
  removeFromCart,
  updateCartItem,
  clearCart,
} from "@/lib/api";

export default function CartPage() {
  const router = useRouter();
  const [cart, setCart] = useState<CartResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadCart = async () => {
    try {
      const data = await fetchCart();
      setCart(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load cart");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCart();
  }, []);

  const handleRemove = async (cartItemId: number) => {
    try {
      const updatedCart = await removeFromCart(cartItemId);
      setCart(updatedCart);
      window.dispatchEvent(new Event("cart-updated"));
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Failed to remove from cart"
      );
    }
  };

  const handleUpdateQuantity = async (
    cartItemId: number,
    newQuantity: number
  ) => {
    if (newQuantity < 1) return;
    try {
      const updatedCart = await updateCartItem(cartItemId, newQuantity);
      setCart(updatedCart);
      window.dispatchEvent(new Event("cart-updated"));
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Failed to update quantity"
      );
    }
  };

  const handleClearCart = async () => {
    try {
      await clearCart();
      setCart({ items: [], total: 0, itemCount: 0 });
      window.dispatchEvent(new Event("cart-updated"));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to clear cart");
    }
  };

  if (loading) {
    return (
      <div style={styles.center}>
        <p style={styles.loadingText}>Loading cart...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div style={styles.center}>
        <p style={styles.errorText}>Error: {error}</p>
      </div>
    );
  }

  if (!cart || cart.items.length === 0) {
    return (
      <div style={styles.emptyContainer}>
        <h1 style={styles.title}>Shopping Cart</h1>
        <div style={styles.emptyCart}>
          <span style={styles.emptyIcon}>&#128722;</span>
          <p style={styles.emptyText}>Your cart is empty</p>
          <button
            onClick={() => router.push("/")}
            style={styles.shopBtn}
          >
            Continue Shopping
          </button>
        </div>
      </div>
    );
  }

  return (
    <div>
      <div style={styles.headerRow}>
        <h1 style={styles.title}>Shopping Cart</h1>
        <button onClick={handleClearCart} style={styles.clearBtn}>
          Clear Cart
        </button>
      </div>
      <div style={styles.cartContainer}>
        <div style={styles.itemsList}>
          {cart.items.map((item) => (
            <div key={item.id} style={styles.cartItem}>
              <div style={styles.itemImageContainer}>
                <img
                  src={item.product.imageUrl}
                  alt={item.product.name}
                  style={styles.itemImage}
                />
              </div>
              <div style={styles.itemDetails}>
                <h3 style={styles.itemName}>{item.product.name}</h3>
                <p style={styles.itemPrice}>
                  ${item.product.price.toFixed(2)}
                </p>
              </div>
              <div style={styles.quantityControls}>
                <button
                  onClick={() =>
                    handleUpdateQuantity(item.id, item.quantity - 1)
                  }
                  style={styles.qtyBtn}
                  disabled={item.quantity <= 1}
                >
                  -
                </button>
                <span style={styles.qtyText}>{item.quantity}</span>
                <button
                  onClick={() =>
                    handleUpdateQuantity(item.id, item.quantity + 1)
                  }
                  style={styles.qtyBtn}
                >
                  +
                </button>
              </div>
              <div style={styles.itemTotal}>
                <p style={styles.totalText}>
                  ${(item.product.price * item.quantity).toFixed(2)}
                </p>
              </div>
              <button
                onClick={() => handleRemove(item.id)}
                style={styles.removeBtn}
                title="Remove item"
              >
                &#10005;
              </button>
            </div>
          ))}
        </div>
        <div style={styles.summary}>
          <h2 style={styles.summaryTitle}>Order Summary</h2>
          <div style={styles.summaryRow}>
            <span>Items ({cart.itemCount})</span>
            <span>${cart.total.toFixed(2)}</span>
          </div>
          <div style={styles.summaryRow}>
            <span>Shipping</span>
            <span style={{ color: "#27ae60" }}>Free</span>
          </div>
          <div style={styles.divider} />
          <div style={{ ...styles.summaryRow, ...styles.totalRow }}>
            <span>Total</span>
            <span>${cart.total.toFixed(2)}</span>
          </div>
          <button style={styles.checkoutBtn}>Proceed to Checkout</button>
          <button
            onClick={() => router.push("/")}
            style={styles.continueBtn}
          >
            Continue Shopping
          </button>
        </div>
      </div>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  center: {
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    minHeight: "50vh",
  },
  loadingText: {
    fontSize: "18px",
    color: "#aaa",
  },
  errorText: {
    fontSize: "18px",
    color: "#e94560",
  },
  emptyContainer: {
    textAlign: "center" as const,
  },
  title: {
    fontSize: "32px",
    fontWeight: "bold",
    color: "#fff",
    marginBottom: "24px",
  },
  emptyCart: {
    display: "flex",
    flexDirection: "column" as const,
    alignItems: "center",
    gap: "16px",
    padding: "60px 20px",
  },
  emptyIcon: {
    fontSize: "64px",
  },
  emptyText: {
    fontSize: "20px",
    color: "#aaa",
  },
  shopBtn: {
    backgroundColor: "#e94560",
    color: "#fff",
    fontSize: "16px",
    fontWeight: "bold",
    padding: "12px 32px",
    borderRadius: "8px",
    border: "none",
    cursor: "pointer",
  },
  headerRow: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: "24px",
  },
  clearBtn: {
    backgroundColor: "transparent",
    color: "#e94560",
    fontSize: "14px",
    fontWeight: 500,
    padding: "8px 16px",
    borderRadius: "6px",
    border: "1px solid #e94560",
    cursor: "pointer",
  },
  cartContainer: {
    display: "flex",
    gap: "32px",
    flexWrap: "wrap" as const,
  },
  itemsList: {
    flex: "1 1 500px",
    display: "flex",
    flexDirection: "column" as const,
    gap: "16px",
  },
  cartItem: {
    backgroundColor: "#16213e",
    borderRadius: "12px",
    padding: "16px",
    display: "flex",
    alignItems: "center",
    gap: "16px",
    flexWrap: "wrap" as const,
  },
  itemImageContainer: {
    width: "80px",
    height: "80px",
    borderRadius: "8px",
    overflow: "hidden",
    flexShrink: 0,
  },
  itemImage: {
    width: "100%",
    height: "100%",
    objectFit: "cover" as const,
  },
  itemDetails: {
    flex: "1 1 150px",
  },
  itemName: {
    margin: "0 0 4px 0",
    fontSize: "16px",
    fontWeight: 600,
    color: "#fff",
  },
  itemPrice: {
    margin: 0,
    fontSize: "14px",
    color: "#aaa",
  },
  quantityControls: {
    display: "flex",
    alignItems: "center",
    gap: "12px",
  },
  qtyBtn: {
    width: "32px",
    height: "32px",
    borderRadius: "6px",
    backgroundColor: "#0f3460",
    color: "#fff",
    fontSize: "18px",
    fontWeight: "bold",
    border: "none",
    cursor: "pointer",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
  },
  qtyText: {
    fontSize: "16px",
    fontWeight: "bold",
    color: "#fff",
    minWidth: "24px",
    textAlign: "center" as const,
  },
  itemTotal: {
    minWidth: "80px",
    textAlign: "right" as const,
  },
  totalText: {
    margin: 0,
    fontSize: "18px",
    fontWeight: "bold",
    color: "#e94560",
  },
  removeBtn: {
    width: "32px",
    height: "32px",
    borderRadius: "50%",
    backgroundColor: "transparent",
    color: "#888",
    fontSize: "14px",
    border: "1px solid #333",
    cursor: "pointer",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    flexShrink: 0,
  },
  summary: {
    flex: "0 0 320px",
    backgroundColor: "#16213e",
    borderRadius: "12px",
    padding: "24px",
    height: "fit-content",
    position: "sticky" as const,
    top: "88px",
  },
  summaryTitle: {
    margin: "0 0 20px 0",
    fontSize: "20px",
    fontWeight: "bold",
    color: "#fff",
  },
  summaryRow: {
    display: "flex",
    justifyContent: "space-between",
    marginBottom: "12px",
    fontSize: "16px",
    color: "#ccc",
  },
  divider: {
    height: "1px",
    backgroundColor: "#333",
    margin: "16px 0",
  },
  totalRow: {
    fontSize: "20px",
    fontWeight: "bold",
    color: "#fff",
  },
  checkoutBtn: {
    width: "100%",
    backgroundColor: "#e94560",
    color: "#fff",
    fontSize: "16px",
    fontWeight: "bold",
    padding: "14px",
    borderRadius: "8px",
    border: "none",
    cursor: "pointer",
    marginTop: "20px",
  },
  continueBtn: {
    width: "100%",
    backgroundColor: "transparent",
    color: "#aaa",
    fontSize: "14px",
    fontWeight: 500,
    padding: "12px",
    borderRadius: "8px",
    border: "1px solid #333",
    cursor: "pointer",
    marginTop: "8px",
  },
};

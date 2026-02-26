"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { fetchCart } from "@/lib/api";

export default function Navbar() {
  const [cartCount, setCartCount] = useState(0);

  const refreshCart = async () => {
    try {
      const cart = await fetchCart();
      setCartCount(cart.itemCount);
    } catch {
      // Cart may not be available yet
    }
  };

  useEffect(() => {
    refreshCart();

    // Listen for custom cart update events
    const handler = () => refreshCart();
    window.addEventListener("cart-updated", handler);
    return () => window.removeEventListener("cart-updated", handler);
  }, []);

  return (
    <nav style={styles.nav}>
      <div style={styles.container}>
        <Link href="/" style={styles.logo}>
          ShopCart
        </Link>
        <div style={styles.links}>
          <Link href="/" style={styles.link}>
            Products
          </Link>
          <Link href="/cart" style={styles.cartLink}>
            <span style={styles.cartIcon}>&#128722;</span>
            Cart
            {cartCount > 0 && <span style={styles.badge}>{cartCount}</span>}
          </Link>
        </div>
      </div>
    </nav>
  );
}

const styles: Record<string, React.CSSProperties> = {
  nav: {
    backgroundColor: "#1a1a2e",
    padding: "0 20px",
    position: "sticky",
    top: 0,
    zIndex: 1000,
    boxShadow: "0 2px 10px rgba(0,0,0,0.3)",
  },
  container: {
    maxWidth: "1200px",
    margin: "0 auto",
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    height: "64px",
  },
  logo: {
    color: "#e94560",
    fontSize: "24px",
    fontWeight: "bold",
    textDecoration: "none",
    letterSpacing: "1px",
  },
  links: {
    display: "flex",
    alignItems: "center",
    gap: "24px",
  },
  link: {
    color: "#eee",
    textDecoration: "none",
    fontSize: "16px",
    fontWeight: 500,
  },
  cartLink: {
    color: "#eee",
    textDecoration: "none",
    fontSize: "16px",
    fontWeight: 500,
    display: "flex",
    alignItems: "center",
    gap: "6px",
    position: "relative" as const,
  },
  cartIcon: {
    fontSize: "20px",
  },
  badge: {
    backgroundColor: "#e94560",
    color: "#fff",
    borderRadius: "50%",
    padding: "2px 7px",
    fontSize: "12px",
    fontWeight: "bold",
    marginLeft: "4px",
  },
};

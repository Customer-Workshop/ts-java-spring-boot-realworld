"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { Product, fetchProduct, addToCart } from "@/lib/api";

export default function ProductPage() {
  const params = useParams();
  const router = useRouter();
  const [product, setProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState(true);
  const [adding, setAdding] = useState(false);
  const [added, setAdded] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const id = Number(params.id);
    if (isNaN(id)) {
      setError("Invalid product ID");
      setLoading(false);
      return;
    }
    fetchProduct(id)
      .then(setProduct)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [params.id]);

  const handleAddToCart = async () => {
    if (!product) return;
    setAdding(true);
    try {
      await addToCart(product.id, 1);
      setAdded(true);
      // Dispatch custom event for navbar badge update
      window.dispatchEvent(new Event("cart-updated"));
      setTimeout(() => setAdded(false), 2000);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to add to cart");
    } finally {
      setAdding(false);
    }
  };

  if (loading) {
    return (
      <div style={styles.center}>
        <p style={styles.loadingText}>Loading product...</p>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div style={styles.center}>
        <p style={styles.errorText}>{error || "Product not found"}</p>
      </div>
    );
  }

  return (
    <div>
      <button onClick={() => router.back()} style={styles.backBtn}>
        &larr; Back to Products
      </button>
      <div style={styles.container}>
        <div style={styles.imageContainer}>
          <img
            src={product.imageUrl}
            alt={product.name}
            style={styles.image}
          />
        </div>
        <div style={styles.details}>
          <h1 style={styles.name}>{product.name}</h1>
          <p style={styles.price}>${product.price.toFixed(2)}</p>
          <p style={styles.description}>{product.description}</p>
          <button
            onClick={handleAddToCart}
            disabled={adding}
            style={{
              ...styles.addToCartBtn,
              ...(added ? styles.addedBtn : {}),
              ...(adding ? styles.disabledBtn : {}),
            }}
          >
            {adding ? "Adding..." : added ? "Added to Cart!" : "Add to Cart"}
          </button>
          {added && (
            <button
              onClick={() => router.push("/cart")}
              style={styles.viewCartBtn}
            >
              View Cart &rarr;
            </button>
          )}
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
  backBtn: {
    background: "none",
    color: "#aaa",
    fontSize: "16px",
    marginBottom: "24px",
    padding: "8px 0",
    cursor: "pointer",
    border: "none",
  },
  container: {
    display: "flex",
    gap: "40px",
    flexWrap: "wrap" as const,
  },
  imageContainer: {
    flex: "1 1 400px",
    maxWidth: "500px",
    borderRadius: "12px",
    overflow: "hidden",
    backgroundColor: "#0f3460",
  },
  image: {
    width: "100%",
    height: "auto",
    display: "block",
  },
  details: {
    flex: "1 1 300px",
    display: "flex",
    flexDirection: "column" as const,
    gap: "16px",
  },
  name: {
    fontSize: "32px",
    fontWeight: "bold",
    color: "#fff",
    margin: 0,
  },
  price: {
    fontSize: "28px",
    fontWeight: "bold",
    color: "#e94560",
    margin: 0,
  },
  description: {
    fontSize: "16px",
    lineHeight: "1.6",
    color: "#ccc",
    margin: 0,
  },
  addToCartBtn: {
    backgroundColor: "#e94560",
    color: "#fff",
    fontSize: "18px",
    fontWeight: "bold",
    padding: "16px 32px",
    borderRadius: "8px",
    border: "none",
    cursor: "pointer",
    marginTop: "16px",
    transition: "background-color 0.2s",
  },
  addedBtn: {
    backgroundColor: "#27ae60",
  },
  disabledBtn: {
    opacity: 0.7,
    cursor: "not-allowed",
  },
  viewCartBtn: {
    backgroundColor: "#0f3460",
    color: "#fff",
    fontSize: "16px",
    fontWeight: 500,
    padding: "12px 24px",
    borderRadius: "8px",
    border: "2px solid #e94560",
    cursor: "pointer",
  },
};

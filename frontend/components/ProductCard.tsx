"use client";

import Link from "next/link";
import { Product } from "@/lib/api";

interface ProductCardProps {
  product: Product;
}

export default function ProductCard({ product }: ProductCardProps) {
  return (
    <Link href={`/product/${product.id}`} style={styles.card}>
      <div style={styles.imageContainer}>
        <img
          src={product.imageUrl}
          alt={product.name}
          style={styles.image}
        />
      </div>
      <div style={styles.content}>
        <h3 style={styles.name}>{product.name}</h3>
        <p style={styles.description}>
          {product.description.length > 80
            ? product.description.substring(0, 80) + "..."
            : product.description}
        </p>
        <div style={styles.footer}>
          <span style={styles.price}>${product.price.toFixed(2)}</span>
          <span style={styles.viewBtn}>View Details</span>
        </div>
      </div>
    </Link>
  );
}

const styles: Record<string, React.CSSProperties> = {
  card: {
    backgroundColor: "#16213e",
    borderRadius: "12px",
    overflow: "hidden",
    textDecoration: "none",
    color: "#eee",
    transition: "transform 0.2s, box-shadow 0.2s",
    boxShadow: "0 4px 15px rgba(0,0,0,0.2)",
    display: "flex",
    flexDirection: "column",
  },
  imageContainer: {
    width: "100%",
    height: "200px",
    overflow: "hidden",
    backgroundColor: "#0f3460",
  },
  image: {
    width: "100%",
    height: "100%",
    objectFit: "cover",
  },
  content: {
    padding: "16px",
    display: "flex",
    flexDirection: "column",
    flexGrow: 1,
  },
  name: {
    margin: "0 0 8px 0",
    fontSize: "18px",
    fontWeight: 600,
    color: "#fff",
  },
  description: {
    margin: "0 0 16px 0",
    fontSize: "14px",
    color: "#aaa",
    lineHeight: "1.4",
    flexGrow: 1,
  },
  footer: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
  },
  price: {
    fontSize: "20px",
    fontWeight: "bold",
    color: "#e94560",
  },
  viewBtn: {
    backgroundColor: "#0f3460",
    color: "#fff",
    padding: "8px 16px",
    borderRadius: "6px",
    fontSize: "14px",
    fontWeight: 500,
  },
};

import type { Metadata } from "next";
import "./globals.css";
import Navbar from "@/components/Navbar";

export const metadata: Metadata = {
  title: "ShopCart - Shopping Cart App",
  description: "A shopping cart application built with Next.js 15 and Spring Boot",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>
        <Navbar />
        <main style={{ maxWidth: "1200px", margin: "0 auto", padding: "24px 20px" }}>
          {children}
        </main>
      </body>
    </html>
  );
}

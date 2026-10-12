import type { Metadata, Viewport } from "next";
import { Inter, Playfair_Display } from "next/font/google";
import "./globals.css";
import Providers from "./providers";
import { Navbar } from "@/components/layout/Navbar";
import { Footer } from "@/components/layout/Footer";
import { PageTransition } from "@/components/layout/PageTransition";

const inter = Inter({
  variable: "--font-inter",
  subsets: ["latin"],
  display: "swap",
});

const playfair = Playfair_Display({
  variable: "--font-playfair",
  subsets: ["latin"],
  display: "swap",
});

export const metadata: Metadata = {
  title: "CinePulse — Discover 1.6M+ Movies & TV Shows",
  description:
    "Search, track, and discover movies and TV shows. Powered by a data-first catalog of 1.67 million titles.",
  openGraph: {
    title: "CinePulse",
    description: "Discover your next favorite film.",
    type: "website",
  },
};

export const viewport: Viewport = {
  themeColor: "#0a0a0f",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html
      lang="en"
      className={`${inter.variable} ${playfair.variable} dark`}
      data-scroll-behavior="smooth"
    >
      <body className="min-h-screen flex flex-col bg-[#0a0a0f] text-[#f5f5f7] antialiased">
        <Providers>
          <Navbar />
          <PageTransition>{children}</PageTransition>
          <Footer />
        </Providers>
      </body>
    </html>
  );
}

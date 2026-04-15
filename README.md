<div align="center">

# 🛍️ ShopFlow — Inventory & Billing System

**A professional, offline-first desktop POS & inventory management application built entirely in Java.**

![Java](https://img.shields.io/badge/Java-8%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Swing](https://img.shields.io/badge/UI-Java%20Swing-4F46E5?style=for-the-badge)
![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-0078d7?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)
![Status](https://img.shields.io/badge/Status-Active-brightgreen?style=for-the-badge)

*No database. No internet. No external libraries. Just pure Java.*

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Screenshots](#-screenshots)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Data Models](#-data-models)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Running the App](#running-the-app)
- [Usage Guide](#-usage-guide)
- [How PDF Generation Works](#-how-pdf-generation-works)
- [Data Persistence](#-data-persistence)
- [Default Product Categories](#-default-product-categories)
- [Known Limitations](#-known-limitations)
- [Future Improvements](#-future-improvements)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🌟 Overview

**ShopFlow** is a fully self-contained desktop application for small shop owners and retail businesses who need a fast, reliable Point-of-Sale (POS) and inventory tracking system — without depending on the internet, a standalone database engine, or paid third-party software.

Built as a **Java Mini Project**, ShopFlow demonstrates advanced use of Java Swing for rich desktop UIs, object serialization for file-based persistence, and raw PDF byte-stream generation — all within a single `.java` source file.

> **Ideal for:** Retail shops, college project demos, small businesses, and Java learners who want to study a complete real-world GUI application.

---

## ✨ Features

### 📊 Dashboard
- Live stat cards showing: **Total Products**, **Inventory Value** (at buy price), **Low Stock count**, **Total Revenue**, and **Total Profit**
- **Low Stock Alerts** panel — highlights any product whose quantity has dropped to or below its minimum threshold
- **Recent Sales** panel — shows the last 6 transactions at a glance with customer name, time, and total amount
- Auto-refreshes whenever data changes anywhere in the app

### 📦 Product Management
- Add, edit, and delete products through a clean modal dialog
- Fields: **Name**, **Category**, **Barcode/SKU**, **Buy Price**, **Sell Price**, **GST Rate (%)**, **Stock Quantity**, **Minimum Stock Level**
- **Custom Categories** — add your own business-specific categories beyond the built-in defaults
- **Search & Filter** — instant text search across name, category, and barcode; dropdown filter by category
- Color-coded **status column**: `OK` (green) vs `Low Stock` (red)
- **Update Stock** button for quick restocking without editing the full product record
- Inventory value calculated per product: `Buy Price × Stock Qty`

### 🧾 Billing / Point of Sale
- **Live product list** with real-time search — automatically hides out-of-stock items
- Add multiple products to cart with custom quantities
- **Overstock protection** — warns if requested quantity exceeds available stock, respecting items already in the cart
- Cart table showing: Product, Unit Price, Qty, GST breakdown, and Subtotal per item
- Live **totals panel**: Subtotal, Total GST, and Grand Total
- **Customer name** field (required before generating a bill)
- **Custom PDF invoice naming** — choose a filename before saving
- On bill generation: stock levels auto-deduct and all panels refresh

### 📄 PDF Invoice Generation
- Generates fully compliant **PDF 1.4** files **with no external libraries** — pure Java byte-stream construction
- Invoice includes: Bill No., Date & Time, Customer Name, itemized table (Product, Price, Qty, GST, Subtotal), Subtotal, Total GST, and Grand Total
- Invoices saved to `invoices/` folder in the working directory
- **Auto-opens** the PDF after generation using the system's default PDF viewer

### 📈 Sales History
- Complete transaction ledger showing all past sales
- Columns: Bill No., Date & Time, Customer, No. of Items, Subtotal, Total GST, Grand Total, Profit
- **Profit tracking** per sale (Sell Price − Buy Price × Qty)
- **Open PDF Invoice** button — opens an existing invoice or regenerates it if the file was deleted
- Aggregate Revenue and Profit totals shown in the header

---

## 📸 Screenshots

> *Screenshots coming soon. Run the application to see the full UI.*

| Dashboard | Products |
|---|---|
| Live KPI cards + alerts | Filterable product table |

| Billing | Sales History |
|---|---|
| POS-style cart checkout | Full transaction ledger |

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 8+ |
| UI Framework | Java Swing & AWT |
| Layout Managers | `BorderLayout`, `GridLayout`, `BoxLayout`, `CardLayout`, `FlowLayout`, `GridBagLayout` |
| Persistence | Java Object Serialization (`ObjectOutputStream` / `ObjectInputStream`) |
| PDF Engine | Custom raw PDF 1.4 byte-stream builder (zero dependencies) |
| Data Structures | `ArrayList`, `LinkedHashSet`, Java Streams API |
| Date/Time | `java.time` (LocalDate, LocalDateTime, DateTimeFormatter) |
| File I/O | `java.io`, `java.nio.file` |

---

## 🗂️ Project Structure

```
Java Programming/
│
├── Program Files/
│   └── Inventory & Billing System.java   # Complete application (single file)
│
├── Run_ShopFlow.bat                       # Windows launcher — compile & run
├── README.md                              # This file
│
# Auto-generated at runtime:
├── shopflow_data.ser                      # Serialized data (products, sales, categories)
└── invoices/
    ├── 1.pdf
    ├── 2.pdf
    └── ...                               # Generated PDF invoices
```

---

## 🏗️ Data Models

The application uses three serializable data classes:

### `Product`
| Field | Type | Description |
|---|---|---|
| `id` | `int` | Auto-incremented unique identifier |
| `name` | `String` | Product display name |
| `cat` | `String` | Category label |
| `barcode` | `String` | Optional barcode / SKU (nullable) |
| `buyPrice` | `double` | Cost price (used for inventory valuation & profit calc) |
| `sellPrice` | `double` | Selling price (used in billing) |
| `gstRate` | `double` | GST percentage (e.g., `18.0` for 18%) |
| `qty` | `int` | Current stock quantity |
| `min` | `int` | Minimum stock threshold for low-stock alert |

### `BillItem`
| Field | Type | Description |
|---|---|---|
| `p` | `Product` | Reference to the product |
| `qty` | `int` | Quantity added to this bill |

> Computed: `sub()` = sell price × qty | `gst()` = subtotal × (gstRate / 100) | `profit()` = (sell − buy) × qty

### `Sale`
| Field | Type | Description |
|---|---|---|
| `no` | `String` | Sequential bill number |
| `date` | `String` | Timestamp (`yyyy-MM-dd HH:mm:ss`) |
| `customer` | `String` | Customer name |
| `items` | `List<BillItem>` | Snapshot of cart at time of sale |
| `sub`, `gst`, `total`, `profit` | `double` | Aggregate financial figures |
| `invoiceFile` | `String` | Filename of the generated PDF |

---

## 🚀 Getting Started

### Prerequisites

- **Java JDK 8 or higher** must be installed.

Verify your Java installation:
```bash
java -version
# Expected: java version "1.8.x" or higher
```

If Java is not installed, download it from [https://adoptium.net](https://adoptium.net) or [https://www.oracle.com/java](https://www.oracle.com/java).

---

### Installation

**1. Clone the repository**
```bash
git clone https://github.com/your-username/ShopFlow.git
cd ShopFlow
```

**2. (Optional) Verify the structure**
```
ShopFlow/
├── Program Files/
│   └── Inventory & Billing System.java
└── Run_ShopFlow.bat
```

---

### Running the App

#### ✅ Option 1 — Windows (Easiest)
Simply **double-click** `Run_ShopFlow.bat`. It will:
1. Navigate into `Program Files/`
2. Compile the Java source
3. Launch the application in a background window (`javaw`)

#### ✅ Option 2 — Manual (All Platforms)
```bash
# Step into the source directory
cd "Program Files"

# Create output directory (first time only)
mkdir bin

# Compile
javac -d bin -encoding UTF-8 "Inventory & Billing System.java"

# Run
java -cp bin InventoryManagementSystem
```

> **Note:** The compiled class is still named `InventoryManagementSystem` internally. The filename uses spaces, which is handled by quoting it during compilation.

---

## 📖 Usage Guide

### 1️⃣ Adding Your First Products
1. Click **Products** in the left sidebar
2. Click **+ Add Category** to create categories specific to your business (optional)
3. Click **+ Add Product** and fill in:
   - Product Name, Category, Barcode (optional)
   - Buy Price (your cost), Sell Price (what you charge), GST %
   - Stock Quantity, Minimum Stock (triggers low-stock alert)
4. Click **Add** — the product instantly appears in the table

### 2️⃣ Making a Sale / Generating a Bill
1. Click **Billing** in the sidebar
2. Search or scroll to find a product in the left panel (out-of-stock items are hidden)
3. Set the quantity and click **Add to Cart**
4. Repeat for multiple products
5. Enter the **Customer Name** (required)
6. Click **Generate PDF Invoice**
7. Enter a custom filename (or keep the default) → PDF is saved and auto-opened

### 3️⃣ Updating Stock (Restocking)
1. Go to **Products**
2. Select the product row
3. Click **Update Stock** → enter the new quantity → confirm

### 4️⃣ Editing or Removing Products
- Select a row → **Edit** to modify any field
- Select a row → **Delete** to permanently remove the product

### 5️⃣ Viewing Sales & Invoices
1. Click **Sales History**
2. All past transactions are listed with full financial breakdown
3. Select a row → **Open PDF Invoice** to view the receipt
   - If the PDF file exists on disk, it opens directly
   - If the file was deleted, it is automatically regenerated from saved data

---

## 📄 How PDF Generation Works

ShopFlow includes a **hand-crafted PDF 1.4 generator** written entirely in Java using raw byte-stream construction — no Apache PDFBox, iText, or any other library.

**How it works:**
1. An in-memory `ByteArrayOutputStream` is built following the PDF cross-reference (xref) table standard
2. The content stream uses **PDF operators** (`BT`, `Tf`, `Td`, `Tj`, `ET`) to position and render text
3. Two embedded fonts are declared: `Courier` (regular) and `Courier-Bold` (headers/totals)
4. The complete binary PDF is written to disk using `java.nio.file.Files.write()`

**Invoice layout includes:**
- Header: `SHOPFLOW - TAX INVOICE`
- Bill No., Date, Customer Name
- Itemized table: Product name (truncated to 26 chars), Unit Price, Qty, GST breakdown, Subtotal
- Footer: Subtotal, Total GST, Grand Total (bold), thank-you message

---

## 💾 Data Persistence

All application data is saved to **`shopflow_data.ser`** using Java's built-in Object Serialization.

| Saved | Description |
|---|---|
| `List<Product>` | All products and their current state |
| `List<Sale>` | Full sales history with all bill items |
| `int PID` | Next product ID counter |
| `int BILL` | Bill number counter |
| `List<String>` | Custom categories added by user |

**Save trigger:** Data is saved automatically when the application closes via a JVM **shutdown hook** (`Runtime.getRuntime().addShutdownHook(...)`).

**Load trigger:** Data is loaded immediately on application start before the UI is rendered.

> ⚠️ Do not manually edit or delete `shopflow_data.ser` unless you intend to reset all data.

---

## 🏷️ Default Product Categories

The following categories are built in by default:

`Electronics` · `Groceries` · `Clothing` · `Footwear` · `Furniture` · `Kitchen` · `Stationery` · `Other`

You can add unlimited custom categories via the **+ Add Category** button in the Products panel.

---

## ⚠️ Known Limitations

| Limitation | Details |
|---|---|
| Single-user only | No multi-user or network support |
| No database | Uses Java serialization; not suitable for very large datasets (10,000+ products) |
| PDF fonts | Only Courier (monospaced) is used; no Unicode/regional language support |
| No backup system | Data is stored in a single `.ser` file; manual backups are recommended |
| No user authentication | No login system; anyone with access to the machine can use the app |
| Invoice editing | Invoices cannot be edited after generation; a new bill must be created |

---

## 🔮 Future Improvements

- [ ] **Database integration** (SQLite or H2) for robust storage
- [ ] **User authentication** with admin/cashier roles
- [ ] **Barcode scanner** hardware integration
- [ ] **Export to CSV/Excel** for sales reports
- [ ] **Unicode PDF support** for regional languages and currency symbols
- [ ] **Multi-store / branch** management
- [ ] **Supplier management** module for purchase orders
- [ ] **Discount & coupon** support at billing
- [ ] **Dark mode** theme toggle
- [ ] **Keyboard shortcuts** for faster POS operation

---

## 🤝 Contributing

Contributions are welcome! Here's how to get started:

1. **Fork** this repository
2. **Create a branch** for your feature:
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Commit your changes** with a clear message:
   ```bash
   git commit -m "feat: add barcode scanner support"
   ```
4. **Push** to your fork and open a **Pull Request**

Please keep contributions scoped and well-commented. Large refactors should be discussed via an Issue first.

---

## 👨‍💻 Author

**Ashish** — Java Programming Mini Project  
B.Tech / BCA Student | Passionate about building practical software

---

## 📜 License

This project is licensed under the **MIT License** — you are free to use, modify, and distribute it with attribution.

```
MIT License — Copyright (c) 2026 Ashish
```

---

<div align="center">

⭐ **If this project helped you, please give it a star!** ⭐  
*Built with ❤️ using pure Java*

</div>

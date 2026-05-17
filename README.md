# SmartFinance 💰

**Personal Finance Manager** — 100% offline, dark theme, built with Jetpack Compose + Room.

Lightweight Android app to track your income and expenses with visual analytics, gamification, and multi-language support.

---

## ✨ Features

- **Dashboard** — Monthly income/expense overview with a dynamic ring chart that changes color based on your financial health (green ≤ 50%, yellow 51–85%, red > 85%)
- **Quick-Add** — One-tap buttons with your top 3 most used transaction names (incomes and expenses)
- **Transactions** — Add, edit, and delete any transaction with name, amount, place, and category
- **Analytics** — Donut chart of expenses by category + a **mascot** that changes based on your dominant spending (Bear for Food, Raccoon for Shopping, Cheetah for Transport, etc.)
- **Settings** — Language toggle EN/ES, Assets List view, Account management
- **Account** — Profile with name/email, email verification code (sent via device email app), Gmail link, monthly report toggle
- **PDF Reports** — Generate a monthly PDF report with summary, pie chart, and category breakdown — share it from any app
- **Dark Theme** — Eye-friendly dark mode (`#121212` / `#1A1A24`) with pastel accent colors
- **Offline** — All data stored locally in Room (SQLite). No internet required.

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose + Material3 |
| Navigation | Navigation Compose |
| Architecture | MVVM (ViewModel + StateFlow) |
| Database | Room (SQLite) |
| Annotation Processing | KAPT |
| Charts | Custom Canvas (RingChart, PieChart) |
| PDF | Android PdfDocument |
| Email | JavaMail + Intent fallback |
| Language | Android String Resources (EN / ES) |

---

## 📸 Screens

| Screen | Description |
|---|---|
| `DashboardScreen` | Financial cards, health ring chart, quick-add buttons, analytics link |
| `TransactionFormScreen` | Add transaction with type, name, amount, place, category |
| `EditTransactionScreen` | Modify or update an existing transaction |
| `AnalyticsScreen` | Expense pie chart, legend, mascot avatar |
| `SettingsScreen` | Language switch, assets list navigation, account link, logout |
| `AccountScreen` | Profile info, email verification, Gmail link, monthly report, PDF generation |
| `AssetsListScreen` | Scrollable list of incomes or expenses with edit/delete |

---

## 🚀 Getting Started

1. Open the project in **Android Studio**
2. Sync Gradle (AGP 9.2.1 + Kotlin 2.0.21)
3. Run on an emulator or physical device (min SDK 24)
4. Tap **"Load Sample Data"** on the dashboard to populate test transactions
5. Explore: tap the 👤 icon for Account, ⚙️ icon for Settings

---

## 🧪 Build

```bash
./gradlew assembleDebug
```

APK location: `app/build/outputs/apk/debug/app-debug.apk`

---

## 📄 License

MIT

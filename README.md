# KFC Delivery Android App 🍗🛵

A comprehensive, full-stack Android application built in Kotlin that simulates a complete, real-time food delivery ecosystem. This project features a robust 4-tier role system, allowing Customers, Kitchen Staff, Delivery Riders, and Branch Admins to interact seamlessly through a centralized Firebase Cloud Firestore database.

## 🚀 Key Features

### 👤 Customer Portal
- **Browse & Shop:** Dynamic menu loaded in real-time from the cloud.
- **Cart & Checkout:** Interactive cart management with total calculation and delivery address input.
- **Live Order Tracking:** Real-time status updates (Pending ➔ Preparing ➔ Out for Delivery ➔ Delivered).
- **Profile & Order History:** View past orders, track active ones, and cancel pending orders.

### 👑 Admin Dashboard (Manager)
- **Menu Management:** Toggle item availability (in-stock/out-of-stock), edit prices, and upload new item photos directly from the device to Firebase Storage.
- **Order Triage:** Accept incoming customer orders and route them to the kitchen.
- **Fleet Management:** Approve pending rider applications and assign ready orders to specific delivery riders.
- **Staff Control:** Register new kitchen staff accounts on the fly.

### 👨‍🍳 Kitchen Staff Portal
- **Live Queue:** Instantly receive orders accepted by the Admin.
- **Workflow Tracking:** Mark orders as "Preparing" while cooking, and "Ready for Pickup" when bagged for the rider.

### 🛵 Delivery Rider Portal
- **Onboarding:** Built-in application form for new riders (requires Admin approval).
- **Assigned Deliveries:** Receive orders specifically routed to them by the Admin.
- **Delivery Lifecycle:** Update statuses to "Out for Delivery" upon leaving the branch and "Delivered" upon arrival.

## 🛠️ Tech Stack & Architecture
- **Language:** Kotlin
- **UI Framework:** Android XML / Material Design
- **Backend (BaaS):** Firebase (Firestore Realtime Database)
- **Storage:** Firebase Cloud Storage (for dynamic menu images)
- **Image Loading:** Glide (seamlessly handles local drawables and remote HTTPS images)
- **Architecture:** Multi-Activity flow with RecyclerViews and Realtime Snapshot Listeners

## 📸 How it Works
The application leverages Firestore's `addSnapshotListener` to ensure that when one role updates a document (e.g., a cook marks an order as "Ready"), the UI instantly refreshes on the screens of all other relevant roles (the Customer sees it's ready, and the Admin sees it's time to assign a rider) without needing to manually refresh.

# BrewHaven Café App

BrewHaven is a mobile café application that allows customers to browse menu items, mark favourites, manage a shopping cart, place orders, and leave feedback.  
The app is built with Kotlin, XML layouts, Firebase Authentication, and Firestore.

---

## Features

### User Accounts
- Create an account with email and password
- Sign in securely
- Validation checks for strong passwords
- Keeps users logged in between sessions

### Menu Browsing
- View all drinks and food categories
- View full item details: price, image, calories, allergens, description
- Smooth navigation between menu and item detail pages

### Favourites
- Mark or unmark items as favourites
- View a dedicated favourites list

### Cart
- Add items with custom quantity
- Increase, decrease, or remove items
- Total price updates automatically
- Checkout creates a new order

### Orders
- View all past orders
- Each order shows date, time, total, and status
- Automatically updates when a new order is placed

### Feedback
- Leave a written comment and a 1–5 star rating
- Stored under the user’s account

---

## Firebase Structure

### Public data:
- `menu_items` – all café products

### Per-user data:
- `customers/{uid}/favorites`
- `customers/{uid}/cart`
- `customers/{uid}/orders`
- `customers/{uid}/feedback`

Firebase Authentication is used for all account handling.

---

## How the App Works

When the user opens the app:
- They first see a splash screen
- If signed in → they are taken straight to the menu
- If not signed in → they go to the welcome screen
- After signing in → bottom navigation appears with four tabs:
    - Menu
    - Likes
    - Cart
    - Orders

Data such as favourites, cart items, and orders is synced live with Firestore.

---

## Screens Included

- Splash
- Welcome
- Login
- Sign Up
- Menu (categories)
- Store List (items in each category)
- Item Detail
- Favourites
- Cart
- Orders
- Feedback

All screens are built with XML layouts and Material components.

---

## Tech Used

- Kotlin
- Android XML Layouts
- Firebase Authentication
- Firestore Database
- Material Components
- RecyclerView for lists

---

## Summary

BrewHaven provides a complete customer-side café experience:
- Authentication
- Menu browsing
- Cart + ordering
- Favourites
- Feedback
- Persistent data through Firebase

It offers a smooth, organised UI and clear navigation through all customer features.



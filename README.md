# RythmScouts

**Owners:**  
Halalisile Mzobe ST10355256  
Mmangaliso Malunga ST10296771  
Mvelo Mkhize ST10291856  

RythmScouts is an Android mobile application for discovering, browsing, and saving music events. The app allows users to create accounts, sign in, manage their events, and authenticate with social providers. It is designed for music enthusiasts who want a simple and organized way to keep track of upcoming events.

[Demo Video]
---

## Table of Contents

- [App Overview](#app-overview)
- [Features](#features)
- [Architecture & APIs](#architecture--apis)
- [Screens](#screens)
- [Technologies Used](#technologies-used)
- [Installation](#installation)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

---

## App Overview

RythmScouts lets users:

1. Sign up using email/password or Google Sign-In  
2. Log in and manage their profile  
3. Reset passwords if forgotten  
4. Browse music events fetched from a REST API  
5. Save favorite events to a Firebase-backed database  
6. Receive feedback on form inputs through Material Design error messages  

The app is designed with **Material Design 3** for a clean and consistent user experience.  

---

## Features

- **User Authentication**
  - Email/password sign-up and sign-in  
  - Google OAuth sign-in  
  - Password reset functionality  

- **Event Management**
  - Fetch events from a REST API  
  - Save favorite events to Firebase Realtime Database or Firestore  
  - Display saved events in a dedicated “My Events” screen  

- **Form Validation**
  - Live validation for username, email, and password fields  
  - Confirm password checks  
  - Terms and privacy policy agreement validation  

- **UI/UX**
  - Responsive layouts using ConstraintLayout and ScrollView  
  - Material 3 TextInputLayouts with outlined boxes  
  - Snackbars for instant feedback  
  - Buttons with icons and custom styles  

---

## Architecture & APIs

- **MVVM / Fragment-based Architecture**: Keeps UI, data, and business logic separated  
- **Firebase Authentication**: For managing user accounts  
- **Firebase Realtime Database / Firestore**: For storing user-specific event data  
- **REST API Integration**: Fetches event details (name, date, location, image)  
  - GET `/events` – fetch all events  
  - GET `/events/{id}` – fetch event details  
  - POST `/users/{id}/savedEvents` – save an event to a user’s account  
- **Google Sign-In**: OAuth integration for easy authentication  
- **Material Components Library**: For consistent UI  

---

## Screens

- **Sign Up** – Register with email/password or Google  
- **Sign In** – Login to existing account  
- **Reset Password** – Send a password reset email  
- **My Events** – Browse and view saved events  
- **Event Details** – View detailed information about each event  

---

## Technologies Used

- **Kotlin** – Main programming language  
- **Android SDK** – App framework  
- **Material Design 3** – UI components  
- **Firebase Authentication** – User account management  
- **Firebase Realtime Database / Firestore** – Event storage  
- **Google Sign-In API** – Social login  
- **Retrofit / OkHttp** – REST API calls  

---

## Installation

1. Clone the repository:  
   ```bash
   git clone https://github.com/your-username/rythmscouts.git

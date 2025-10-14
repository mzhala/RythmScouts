# RythmScouts

**Owners:**  
Halalisile Mzobe ST10355256  
Mmangaliso Malunga ST10296771  
Mvelo Mkhize ST10291856  

RythmScouts is an Android mobile application for discovering, browsing, and saving music events. The app allows users to create accounts, sign in, manage their events, and authenticate with social providers. It is designed for music enthusiasts who want a simple and organized way to keep track of upcoming events.

[Demo Video]
---
https://youtu.be/5GU_4Ycl4D8

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


## Usage
Account Creation: Sign up using email/password or Google Sign-In

Event Discovery: Browse events on the Explore screen

Save Events: Tap the save button on events you're interested in

Manage Profile: Update your information in the Settings screen

Track Events: View your saved events in the My Events section

Current Status & Roadmap
✅ Implemented Features
User Authentication (Google Sign-In & Email/Password)

User Profile Management

Event Discovery Interface

Event Saving Functionality

Dark Mode Support

Settings & Preferences

Form Validation & Error Handling

🚧 In Progress
Eventbrite API Integration

Real Event Data Population

Push Notifications

Advanced Search Filters

📅 Planned Features
Social Features (Event Sharing)

Ticket Purchase Integration

Artist Following

Multi-language Support

GitHub Actions CI/CD
Automated Workflows
ci.yml - Continuous Integration on every push

build.yml - APK building and linting

security.yml - Weekly security scans

Quality Gates
✅ Unit Test Execution

✅ Code Linting & Analysis

✅ Security Vulnerability Scanning

✅ Build Success Verification

## Contributing
We welcome contributions from the community! Please see our Contributing Guidelines for details.

🐛 Reporting Issues
Use the GitHub Issues tab

Include Android version and device information

Provide detailed steps to reproduce

Attach relevant screenshots or logs

💡 Feature Requests
Open an issue with the "enhancement" label

Describe the use case and expected behavior

Consider implementation complexity

## License
This project is licensed under the MIT License - see the LICENSE file for details.

Support & Contact
Developers:

Halalisile Mzobe

Mmangaliso Malunga

Mvelo Mkhize

Repository: https://github.com/mzhala/RythmScouts

Issues: GitHub Issues

<div align="center">
🎵 Built for music lovers, by music lovers 🎵
Never miss another beat with RythmScouts

<img width="100" height="100" alt="ic_logo" src="https://github.com/user-attachments/assets/97cbdd4a-dd18-4c9c-b32e-4eb949c268fb" />


</div>
Project started: October 2025 | Last updated: October 2025

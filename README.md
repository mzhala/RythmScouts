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
- [Screenshots](#screenshots)
- [Technical Stack](#technical-stack)
- [GitHub Actions CI/CD](#github-actions-cicd)
- [Support & Contact](#support--contact)

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

### 🔐 **User Authentication**
- **Email/password sign-up and sign-in** - Traditional registration with validation
- **Google OAuth sign-in** - One-tap login with Google accounts
- **Password reset functionality** - Firebase-powered recovery system
- **Secure Session Management** - Automatic login persistence

### 🎵 **Event Management**
- **Fetch events from a REST API** - Browse events with search and filters
- **Save favorite events** to Firebase Realtime Database
- **Location-based discovery** - Find events in Cape Town and other cities
- **Event Categories** - Comedy, Musicals, Concerts, and more
- **Display saved events** in a dedicated "My Events" screen

### 👤 **User Profile & Preferences**
- **Personal Information** - Manage username, email, and profile details
- **Dark Mode Support** - Complete dark theme implementation
- **Preferences Management** - Language and notification settings
- **Account Security** - Password changes and security settings

### 🎨 **UI/UX Excellence**
- **Material Design 3** - Modern, responsive interface
- **Live Form Validation** - Real-time validation for username, email, and password fields
- **Confirm password checks** and terms agreement validation
- **Snackbars for instant feedback** - Enhanced user communication
- **Responsive layouts** using ConstraintLayout and ScrollView
- **Smooth Animations** - Enhanced user interactions

---

## Architecture & APIs

### 🏗 **Software Architecture**
- **MVVM / Fragment-based Architecture**: Keeps UI, data, and business logic separated  
- **Repository Pattern**: Data abstraction layer for clean architecture
- **Dependency Management**: Clean separation of concerns

### 🔥 **Backend Services**
- **Firebase Authentication**: For managing user accounts  
- **Firebase Realtime Database**: For storing user-specific event data  
- **Firebase Analytics**: Usage tracking and insights
- **Google Sign-In API**: OAuth integration for easy authentication

Got it! Here’s your **API Integration** section rewritten to perfectly match your `TicketmasterResponse` model and setup, formatted for GitHub README:

---

### 🌐 **API Integration**

* **Ticketmaster Discovery API**: Provides real-time event data including name, date, venue, city, and images.

  * `GET /discovery/v2/events` – Fetch a list of events based on filters such as keyword, city, or date range.
  * `GET /discovery/v2/events/{id}` – Retrieve detailed information for a specific event including venue, country, and location data.
  * `POST /users/{id}/savedEvents` – Save selected events to the user’s Firebase account for tracking in the “My Events” section.

* **Retrofit / OkHttp**: Used for all network requests to the Ticketmaster API, providing asynchronous handling and efficient response parsing.

* **API Response Model**: Maps to the Ticketmaster data structure using Kotlin data classes such as:

  * `TicketmasterResponse`, `Event`, `Image`, `Event Dates`, `Ticket startDateTime`, `Ticket endDateTime`, `Venue`, `Latitude`, `Longitude`, `City` and `Country`.

* **API Authentication**: Access is secured using a private Ticketmaster API key stored in the project configuration file.

---


---

## Screens

- **Welcome Screen** - App introduction and main entry point
- **Sign Up** – Register with email/password or Google  
- **Sign In** – Login to existing account  
- **Reset Password** – Send a password reset email  
- **Home** - Event discovery and recommendations
- **Explore Events** - Browse all available events
- **My Events** – Browse and view saved events  
- **Event Details** – View detailed information about each event
- **User Profile** - Manage personal information and preferences
- **Settings** - App configuration and account settings

---

## Technologies Used

- **Kotlin** – Main programming language  
- **Android SDK** – App framework  
- **Material Design 3** – UI components  
- **Firebase Authentication** – User account management  
- **Firebase Realtime Database** – Event storage  
- **Google Sign-In API** – Social login  
- **Retrofit / OkHttp** – REST API calls
- **Android Jetpack Components**:
  - ViewBinding - Type-safe view access
  - Navigation Component - Fragment management
  - LiveData & ViewModel - Data persistence
  - Lifecycle-Aware Components - Efficient resource management

---

## Screenshots

<div align="center">

### Authentication & Onboarding
| Welcome Screen | Sign In | Sign Up |
|----------------|---------|---------|
| ![WhatsApp Image 2025-10-14 at 23 26 00 (2)](https://github.com/user-attachments/assets/fa85a269-fa04-49d0-9b72-88606d0928d8)
 |  ![WhatsApp Image 2025-10-14 at 23 26 00 (1)](https://github.com/user-attachments/assets/bd922391-c899-4a5b-bfc7-e3ec65b7b5b8)
| ![WhatsApp Image 2025-10-14 at 23 25 59 (3)](https://github.com/user-attachments/assets/1ec14794-25f9-46a2-993e-5b850922b994)
 |![WhatsApp Image 2025-10-14 at 23 26 00](https://github.com/user-attachments/assets/0cdcd5f7-6d57-4e26-9b19-4cd201a66c71)


### Core Features
| Home Discovery | Event Exploration | Saved Events |
|----------------|-------------------|--------------|
|![WhatsApp Image 2025-10-14 at 23 25 59 (2)](https://github.com/user-attachments/assets/6face01e-b9c3-4584-b3a9-83751b3668bf)| ![WhatsApp Image 2025-10-14 at 23 25 59 (1)](https://github.com/user-attachments/assets/a6ba8467-7da4-4259-95d4-e9efa69c8002)
 | ![WhatsApp Image 2025-10-14 at 23 25 59](https://github.com/user-attachments/assets/88c7dab6-c339-406f-8b2c-3eceeb16ad3c)|

### User Experience
| User Profile | Settings | Event Details |
|--------------|----------|---------------|
| ![WhatsApp Image 2025-10-14 at 23 25 58 (1)](https://github.com/user-attachments/assets/4daf630e-ae5d-483d-bdc0-0f84ec1d8edc) | ![WhatsApp Image 2025-10-14 at 23 25 58](https://github.com/user-attachments/assets/70a9cbdd-032a-4b2c-b469-faa1ef038822)|
</div>

---

## Technical Stack

### 📱 **Frontend Architecture**
- **Kotlin** - Primary programming language
- **Android Jetpack Components**:
  - `ViewBinding` - Type-safe view access
  - `Navigation Component` - Fragment management
  - `LiveData & ViewModel` - Data persistence
  - `Lifecycle-Aware Components` - Efficient resource management

### 🔥 **Backend & Services**
- **Firebase Authentication** - User management & security
- **Firebase Realtime Database** - User profiles & event data
- **Firebase Analytics** - Usage tracking & insights
- **Google Sign-In Integration** - Social authentication

---

## Installation

### Prerequisites
- Android Studio Hedgehog or later
- JDK 11 or higher
- Android SDK API 25+
- Firebase project with Authentication enabled

### 🔧 Development Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/mzhala/RythmScouts.git
   cd RythmScouts


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
## License
This project is licensed under the MIT License - see the LICENSE file for details.

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

## **GitHub Actions CI/CD**

RythmScouts uses GitHub Actions to automate the build and artifact generation process. The workflow triggers on **pushes to `release/**` branches** and can also be run manually via the Actions tab.

### **Workflow Features**

* **Run Unit Tests** – Ensures that the project compiles and tests pass
* **Build APKs and AAB** – Generates both debug and release APKs as well as a release App Bundle (AAB)
* **Upload Artifacts** – Automatically uploads generated APKs and AABs to GitHub for download

### **Workflow Highlights**

* Uses **Ubuntu-latest** runner
* Sets up **JDK 17** with Gradle caching for faster builds
* Dynamically sets environment variables such as current date and repository name
* Builds:

  * Debug APK (`assembleDebug`)
  * Release APK (`assembleRelease`)
  * Release App Bundle (`bundleRelease`)
* Uploads artifacts to GitHub Actions for easy access

### **Example Workflow Snippet**

```yaml
name: Generated APK AAB (Upload - Create Artifact To Github Action)

on:
  push:
    branches:
      - 'release/**'
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4
      - name: Set current date
        run: echo "date_today=$(date +'%Y-%m-%d')" >> $GITHUB_ENV
      - name: Set repository name
        run: echo "repository_name=$(echo '${{ github.repository }}' | awk -F '/' '{print $2}')" >> $GITHUB_ENV
      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          distribution: 'zulu'
          java-version: '17'
          cache: 'gradle'
      - name: Change wrapper permissions
        run: chmod +x ./gradlew
      - name: Run tests
        run: ./gradlew test
      - name: Build project
        run: ./gradlew build
      - name: Build debug APK
        run: ./gradlew assembleDebug
      - name: Build release APK
        run: ./gradlew assemble
      - name: Build release AAB
        run: ./gradlew app:bundleRelease
      - name: Upload debug APK
        uses: actions/upload-artifact@v4
        with:
          name: ${{ env.date_today }} - Debug APK
          path: app/build/outputs/apk/debug/
      - name: Upload release APK
        uses: actions/upload-artifact@v4
        with:
          name: ${{ env.date_today }} - Release APK
          path: app/build/outputs/apk/release/
      - name: Upload release AAB
        uses: actions/upload-artifact@v4
        with:
          name: ${{ env.date_today }} - Release AAB
          path: app/build/outputs/bundle/release/
```

### **Benefits**

* Ensures that your app builds correctly for every release branch
* Provides downloadable APKs/AABs directly from GitHub Actions
* Automates testing to catch issues before deployment

---

## Support & Contact
Developers:

Halalisile Mzobe

Mmangaliso Malunga

Mvelo Mkhize

Repository: https://github.com/mzhala/RythmScouts

Issues: GitHub Issues

<div align="center">
🎵 Built for music lovers, by music lovers 🎵
Never miss another beat with RythmScouts
</div>

<div align="center">
<img width="100" height="100" alt="ic_logo" src="https://github.com/user-attachments/assets/97cbdd4a-dd18-4c9c-b32e-4eb949c268fb" />
</div>


Project started: October 2025 | Last updated: October 2025

# SAWeather App - OPSC6312 Final POE

![SAWeather Banner](https://via.placeholder.com/800x200/2196F3/FFFFFF?text=SAWeather+-+South+African+Weather+Companion)

A fully functional weather application built with Kotlin that provides accurate weather information with a seamless and intuitive user experience. Designed specifically for South African users with multi-language support and real-time weather updates.

## 🌟 Features

### 🔐 Authentication System
- **User Registration**: Secure account creation with encrypted password storage
- **User Login**: Authentication using email and password credentials
- **Single Sign-On (SSO)**: Google and Firebase authentication integration
- **Firebase Integration**: All user data securely stored in Firebase (NoSQL database)

### ⚙️ User Settings & Profile
- Customizable app preferences and user configurations
- Profile management capabilities
- Multi-language support (English, isiZulu, Afrikaans)
- Theme customization options

### 🌤️ Weather Features
- **Real-time Weather Data**: Current conditions and forecasts
- **Location-based Weather**: Automatic and manual location detection
- **10-Day Forecast**: Extended weather predictions for planning
- **Air Quality Index**: Real-time AQI monitoring with health recommendations
- **Multiple Cities**: Johannesburg, Cape Town, Pretoria, Durban support

### 📱 User Interface
- **Login Screen**: Secure authentication with Firebase
- **Main Weather Dashboard**: Current temperature, conditions, and location
- **Locations Screen**: Multi-city temperature overview and management
- **Forecast Display**: 10-day weather predictions with daily highs/lows
- **AQI Monitoring**: Color-coded air quality alerts and safety tips

### 🔄 Advanced Features (POE Implementation)
- **Offline Mode with Sync**: Weather data caching with RoomDB and synchronization
- **Real-time Push Notifications**: Firebase Cloud Messaging for weather alerts
- **Multi-language Support**: English, isiZulu, and Afrikaans localization
- **Blob Storage**: Weather icons and user avatars stored in cloud storage

## 🛠️ Technical Implementation

### Architecture & Development
- **Language**: Kotlin
- **IDE**: Android Studio
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Firebase Firestore (NoSQL), RoomDB for offline storage

### API Integration
- **WeatherAPI.com**: Real-time weather data and forecasts
- **Firebase Authentication**: Secure user management
- **Firebase Firestore**: User data and preferences storage
- **Firebase Cloud Messaging**: Push notifications

### External Libraries
- `Firebase Authentication & Firestore` - Backend services
- `Retrofit` - RESTful API calls
- `Glide` - Image loading and caching
- `Room Database` - Local data persistence
- `Material Design Components` - Modern UI elements
- `WorkManager` - Background synchronization

## 🚀 Installation & Setup

### Prerequisites
- Android Studio Arctic Fox or later
- Physical Android device (required for final testing)
- Firebase project setup

### Installation Steps
1. **Clone the repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/SAWeather-OPSC6312.git

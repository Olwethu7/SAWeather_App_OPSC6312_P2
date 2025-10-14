SAWeather App - OPSC6312 Part 2 App Overview SAWeather is a fully functional weather application built with Kotlin in Android Studio. The app provides users with accurate weather information while offering a seamless and intuitive user experience.

Key Features Implemented (Part 2) Authentication System User Registration: Secure account creation with encrypted password storage

User Login: Authentication using email and password credentials

Firebase Integration: All user data securely stored in Firebase (NoSQL database)

User Settings Customizable app preferences and user configurations

Profile management capabilities

Weather Features Real-time weather data display

Location-based weather information

User-friendly and visually appealing interface

Technical Implementation Architecture & Development Language: Kotlin

IDE: Android Studio

Architecture: MVVM (Model-View-ViewModel)

API Integration: RESTful weather API consumption

Security Password encryption using Firebase Authentication

Secure data transmission

External Libraries Used Firebase Authentication & Firestore

Retrofit for API calls

Glide for image loading

Material Design components

Testing & Quality Assurance Automated Testing The app includes comprehensive unit testing for:

User registration functionality

Weather data fetching and API integration

User authentication processes

Settings configuration

GitHub Actions CI/CD This project uses GitHub Actions for continuous integration:

Automated builds on every push

Unit test execution

Build verification across different environments

Installation & Setup Clone the repository

Open in Android Studio

Configure Firebase by adding google-services.json file

Set up Firebase Authentication and Firestore

Build and run the project

API Integration The app connects to:

Firebase Authentication for user management

Weather API for weather data

Firestore for user data storage

Demonstration Video A demonstration video is available showing:

User registration and login process

Weather data display and functionality

Settings configuration

Data storage in Firebase console

API integration working

Future Features (POE) Single Sign-On (SSO) integration

Offline mode with synchronization

Real-time push notifications

Multi-language support (including South African languages)

Development Notes Code Quality Comprehensive code commenting throughout

Strategic logging implementation for debugging

Clean architecture principles followed

Material Design guidelines adhered to

Challenges Overcome Secure authentication implementation

API integration and error handling

Responsive UI design

Data persistence and synchronization

1. Login Screen 🔐
Secure user authentication with Firebase
Email/password login and registration
Input validation and error handling
Automatic login persistence
2. Main Weather Dashboard 🌡️
Current Weather Display: Temperature, conditions, location
Weather Details: Humidity, wind speed, feels-like temperature
Quick Actions: One-tap access to all features
User Info: Display logged-in user email
Real-time Data: Live weather updates from WeatherAPI.com
3. Locations Screen 📍
Multiple Cities: Johannesburg, Cape Town, Pretoria, Durban
Temperature Overview: Current temps for all locations
Easy Navigation: Quick switching between cities
My Locations: Personalized location management
4. 10-Day Forecast 📅
Extended Forecast: 10-day weather predictions
Daily Overview: High/low temperatures per day
Visual Layout: Clean, easy-to-read forecast display
Detailed Planning: Perfect for trip planning
5. Air Quality Index 🌫️
AQI Monitoring: Real-time air quality data
Health Recommendations: Safety tips based on AQI levels
Color-coded Alerts: Visual indicators for air quality
Moderate Level Handling: Special warnings for sensitive groups

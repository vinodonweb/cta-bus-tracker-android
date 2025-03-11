# CTA Bus Tracker App 🚍  

## Overview  
This is an Android application that provides real-time information on **CTA bus routes, nearby stops, and arrival predictions** using the **CTA Bus Tracker API**. Users can search for bus routes, view stops within **1000m of their location**, and check upcoming bus arrivals.  

## Features  
✅ **Bus Routes & Stops**  
- Displays all **CTA bus routes** retrieved from the API.  
- Users can **search** for routes by name or number.  
- **Filter stops within 1000m** of the user's location.  

✅ **Bus Arrival Predictions**  
- Displays **real-time arrival predictions** for selected stops.  
- Swipe down to **refresh predictions**.  
- Tap a bus arrival to view **distance from the stop**.  

✅ **User Experience & UI**  
- **Splash Screen API** for a smooth startup experience.  
- **AlertDialogs** for user interactions and location permissions.  
- **TextInputLayout** for intuitive route searching.  
- **Custom fonts** (`helvetica_neue_medium`).  
- **Admob/Unity Ads** integration for monetization.  

✅ **Location & Caching**  
- Uses **GPS location** to determine nearby stops.  
- **Caches route and stop data for 24 hours** to reduce API calls.  
- Prediction data is always retrieved fresh.  

## Tech Stack 🛠️  
- **Android Studio** (Java/Kotlin)  
- **CTA Bus Tracker API** for transit data  
- **Android Volley** for API requests  
- **Fused Location API** for GPS-based stop detection  
- **AdMob/Unity Ads** for in-app advertisements  
- **RecyclerView & ViewPager2** for UI presentation  
- **Implicit Intents** for viewing locations on a map  

## How to Run the App 🚀  
1. Clone the repository:  
   ```sh  
   git clone git@github.com:vinodonweb/cta-bus-tracker-android.git
   cd cta-bus-tracker  
   ```  
2. Open the project in **Android Studio**.  
3. Obtain a free **API Key** from [CTA Bus Tracker](https://www.ctabustracker.com/dev-account) and add it to `strings.xml`:  
   ```xml  
   <string name="cta_api_key">YOUR_API_KEY_HERE</string>  
   ```  
4. Run the app on an emulator or physical device.  

## Screenshots 📸  

![Screenshot 2025-03-11 114652](https://github.com/user-attachments/assets/9824b85d-66ac-4f19-b3b2-c47a65ef483a)
![Screenshot 2025-03-11 114701](https://github.com/user-attachments/assets/70549fb3-4704-4948-a341-eaa7a73261a9)
![Screenshot 2025-03-11 114755](https://github.com/user-attachments/assets/a6e270d5-c482-4cac-ad6b-a4d0237ee9db)
![Screenshot 2025-03-11 114809](https://github.com/user-attachments/assets/fbc265e4-6073-4cb4-95bb-5705c7e6ca0d)
![Screenshot 2025-03-11 114822](https://github.com/user-attachments/assets/469d29f8-5a1a-4910-9ab0-c2b41f6878ea)
![Screenshot 2025-03-11 114845](https://github.com/user-attachments/assets/06291747-06bc-456c-89e9-2629e634b59a)
![Screenshot 2025-03-11 114645](https://github.com/user-attachments/assets/08c0a623-fdee-4607-ab49-70491d12eee4)


## License 📜  
This project is for educational purposes only.  

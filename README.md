# Splitr - Receipt-Scanning and Itemized Splitting App

**Ryan Chan, Ryan Hu, Kyle Taschek**  

## Introduction
Splitr allows users to scan physical receipts using their smartphone camera, automatically extract itemized details using Optical Character Recognition (OCR), and facilitate splitting the total cost among a group of people. The app includes an interface to assign individual receipt items to specific people and calculate the total amount each person owes. With on-device scanning, processing and splitting, users have the convenience of handling receipt splits on the go, especially after group meals or shared purchases.

## Figma Design
![image](https://github.com/user-attachments/assets/e3a12b48-3d58-4157-a86a-18615274768e)


## Android and Jetpack Compose Features
- **CameraX API:** Utilized for accessing the device camera to scan receipts.
- **Google ML Kit OCR:** Employed to extract text from the scanned receipt images.
- **Room Database:** Used for storing past receipts, users, and split data locally.
- **Coil Compose:** For efficient image loading (Particularly for LeBron).
- **AndroidX Navigation and Lifecycle Components:** To manage navigation and UI state.

No additional third part APIs or server.

## Device Dependencies & SDK Requirements
- **Device Features:** The app depends on a functional camera and local processing capabilities.
- **Android SDK:**
  - **Min SDK:** 24
  - **Compile SDK:** 35
  - **Target SDK:** 34

## Additional Notes
In our project, we went beyond the base requirements by leveraging Google ML Kit's OCR for on-device text recognition, allowing our app to work offline.We integrated error handling and fallback mechanisms that allow users to correct the receipt items, prices, location and dates if incorrectly scanned. If no receipt is recognized, a blank receipt is opened to maintain a smooth user experience. Additionally, we handle various different types of receipts and receipt fields such as discounts on certain items.


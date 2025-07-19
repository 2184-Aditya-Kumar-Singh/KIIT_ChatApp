# KIIT ChatApp

A full-featured real-time chat application for students and staff of KIIT, built with Android (Java), Firebase Realtime Database, and Firebase Storage.  
The app supports group and private chats, text, image, and audio message sharing, role-based user features (such as student roll number display), message read status, and more.

---

## Features

- **Firebase Authentication:** Secure user sign-in and identification.
- **User Roles:** Supports students and staff. Students' roll numbers (parsed from their KIIT email) are shown in chats.
- **Group Chat:** Join or create group chats. Group name is displayed at the top.
- **Private Chat:** One-on-one private messaging with message seen/unseen status and unread counts.
- **Text Messaging:** Send and receive text messages in real-time.
- **Image & Audio Messaging:** (Optional, requires Firebase Storage) Send and receive images and audio files.
- **Role-based UI:** 
  - Student roll numbers are displayed below the sender's name for student messages.
  - Upload buttons are visible but disabled if storage is not enabled.
- **Seen/Unseen & Unread Count:** Messages are marked as seen/unseen, and unread counts are tracked.
- **Modern UI:** RecyclerView-based chat screens with clear message formatting.

---




---

## Project Structure

```
app/
 ├─ java/com/example/kiit_chatapp/
 │   ├─ activities/         # All Activity classes (GroupChatActivity, PrivateChatActivity, etc.)
 │   ├─ adapters/           # RecyclerView Adapters (MessageAdapter)
 │   ├─ models/             # Data models (Message, User)
 │   └─ ...                 # Other app code
 ├─ res/layout/             # XML layouts (activity_group_chat.xml, item_message_received.xml, etc.)
 ├─ res/drawable/           # Image assets (icons, etc.)
 └─ ...
```

---

## Setup & Installation

### 1. **Clone the Repository**

```bash
git clone https://github.com/yourusername/kiit_chatapp.git
cd kiit_chatapp
```

### 2. **Firebase Setup**

- **Create a Firebase Project:** [Firebase Console](https://console.firebase.google.com/)
- **Enable Authentication:** (Email/Password or Google Sign-in)
- **Realtime Database:**  
  - Create a Realtime Database and set [appropriate rules](#security-rules).
- **Storage:**  
  - For Image and Audio, use Cloudinary

### 3. **Add `google-services.json`**

- Download your Firebase project’s `google-services.json` and place it in `app/` directory.

### 4. **Build in Android Studio**

- Open the project in Android Studio.
- Build and run the app on your emulator/device.

---

## Usage

1. **Sign in** with your KIIT email.
2. **Join/Create Groups:** Participate in group discussions.
3. **Private Chat:** Select a user to chat privately.
4. **Send Messages:** Text, image, and audio (if enabled).
5. **Student Roll Numbers:** If the sender's role is student, their roll number will be shown below their name in group/private chat.

---

## Code Highlights


## Customization

- **Hide/Disable Media Upload:**  
  - When Firebase Storage is not set up, image/audio upload buttons are disabled (not hidden).
- **Add More User Roles:**  
  - Extend the user model and adjust the adapter logic as needed.
- **UI Themes:**  
  - Modify `res/layout/` and `res/values/` for custom appearances.

---

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/my-feature`)
3. Commit your changes (`git commit -am 'Add new feature'`)
4. Push to the branch (`git push origin feature/my-feature`)
5. Create a new Pull Request

---

## Guide to run app

1. Only accessible for Kiitian, i.e Kiit Email.
2. After verifying through email, come back to same page and click register then setup your profile.
3. To chat privately with someone, search there KIIT email id in private chat section.
4. Groups will be visible to you based on your selected interests, which can be updated later in my profile section.
5. Contact 22052184@kiit.ac.in for any assistance or complaints regarding this application.

---

## Pre-Release
[Click To Download the App](https://github.com/2184-Aditya-Kumar-Singh/KIIT_ChatApp/releases/download/v1.1/KIIT.ChatApp.apk)

---

## Credits

- [Firebase](https://firebase.google.com/) (Realtime Database, Auth, Storage)
- [Cloudinary](https://cloudinary.com/) (Real time Image and Audio Storage)

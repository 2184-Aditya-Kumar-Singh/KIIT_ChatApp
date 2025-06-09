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

## Screenshots

<img src="https://github.com/user-attachments/assets/d67ee4a8-6117-4b5e-9ae7-d2d257837af5" width="100"/>
<img src="https://github.com/user-attachments/assets/115e4fe1-ec00-4e5d-8c85-99fb6f263691" width="100"/>
<img src="https://github.com/user-attachments/assets/02df5494-2e9c-43be-a3a6-f297480f0079" width="100"/>
<img src="https://github.com/user-attachments/assets/3dc793e2-9c4f-49ab-83d5-1a396785167a" width="100"/>
<img src="https://github.com/user-attachments/assets/5ac5984b-2d04-45e5-a6cb-bd50b496c162" width="100"/>
<img src="https://github.com/user-attachments/assets/654b9232-5b01-44bd-bb13-a5837b23f8a8" width="100"/>
<img src="https://github.com/user-attachments/assets/d20ccba8-c24d-4d21-a31b-69a6a31e9c53" width="100"/>
<img src="https://github.com/user-attachments/assets/5b78dfc2-e53d-47ee-95ca-2aa07e73854d" width="100"/>
<img src="https://github.com/user-attachments/assets/e81c8367-b8df-4cfe-815f-2bb13d9bf599" width="100"/>
<img src="https://github.com/user-attachments/assets/368f68b5-f54a-4335-9bcf-c189756c7615" width="100"/>
<img src="https://github.com/user-attachments/assets/aaf98443-6258-4e67-8977-6804d7ac45c5" width="100"/>
<img src="https://github.com/user-attachments/assets/98b28bc4-c977-4485-9fd3-f55df9af9320" width="100"/>
<img src="https://github.com/user-attachments/assets/d4abe98f-f189-4179-9496-561b6846599c" width="100"/>
<img src="https://github.com/user-attachments/assets/d93052b4-ac72-4fad-88c5-e66bbfae7ada" width="100"/>
<img src="https://github.com/user-attachments/assets/42192b04-c48d-491f-b514-5bd6e21ae926" width="100"/>
<img src="https://github.com/user-attachments/assets/57525151-8c20-4d48-acbe-89c338f692d4" width="100"/>
<img src="https://github.com/user-attachments/assets/707f7a53-a393-4d4e-b9b8-f8dedb346080" width="100"/>



 


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
  - (Optional, for images/audio) Enable Firebase Storage and configure rules.

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

1.Only accessible for Kiitian, i.e Kiit Email.
2.Image and Voice sending feature is yet to be released.
3.To chat privately with someone, search there KIIT email id in private chat section.
4.Groups will be visible to you based on your selected interests, which can be updated later in my profile section.
5.Contact 22052184@kiit.ac.in for any assistance or complaints regarding this application.

---

## Pre-Release
https://github.com/2184-Aditya-Kumar-Singh/KIIT_ChatApp/releases/download/v1.1/KIIT.ChatAPp.apk

---

## Credits

- [Firebase](https://firebase.google.com/) (Realtime Database, Auth, Storage)

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

<!-- Add screenshots here of group chat, private chat, roll number display, etc. -->

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

## Security Rules

### **Realtime Database (for development)**
```json
{
  "rules": {
    ".read": "now < 1751653800000",  // 2025-7-5
    ".write": "now < 1751653800000"  // 2025-7-5
  }
}
```
> ⚠️ For production, update rules to restrict access to authenticated users and validate data structure.

### **Storage (for development)**
```plaintext
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if true;
    }
  }
}
```
> ⚠️ Never use open storage rules in production. Restrict access to authenticated users only.

---

## Usage

1. **Sign in** with your KIIT email.
2. **Join/Create Groups:** Participate in group discussions.
3. **Private Chat:** Select a user to chat privately.
4. **Send Messages:** Text, image, and audio (if enabled).
5. **Student Roll Numbers:** If the sender's role is student, their roll number will be shown below their name in group/private chat.

---

## Code Highlights

### **Message Model**
```java
public class Message {
    private String messageId, senderId, senderName, senderEmail, senderRole, text, type, timeStamp, fileUrl;
    private boolean seen;
    // Getters/Setters...
}
```

### **Displaying Roll Number for Students**
- In chat, if the sender's role is `"student"` and their email ends with `@kiit.ac.in`, the app will parse and display the roll number below their name.

---

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

## Credits

- [Firebase](https://firebase.google.com/) (Realtime Database, Auth, Storage)

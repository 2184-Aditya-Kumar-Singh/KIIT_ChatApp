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

<!-- ![WhatsApp Image 2025-06-08 at 22 36 35_b6760f42](https://github.com/user-attachments/assets/3e207945-3589-42ef-bab4-3cd6f1b84a5a)
Add screenshots![WhatsApp Image 2025-06-08 at 22 36 35_817f1126](https://github.com/us![WhatsApp Image 2025-06-08 at 22 36 39_285edd98](https://github.com/user-attachments/assets/6c55a5fc-fd8a-44a1-9cbf-46a10697fcc0)
![WhatsApp Image 2025-06-08 at 22 36 38_e2e096b7](https://github.com/user-attachments/assets/535baaf2-2322-4984-90fa-8d843fdf0e7d)
![WhatsApp Image 2025-06-08 at 22 36 38_29cd1698](https://github.com/user-attachments/assets/d75b0575-439c-433e-b2ff-ca224de910e0)
![WhatsApp Image 2025-06-08 at 22 36 38_1ea270f0](https://github.com/user-attachments/assets/cbdef444-8568-473c-9443-997e0a4e24fc)
![WhatsApp Image 2025-06-08 at 22 36 37_fc5e971d](https://github.com/user-attachments/assets/b51dcaa4-91f7-411f-8d75-f6af5ec8d5a4)
![WhatsApp Image 2025-06-08 at 22 36 37_52137de3](https://github.com/user-attachments/assets/7ed9f444-5631-4c12-9876-7dab88d4fcc6)
![WhatsApp Image 2025-06-08 at 22 36 36_c4bac202](https://github.com/user-attachments/assets/e20c4457-9a5a-4e05-9988-f2162c257fd2)
![WhatsApp Image 2025-06-08 at 22 36 36_086921f4](https://github.com/user-attachments/assets/522123ba-73ea-4403-afdf-b4741a23e9eb)
![WhatsApp Image 2025-06-08 at 22 36 36_31ed1b95](https://github.com/user-attachments/assets/c66fed55-43ee-4358-992e-76543b748872)
![WhatsApp Image 2025-06-08 at 22 36 42_996e3f4b](https://github.com/user-attachments/assets/2e33fe7a-a809-465e-8ea3-d0c57cf2f4a7)
![WhatsApp Image 2025-06-08 at 22 36 41_a031170a](https://github.com/user-attachments/assets/ed2d5e9e-1803-4ac2-99c6-33b1093e7497)
![WhatsApp Image 2025-06-08 at 22 36 41_97ed8bac](https://github.com/user-attachments/assets/2fec94ab-15b9-4161-b230-0b814c325842)
![WhatsApp Image 2025-06-08 at 22 36 40_c9eca4f5](https://github.com/user-attachments/assets/ea94f8e7-acf7-444f-af3a-2d5d5c54d6ce)
![WhatsApp Image 2025-06-08 at 22 36 40_0c9d92c1](https://github.com/user-attachments/assets/520e40e3-c94b-4998-acbe-c8f0c6502121)
![WhatsApp Image 2025-06-08 at 22 36 39_554d2d30](https://github.com/user-attachments/assets/d0bf70dc-4d18-4583-9160-13c075582f4d)
er-attachments/assets/f32f3a2c-7148-4df6-bc0f-cd9cf846814d)
 here of group chat, private chat, roll number display, etc. -->


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

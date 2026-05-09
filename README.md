# Mobile Lock System 🔐

A Java-based Mobile Lock / PIN Control System with a modern graphical interface.  
This application allows users to securely set, unlock, change, and lock a mobile PIN with validation and access messages.

---

# 📌 Features

- ✅ Set a secure PIN
- ✅ Unlock mobile using PIN
- ✅ Change existing PIN
- ✅ Lock mobile again
- ✅ PIN validation system
- ✅ Access granted / denied messages
- ✅ Attractive UI design
- ✅ Real-time status display

---

# 🛠️ Technologies Used

- Java
- Java Swing
- VS Code

---

# 📂 Project Structure

```bash
MobileLockProject/
│
├── src/
│   └── MobileLockWebApp.java
│
├── out/
│   ├── AccessMessage.class
│   ├── LockPageHandler.class
│   ├── MobileLockSystem.class
│   ├── MobileLockWebApp.class
│   ├── PinValidator.class
│   └── ValidationResult.class
│
├── Result.png
├── image.png
└── README.md
```

---

# 🚀 How to Run the Project

## 1️⃣ Compile the Program

```bash
javac -d out src/MobileLockWebApp.java
```

## 2️⃣ Run the Program

```bash
java -cp out MobileLockWebApp
```

---

# 🔑 PIN Rules

- PIN must contain digits only
- PIN length should be between 4 to 8 digits
- Repeated digits are not allowed continuously

### Example

| PIN | Status |
|-----|--------|
| 2580 | ✅ Valid |
| 1111 | ❌ Invalid |

---

# 📸 Output Screenshots

## 🔹 Mobile Lock System UI

<img width="100%" src="Result.png">

---

## 🔹 Project Structure in VS Code

<img width="60%" src="image.png">

---

# 📖 Working Process

### Set PIN
User enters a new PIN and clicks **Set PIN**.

### Unlock Mobile
Enter the correct PIN to unlock the mobile system.

### Change PIN
Users can change their current PIN after verification.

### Lock Mobile
Click **Lock Now** to lock the system again.

---

# 💡 Future Improvements

- Fingerprint authentication
- Face unlock feature
- Database storage
- OTP verification
- Dark mode UI
- Mobile responsive design

---

# 👩‍💻 Author

**Pooja Dv**

---

# ⭐ GitHub Upload Steps

## Initialize Git

```bash
git init
```

## Add Files

```bash
git add .
```

## Commit Files

```bash
git commit -m "Initial commit - Mobile Lock System"
```

## Connect GitHub Repository

```bash
git remote add origin https://github.com/your-username/MobileLockProject.git
```

## Push Project

```bash
git branch -M main
git push -u origin main
```

---

# 📜 License

This project is created for educational purposes.

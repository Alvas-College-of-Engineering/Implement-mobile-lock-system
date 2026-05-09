# Mobile Lock System 🔐

A Java-based Mobile Lock / PIN Control System with a modern graphical interface.  
This project allows users to securely set, unlock, change, and lock a mobile PIN with validation and access messages.

---

# 📌 Features

- ✅ Set a secure PIN
- ✅ Unlock mobile using PIN
- ✅ Change existing PIN
- ✅ Lock mobile again
- ✅ PIN validation system
- ✅ Access granted / denied messages
- ✅ Modern UI Design
- ✅ Real-time lock status display

---

# 🛠️ Technologies Used

- Java
- Java Swing
- VS Code
- GitHub

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

<img src="./Result.png" width="100%">

---

## 🔹 Project Structure in VS Code

<img src="./image.png" width="70%">

---

# 📖 Working Process

## 🔹 Set PIN

User enters a new PIN and clicks **Set PIN**.

## 🔹 Unlock Mobile

Enter the correct PIN to unlock the mobile system.

## 🔹 Change PIN

Users can change their current PIN after verification.

## 🔹 Lock Mobile

Click **Lock Now** to lock the system again.

---

# 💡 Future Improvements

- Fingerprint authentication
- Face unlock system
- OTP verification
- Database storage
- Dark mode support
- Mobile responsive UI

---

# 👩‍💻 Author

**Pooja Dv**

---

# ⭐ GitHub Upload Steps

## Step 1️⃣ Initialize Git

```bash
git init
```

---

## Step 2️⃣ Add All Files

```bash
git add .
```

---

## Step 3️⃣ Commit Files

```bash
git commit -m "Initial commit - Mobile Lock System"
```

---

## Step 4️⃣ Create Repository in GitHub

1. Open GitHub
2. Click **New Repository**
3. Repository Name: `MobileLockProject`
4. Click **Create Repository**

---

## Step 5️⃣ Connect Local Project to GitHub

Replace `your-username` with your GitHub username.

```bash
git remote add origin https://github.com/your-username/MobileLockProject.git
```

---

## Step 6️⃣ Push Project to GitHub

```bash
git branch -M main
git push -u origin main
```

---

# ✅ After Upload

Your GitHub repository will contain:

- Java source code
- README documentation
- Output screenshots
- Project structure

---

# 📜 License

This project is created for educational purposes only.

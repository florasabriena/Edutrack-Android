# 📱 EduTrack: Android-Based Academic Task Monitoring Application

EduTrack is a lightweight, real-time Android application designed to help university students systematically manage their coursework, track multi-layered deadlines, and optimize daily productivity. Built using Kotlin and integrated with Firebase, the platform leverages Agile (Scrum) development methodologies to ensure a robust, high-performing software architecture.

---

## 🚀 Key Features

- **Real-Time Task Synchronization:** Instant data state updates across devices powered by Firebase Realtime Database.
- **Automated Priority Filtering:** Dynamic task sorting and filtering based on deadline proximity and task importance.
- **Secure Data Isolation:** User authentication and profile protection integrated via Firebase Authentication.
- **Intuitive UI/UX:** A clean, responsive user interface engineered in Kotlin to minimize cognitive load during workflow tracking.

---

## 🛠️ Tech Stack & Architecture

- **Frontend:** Android Studio, Kotlin, XML Layouts
- **Backend & Database:** Firebase Authentication, Firebase Realtime Database
- **Project Management Methodology:** Scrum Framework
- **Architecture Pattern:** MVVM (Model-View-ViewModel) for clean separation of concerns

---

## 📂 Project Structure

```
app/
└── src/
    ├── main/
    │   ├── java/com/edutrack/
    │   │   ├── data/          # Firebase data models and repositories
    │   │   ├── ui/            # View classes (Activities/Fragments) and ViewModels
    │   │   └── utils/         # Helper classes and priority logic constants
    │   └── res/
    │       ├── layout/        # Responsive XML UI layouts
    │       └── values/        # Styles, colors, and string resources
    └── build.gradle           # Dependency configurations
```

---

## 💻 Getting Started & Installation

To run this project locally, ensure you have **Android Studio** installed on your machine.

1. **Clone the repository:**

   ```bash
   git clone https://github.com/yourusername/edutrack.git
   ```

2. **Open the project:**

   Open Android Studio, select **Open an Existing Project**, and navigate to the cloned `edutrack` folder.

3. **Connect to Firebase:**

   - Create a new project in the [Firebase Console](https://console.firebase.google.com/).
   - Enable **Authentication** (Email/Password) and **Realtime Database**.
   - Download the `google-services.json` file and place it inside the `app/` directory of your project.

4. **Build and Run:**

   Sync the Gradle files and run the application on an Android Emulator or a physical device.

---

## 📈 Agile Development Process (Scrum)

This project was executed using the **Scrum Framework** to maintain development velocity and adaptability:

- **Product Backlog Refinement:** User pain points were mapped out and converted into technical specifications.
- **Sprint Cycles:** Development was structured into bi-weekly sprints focusing on incremental feature deployment (e.g., UI layout completion, Firebase integration, and data querying optimization).

---

## 🔮 Future Enhancements

- Integration with **Google Calendar API** for external task syncing.
- Implementing **Local Push Notifications** for proactive deadline warnings.
- Developing **AI-Driven Study Analytics** to evaluate weekly academic workloads.

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).

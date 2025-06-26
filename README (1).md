# BudgetIQ - Smart Personal Finance Manager_POE

![horizontal](https://github.com/user-attachments/assets/3828d66d-cbcf-476b-81ff-380e287ff845)

BudgetIQ is a modern Android application designed to help users manage their personal finances effectively. Built with Jetpack Compose, Room, Hilt, and MVVM, it offers a seamless and intuitive experience for tracking expenses, setting budgets, and achieving financial goals.

YouTube Link: https://youtu.be/039g-9lgIvE?si=ac7FpT4Wn-A4npkJ

GitHub Link:https://github.com/Princely24/BudgetIQ_FinanceTracker_POE

---

## Features

- **User Authentication**: Secure login and registration system.
- **Expense Tracking**: Add, view, and manage expenses with category assignment and receipt capture.
- **Category Management**: Organize expenses with customizable categories and view recent categories.
- **Budget Goals**: Set monthly budget goals per category and track your progress visually.
- **Achievements & Badges**: Earn badges for financial milestones (e.g., Consistency, Frugal spending).
- **Analytics & Insights**:
  - Visual bar and pie charts for spending by category.
  - Time period selector (week, month, year) for dynamic analytics.
  - Dashboard with summaries, top categories, and recent expenses.
- **Material Design 3**: Modern, responsive UI with dynamic theming.
- **Receipt Management**: Capture and store receipt images using CameraX.
- **Data Persistence**: All data is stored locally using Room.

---

## Screenshots
![Image](https://github.com/user-attachments/assets/b47cb694-038a-4b4a-99e8-587f16198089)
![Image](https://github.com/user-attachments/assets/a775646c-9c9f-47b8-a434-002bb6882e27)
![Image](https://github.com/user-attachments/assets/bf46656b-48da-413d-89e7-4fbd40652b2d)
![Image](https://github.com/user-attachments/assets/d5cfb5b3-ecc5-429f-af84-d7b8d7d05d7b)
![Image](https://github.com/user-attachments/assets/97044aab-7b10-4f7c-9a87-091109e020a0)
![Image](https://github.com/user-attachments/assets/c8c7e3e4-3305-4ad6-9355-639b2c7e1adb)
![Image](https://github.com/user-attachments/assets/80e1ce07-4cf1-48f6-be7c-ac7d6192281f)
![Image](https://github.com/user-attachments/assets/11cb9f0d-f0fb-4113-b7db-abdad45be064) 
![Image](https://github.com/user-attachments/assets/e6380d11-4151-4762-b624-928fbb5e2c43) 
![Image](https://github.com/user-attachments/assets/0e2696d8-d208-4dd9-9cbd-abdda77d8142)
![Image](https://github.com/user-attachments/assets/8d10595c-2af2-4dc9-82a1-aafd39680078) 
![Image](https://github.com/user-attachments/assets/a41c055f-289c-430a-948e-aeee0c0dce70)
![Image](https://github.com/user-attachments/assets/f37a00d7-53e9-45f6-a576-f69b98c2d99a)
![Image](https://github.com/user-attachments/assets/4a51f5b6-9aed-4b4b-8a95-bfce94444c60)    
![Image](https://github.com/user-attachments/assets/7a09a43a-49c2-464b-8198-841d14aceddc)
---

## Tech Stack

- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Hilt
- **Database**: Room
- **Concurrency**: Kotlin Coroutines & Flow
- **Navigation**: Jetpack Navigation Compose
- **Image Loading**: Coil
- **Camera**: CameraX
- **Design System**: Material Design 3

---

## Architecture Overview

The app follows Clean Architecture principles and is organized into the following layers:

- **UI Layer**: Compose UI screens, reusable components, and ViewModels.
- **Data Layer**: Room entities, DAOs, and repositories for expenses, categories, users, badges, and budget goals.
- **Dependency Injection**: All repositories and data sources are injected using Hilt.
- **Navigation**: Centralized navigation using Jetpack Compose Navigation.

### Main Packages

- `ui/screens/` - All main screens (Home, AddExpense, BudgetGoals, Achievements, Categories, etc.)
- `ui/components/` - Reusable Compose UI components.
- `ui/viewmodels/` - ViewModels for each screen, handling state and business logic.
- `data/model/` - Data entities (Expense, Category, BudgetGoal, Badge, User).
- `data/dao/` - Room DAOs for database access.
- `data/repository/` - Repositories abstracting data access.
- `di/` - Hilt modules for dependency injection.
- `util/` - Utility classes and converters.

---

## Key Features in Detail

### 1. Dashboard & Analytics
- **Dynamic Bar and Pie Charts**: Visualize spending by category.
- **Time Period Selector**: Filter analytics by week, month, or year.
- **Budget Progress**: See your total and per-category budget progress with color cues.
- **Recent Expenses & Categories**: Quick overview of your latest activity.

### 2. Budget Goals
- Set minimum and maximum budget goals per category.
- Visual progress bars for each goal.
- Edit and manage goals easily.

### 3. Achievements & Badges
- **Consistency Badge**: Awarded for logging expenses 7 days in a row.
- **Frugal Badge**: Awarded for spending less than 80% of your monthly budget.
- All badges are persistent and shown on the dashboard and Achievements screen.

### 4. Categories
- Add, edit, and delete custom categories.
- Recent categories are highlighted for quick access.

### 5. Expense Management
- Add expenses with amount, description, category, date, and optional receipt photo.
- View, edit, and delete expenses.
- Filter and analyze expenses by time period.

---

## Prerequisites

- Android Studio Hedgehog | 2023.1.1 or newer
- Minimum SDK: 24
- Target SDK: 34
- Kotlin version: 1.9.0 or newer

---

## Getting Started

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Princely24/BudgetIQ_FinanceTracker_POE.git
   ```

2. **Open the project in Android Studio**

3. **Sync the project with Gradle files**

4. **Run the app** on an emulator or physical device

---

## Building

To build the app, you can use Android Studio or run the following command:

```bash
./gradlew assembleDebug
```

For release build:
```bash
./gradlew assembleRelease
```

---

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

---

## Acknowledgments

- Material Design 3 for the modern UI components
- Android Jetpack libraries
- All contributors who help improve BudgetIQ

---

## Contact

Princely Makhwara (ST10263265) - [LinkedIn](https://www.linkedin.com/in/princely-makhwara-096285197)  
Aime Ndumuhire (ST10255663) - [LinkedIn](https://www.linkedin.com/in/aime-ishimwe-675557272/)  
Fortunate Majere (ST10231459) - [LinkedIn](https://za.linkedin.com/in/fortunate-majere-205933289)  
Sabelo Sibiya (ST10327016) - [LinkedIn](https://za.linkedin.com/in/sabelo-sibiya-712935289)  
Enrique Arendse (10302006) - [LinkedIn](https://www.linkedin.com/in/enrique-arendse-033700231?utm_source=share&utm_campaign=share_via&utm_content=profile&utm_medium=android_app)

Project Link: [(https://github.com/Princely24/BudgetIQ_FinanceTracker_POE)]

# 🚀 RevHire: Comprehensive Job Portal System

RevHire is a robust, console-based recruitment platform built with Java. It streamlines the hiring process by connecting Employers with qualified Job Seekers through a secure, efficient, and data-driven architecture.

---

## 🏗️ System Architecture
The project follows the **MVC (Model-View-Controller)** design pattern to ensure a clean separation of concerns:
* **Model:** Encapsulates data (User, Job, Skill, Application).
* **View:** Interactive console-based UI for seamless user experience.
* **DAO (Data Access Object):** Optimized SQL queries for high-performance data retrieval.
* **Service Layer:** Contains business logic, input validation, and security authorization.

---

## 🛠️ Modules & Functionalities

### 1. 👤 Job Seeker Module
* **Advanced Profile Management:** Manage educational background, professional experience, and technical certifications in one place.
* **✨ Custom Resume Creation:** A specialized engine that extracts user profile data to generate a professionally formatted resume instantly.
* **🔍 Wide-Range Search Filters:** Find the perfect role using a granular search engine. Users can filter jobs by:
    * **Job Title / Keywords**
    * **Location** (City/Remote)
    * **Job Type** (Full-time, Part-time, Contract)
    * **Salary Range** (Minimum and Maximum thresholds)
* **Applications:** Apply for roles and track status updates in real-time.

### 2. 🏢 Employer Module
* **Job Lifecycle:** Create, Update, Close, and Delete job postings.
* **Applicant Tracking:** View candidate counts and dive into specific profiles of applicants to find the best match.
* **Company Branding:** Manage company profile information visible to seekers to attract top talent.
* **Access Control:** Strict ownership validation ensures employers only manage their own listings.

---

## 📡 Core Services

### 📄 Resume Engine Service
* **Data Synthesis:** Consolidates contact info, skills, and work history into a structured layout.
* **Dynamic Updates:** Any change to the user's profile is instantly reflected in their generated resume.

### 🔔 Real-Time Notification Service
* **Trigger:** When an employer posts a new job, the system automatically scans all Job Seeker profiles.
* **Logic:** It identifies users whose skills match the job requirements.
* **Action:** Notifications are generated instantly, ensuring seekers never miss a relevant opportunity.

### 🔒 Advanced Security Service
* **Credential Masking:** Uses `java.io.Console` to provide secure, hidden password entry to prevent shoulder-surfing.
* **Authorization Guards:** Implements session-based security checks to prevent unauthorized data access or "ID Spoofing."

---

## 🚀 Getting Started

### Prerequisites
* **Java Development Kit (JDK):** 11 or higher.
* **Database:** Oracle DB or MySQL.
* **Build Tool:** Maven (recommended).

### Installation & Setup
1.  **Clone the Repository:**
    ```bash
    git clone [https://github.com/AswathyJL/RevHire.git](https://github.com/AswathyJL/RevHire.git)
    cd RevHire
    ```
2.  **Database Configuration:**
    Update `src/main/java/com/pack/RevHire/config/DBConnection.java` with your credentials.
3.  **Compile the Project:**
    ```bash
    mvn clean install
    ```

### Running the Application
To ensure the **Password Masking** works correctly, run the application in a native terminal (CMD/PowerShell) rather than the IDE console:
```cmd
java -cp "target/classes;lib/*" com.pack.RevHire.Main

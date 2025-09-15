# 📝 SiTracker – Simple Issue Tracker CLI

SiTracker is a **command-line issue tracker** built with **Java 17**, **Spring Boot**, and **Picocli**.  
It stores issues in a **Google Sheets document** to make collaboration simple and transparent.

---

## ✨ Features

- Create new issues with descriptions and optional parent IDs  
- Update the status of existing issues (`OPEN`, `IN_PROGRESS`, `CLOSED`)  
- List issues filtered by status  
- Persist issues in Google Sheets for team-wide visibility  
- Run locally or as a **Docker container**  

---

## 🚀 Requirements

- **Java 17** (JDK)  
- **Maven 3.8+**  
- **Google Cloud service account** with access to Google Sheets API  
- A **Google Sheets document** with a tab named `Issues`  
- (Optional) **Docker** for containerized execution  

---

## ⚙️ Setup

### 1. Clone the repository
```bash
git clone https://github.com/IrynaKhrustalova/SITracker.git
cd SITracker
```

### 2. Configure Google credentials
1. Create a service account in Google Cloud and enable **Google Sheets API**.  
2. Download the `credentials.json` file.  
3. Either:  
   - Place the file somewhere accessible and set an environment variable:  
     ```bash
     export GOOGLE_APPLICATION_CREDENTIALS=/path/to/credentials.json
     ```  
   - Or configure it in `application.yml`:  
     ```yaml
     sitracker:
       google:
         credentials: /absolute/path/to/credentials.json
       spreadsheet:
         id: YOUR_SPREADSHEET_ID
     ```

⚠️ The spreadsheet must contain a sheet/tab named **`Issues`**.

---

### 3. Build the application
```bash
mvn clean package
```

### 4. Run locally
```bash
java -jar target/sitracker-0.0.1-SNAPSHOT.jar create -d "My first issue"
java -jar target/sitracker-0.0.1-SNAPSHOT.jar list -s OPEN
java -jar target/sitracker-0.0.1-SNAPSHOT.jar update AD-1 -s IN_PROGRESS
```

---

## 🐳 Run with Docker

### 1. Build Docker image
```bash
docker build -t sitracker-cli:latest .
```

### 2. Run container with mounted credentials
```bash
docker run --rm   -v /absolute/path/to/credentials.json:/app/credentials.json   -e GOOGLE_APPLICATION_CREDENTIALS=/app/credentials.json   -e SITRACKER_SPREADSHEET_ID=YOUR_SPREADSHEET_ID   sitracker-cli:latest create -d "Issue created from Docker"
```

Example: list open issues
```bash
docker run --rm   -v /absolute/path/to/credentials.json:/app/credentials.json   -e GOOGLE_APPLICATION_CREDENTIALS=/app/credentials.json   -e SITRACKER_SPREADSHEET_ID=YOUR_SPREADSHEET_ID   sitracker-cli:latest list -s OPEN
```

---

## 📖 CLI Usage

Root command:
```bash
java -jar sitracker.jar --help
```

Subcommands:

- **Create issue**
  ```bash
  create -d "Issue description" [-p PARENT_ID]
  ```

- **Update issue status**
  ```bash
  update <ISSUE_ID> -s <OPEN|IN_PROGRESS|CLOSED>
  ```

- **List issues by status**
  ```bash
  list -s <OPEN|IN_PROGRESS|CLOSED>
  ```

---

## 🧪 Testing

Run unit tests:
```bash
mvn test
```

---

## 📂 Project Structure

```
src/main/java/org/example/sitracker
├── cli/          # CLI commands (Picocli)
├── config/       # Google Sheets config
├── domain/       # Domain models (Issue, Status)
├── repository/   # Repository layer (Google Sheets)
├── service/      # Business logic
├── CliRunner.java   # Wires CLI + Spring Boot
└── SiTrackerApplication.java # Main entrypoint
```

---

## 👩‍💻 Author

Developed by [Iryna Khrustalova](https://github.com/IrynaKhrustalova)

---

# **StoreFlow**

### **Prerequisites**
- **Java 21**
- **Apache Maven**
- **Docker**

### **Instructions**

#### Clone the repository
```bash
git clone https://github.com/your-repository/ordercore.git
cd ordercore
```

#### Build the project
```bash
mvn clean install -DskipTests
```

#### Start the application
Runs on Docker — port **8080** for the app, port **3306** for MySQL
```bash
docker-compose up --build
```

The application will start on `http://localhost:8080`. Once running, visit the Swagger UI:
```
http://localhost:8080/swagger-ui/index.html
```

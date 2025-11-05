# Quick Start
## 1. Clone the repo
```bash
git clone -b 2.0.0-demo https://github.com/thridparty-cloud2/my-protocol-example
```
## 2. Package the project
```bash
mvn clean package 
```
## 3. Run Tests
```bash
## Authentication test
mvn test -Dtest=AuthenticationTest
## Message codec test
mvn test -Dtest=MessageCodecTest
```
# Develop Your Custom Protocol
## Implementation Steps
### 1. Device Authentication Logic
- Navigate to MyAuthenticator.java
- Implement your custom device authentication logic
### 2. Message Encoding/Decoding
- For MQTT protocol: Modify MyMqttMessageCodec.java
- For LwM2M protocol: Modify MyLwM2MMessageCodec.java
- Implement custom encoding/decoding logic
### 3. Testing
- Create and run test classes to validate your implementation
- Ensure all edge cases are covered
### 4. Deployment
- Package the project into a JAR file
- Upload the compiled package to the platform
# Documentation Reference
For detailed specifications and guidelines, please refer to:
《Private Cloud Standard Edition - Custom Protocol Development Guide V2.0》

# System Requirements

## Java Development Kit
- **Recommended**: Java 8 or later (1.8.+)
- **Note**: The project is compatible with Java 8 and all subsequent versions

> **Note:** protocol support version: 2.0.0

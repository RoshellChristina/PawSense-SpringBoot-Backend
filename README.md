# PawSense Spring Boot Backend

This is the backend service for PawSense, a pet management platform.

## Prerequisites

Before running the application, you need to provide your own API tokens and keys. Add them into your application.properties

```properties
spring.application.name=petapp
server.address=0.0.0.0
server.port=8080

# GitHub AI token
github.ai.token=YOUR_GITHUB_AI_TOKEN_HERE

# Google API key
google.api.key=YOUR_GOOGLE_API_KEY_HERE


# Logging
logging.level.org.springframework.web=DEBUG
logging.level.org.springframework.web.servlet.DispatcherServlet=DEBUG
logging.level.org.springframework.jdbc=DEBUG

# Flask embed service URL
embedding.service.url=http://localhost:6000/embed

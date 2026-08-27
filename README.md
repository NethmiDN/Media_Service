# Media Service

A Spring Boot microservice for uploading media files directly to Google Cloud Storage (GCS) and persisting media metadata in MySQL.

## Overview

This service exposes REST endpoints for:

- uploading media to GCS
- storing metadata in the database
- retrieving media by media ID or event ID
- deleting media from GCS and the database

It is intended to act as a reusable media component inside a larger distributed application architecture.

## Tech Stack

- Java 21
- Spring Boot 3.4.3
- Spring Web
- Spring Data JPA
- Spring Boot Actuator
- MySQL
- Spring Cloud Netflix Eureka Client
- Spring Cloud Config Client
- Google Cloud Storage via Spring Cloud GCP
- Maven

## Project Structure

```text
media-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/mediaservice/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── exception/
│   │   │       ├── model/
│   │   │       ├── repository/
│   │   │       ├── service/
│   │   │       └── MediaServiceApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── memory-key.json
│   └── test/
├── pom.xml
├── mvnw
├── mvnw.cmd
├── uploaded-media/
└── README.md
```

## Features

- Upload a file to GCS and return persisted metadata
- Store photo metadata in the `photos` table
- Retrieve media by `id`
- Retrieve media by `eventId`
- Delete media by database ID
- Expose actuator health status
- Global exception handling for storage and validation errors

## Configuration

The application configuration is defined in [src/main/resources/application.yml](src/main/resources/application.yml):

```yaml
spring:
  application:
    name: media-service
  config:
    import: optional:configserver:http://localhost:8888

gcp:
  project-id: nethmi-project
  bucket:
    name: my-memory-bucket
  credentials:
    location: classpath:memory-key.json
```

### Required setup

- A Google Cloud project with Cloud Storage enabled
- A GCS bucket named `my-memory-bucket` or an updated bucket name in config
- A service account JSON key at `src/main/resources/memory-key.json`
- A MySQL database for JPA metadata storage
- Optional Spring Cloud Config Server running at `http://localhost:8888`

## Database Model

Metadata is stored in the `photos` table using the `PhotoMetadata` entity.

Fields include:

- `id`
- `userId`
- `eventId`
- `photoUrl`
- `fileName`
- `contentType`
- `uploadedAt`

## Running the Service

### Using Maven

```bash
./mvnw clean install
./mvnw spring-boot:run
```

On Windows:

```powershell
mvnw.cmd clean install
mvnw.cmd spring-boot:run
```

The app starts on port `8080` unless configured otherwise.

## API Endpoints

Base path: `/api/media`

### Upload media

```http
POST /api/media/upload
```

Form-data parameters:

- `file` - multipart file
- `userId` - optional long value
- `eventId` - optional string value

Example:

```bash
curl -X POST "http://localhost:8080/api/media/upload" \
  -F "file=@/path/to/image.jpg" \
  -F "userId=1" \
  -F "eventId=trip-123"
```

### Get media by ID

```http
GET /api/media/{id}
```

### Get media by event

```http
GET /api/media/event/{eventId}
```

### Delete media

```http
DELETE /api/media/{id}
```

### Health check

```http
GET /actuator/health
```

## Example Response

Successful upload:

```json
{
  "id": 1,
  "userId": 1,
  "eventId": "trip-123",
  "fileName": "uuid.jpg",
  "contentType": "image/jpeg",
  "photoUrl": "https://storage.googleapis.com/my-memory-bucket/uuid.jpg",
  "uploadedAt": "2026-08-27T12:00:00"
}
```

## Notes

- The application uses Spring Cloud Config with an optional remote config server.
- Google credentials are loaded from `classpath:memory-key.json` by default.
- The `uploaded-media` folder appears to be a local working directory for uploaded content, but the primary storage backend is GCS.

## License

This project does not currently declare a formal license in the repository.

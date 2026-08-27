# Media Service

A Spring Boot microservice for uploading and managing media files in Google Cloud Storage (GCS), while persisting metadata such as user ID, destination ID, filename, and URL in MySQL.

## Overview

This service exposes REST endpoints for:

- uploading files to GCS
- storing metadata in the database
- retrieving media by user or destination
- deleting files from GCS

It is designed to work as a reusable media component in a larger distributed application architecture.

## Tech Stack

- Java 21
- Spring Boot 3.4.3
- Spring Web
- Spring Data JPA
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

- Upload a file to GCS and return a public URL
- Store photo metadata in the `photos` table
- Retrieve images by `userId`
- Retrieve images by `destinationId`
- Delete a GCS object using filename or public URL
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
- A GCS bucket named `my-memory-bucket` (or update the value in config)
- A service account JSON key at `src/main/resources/memory-key.json`
- A MySQL database for JPA metadata storage
- Optional Spring Cloud Config Server running at `http://localhost:8888`

## Database Model

Metadata is stored in the `photos` table using the `PhotoMetadata` entity.

Fields include:

- `id`
- `userId`
- `destinationId`
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

The app starts on the default Spring Boot port `8080` unless configured otherwise.

## API Endpoints

Base path: `/api/media`

### Upload a file

```http
POST /api/media/upload
```

Form-data parameters:

- `file` - multipart file
- `userId` - optional long value
- `destinationId` - optional string value

Behavior:

- If `userId` or `destinationId` is provided, the service stores metadata in MySQL
- Otherwise, it returns a public GCS URL in the response body

Example:

```bash
curl -X POST "http://localhost:8080/api/media/upload" \
  -F "file=@/path/to/image.jpg" \
  -F "userId=1" \
  -F "destinationId=trip-123"
```

### Get media by user

```http
GET /api/media/user/{userId}
```

### Get media by destination

```http
GET /api/media/event/{destinationId}
```

### Delete a file

```http
DELETE /api/media/delete?fileNameOrUrl=https://storage.googleapis.com/my-memory-bucket/filename.jpg
```

You can also send a `file` parameter instead of `fileNameOrUrl`.

## Example Response

Successful upload without metadata:

```json
{
  "message": "File uploaded successfully to GCS",
  "url": "https://storage.googleapis.com/my-memory-bucket/uuid.jpg"
}
```

Successful upload with metadata:

```json
{
  "id": 1,
  "userId": 1,
  "destinationId": "trip-123",
  "fileName": "uuid.jpg",
  "contentType": "image/jpeg",
  "photoUrl": "https://storage.googleapis.com/my-memory-bucket/uuid.jpg",
  "uploadedAt": "2026-08-27T12:00:00"
}
```

## Notes

- The application uses `Spring Cloud Config` with an optional remote config server.
- Google credentials are loaded from `classpath:memory-key.json` by default.
- The `uploaded-media` folder appears to be a local working directory for uploaded content, but the primary storage backend is GCS.

## License

This project does not currently declare a formal license in the repository.

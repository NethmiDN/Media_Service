# Media Service

A cloud-native media management microservice for the enterprise platform, designed to upload files to Google Cloud Storage (GCS), persist metadata in MySQL, and integrate with the core Spring Cloud infrastructure for service discovery and centralized configuration.

## Project Information

| Field | Details |
| :--- | :--- |
| Student Name | Nethmi Nanayakkara |
| Student ID | 241722047 |
| GCP Project ID | `nethmi-project` |
| Module | ITS 2130 - Enterprise Cloud Architecture |
| Repository Type | Media Storage Microservice |
| Service Role | File Upload, Metadata Storage, and Media Retrieval |

## Architectural Overview

This service is a core platform component within the broader enterprise microservice ecosystem. It is designed to work alongside the API Gateway, Config Server, and Eureka Server to provide a scalable, distributed backend infrastructure.

```text
Client / Browser
        |
        v
[External Load Balancer]
        |
        v
[API Gateway :8080]
        |
        +--> [Media Service :8081]
        |
        +--> [Other Platform Services]

[Media Service]
  - Uploads files to GCS
  - Stores metadata in MySQL
  - Registers with Eureka
  - Loads runtime config from Config Server
```

## Service Role

The Media Service is responsible for:

- uploading media files to Google Cloud Storage
- persisting media metadata in a relational database
- retrieving media content by ID or event ID
- deleting media files and associated metadata
- exposing health and operational status for platform monitoring

This service acts as the storage and media-processing boundary for the platform, supporting event-driven and API-driven application workflows.

## Technology Stack

- Java 21
- Spring Boot 3.4.x
- Spring Web
- Spring Data JPA
- Spring Validation
- MySQL
- Google Cloud Storage via Spring Cloud GCP
- Spring Cloud Netflix Eureka Client
- Spring Cloud Config Client
- Maven

## Runtime Configuration

The service is configured through `src/main/resources/application.yml`:

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

### Required dependencies

- Google Cloud project with Cloud Storage enabled
- GCS bucket named `my-memory-bucket` or a configured replacement bucket
- Service account credentials file at `src/main/resources/memory-key.json`
- MySQL database instance for metadata persistence
- Spring Cloud Config Server running on `http://localhost:8888`
- Eureka Server available for service registration and discovery

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
├── target/
└── README.md
```

## Core Features

- upload multimedia files to Google Cloud Storage
- store file metadata in the `photos` table
- retrieve media by `id`
- retrieve media by `eventId`
- delete stored metadata and cloud object
- expose actuator health status for monitoring
- handle storage and validation exceptions globally

## Database Model

The service stores media metadata in the `photos` table using the `PhotoMetadata` entity.

Fields include:

- `id`
- `userId`
- `eventId`
- `photoUrl`
- `fileName`
- `contentType`
- `uploadedAt`

## Cloning and Local Setup

Clone the repository and run it locally with Maven:

```bash
git clone https://github.com/NethmiDN/media-service.git
cd media-service
./mvnw clean install
./mvnw spring-boot:run
```

On Windows:

```powershell
mvnw.cmd clean install
mvnw.cmd spring-boot:run
```

The service is expected to register with the platform discovery layer and fetch configuration from the Config Server before startup.

## Deployment Topology

- API Gateway routes external requests to the service
- Media Service runs behind the platform gateway boundary
- Google Cloud Storage stores uploaded media objects
- MySQL persists media metadata and access records
- Eureka maintains runtime service registration
- Config Server provides centralized runtime properties

## API Endpoints

Base path: `/api/media`

### Upload media

```http
POST /api/media/upload
```

Form-data parameters:

- `file` - multipart file
- `userId` - optional numeric user identifier
- `eventId` - optional event identifier

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

- This service is intended to operate as part of a larger distributed Spring Cloud platform.
- The primary storage backend is Google Cloud Storage, not the local `uploaded-media` directory.
- Local file storage is mainly used for development or temporary working content.
- The application depends on the platform infrastructure services for service discovery and configuration at runtime.

## License

This repository does not currently declare a formal software license.

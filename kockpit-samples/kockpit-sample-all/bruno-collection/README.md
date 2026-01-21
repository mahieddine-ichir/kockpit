# Kockpit Sample All - Bruno API Collection

This Bruno collection provides comprehensive API testing capabilities for the Kockpit Sample All application and related backend services.

## Overview

Bruno is a modern API testing tool that uses plain text files for collections, making it version control friendly and easy to collaborate on.

## Collection Structure

```
bruno-collection/
├── bruno.json                    # Collection configuration
├── environments/
│   └── Development.bru           # Environment variables
├── Sample App APIs/              # Sample application endpoints
│   ├── Say Hello.bru
│   ├── Create Message.bru
│   └── Evaluate Feature Flag.bru
├── Authentication/               # Authentication endpoints
│   └── Get Current User.bru
├── HeartBeat Monitoring/         # Health monitoring endpoints
│   ├── Get Active Heartbeats.bru
│   └── Get All Heartbeats.bru
├── Dashboard Analytics/          # Dashboard and metrics endpoints
│   ├── Get Application Details.bru
│   └── Get Application Distribution Data.bru
├── Audit Management/             # Audit log endpoints
│   ├── Get Audit by ID.bru
│   ├── List Audits.bru
│   └── Search Audits (POST).bru
├── Feature Management/           # Feature flags and config
│   └── Update Feature Flag.bru
└── Health Monitoring/            # Application health endpoints
    ├── Health Check.bru
    └── Application Info.bru
```

## Environment Variables

The collection includes pre-configured environment variables in `environments/Development.bru`:

- `BASE_URL`: http://localhost:8080/backend/api (Kockpit backend)
- `SAMPLE_BASE_URL`: http://localhost:8081/sample-app (Sample app)
- `domain`: sample
- `env`: dev
- `name`: World
- `feature_key`: sample-feature
- `auth_token`: YWRtaW46YWRtaW4= (admin:admin base64)

## Getting Started

1. **Install Bruno**: Download from [https://www.usebruno.com/](https://www.usebruno.com/)

2. **Open Collection**:
   - Launch Bruno
   - Click "Open Collection"
   - Navigate to this `bruno-collection` folder
   - Select the folder to open the collection

3. **Set Environment**:
   - Select "Development" environment from the environment dropdown
   - Modify variables as needed for your setup

4. **Start Testing**:
   - Ensure your applications are running:
     - Kockpit backend on port 8080
     - Sample app on port 8081
   - Select any request and click "Send"

## Authentication

Most backend endpoints require basic authentication:
- **Username**: admin
- **Password**: admin

This is pre-configured in the requests that require authentication.

## Sample Requests

### Sample App APIs
- **Say Hello**: GET request to test the greeting endpoint
- **Create Message**: POST request to create a new message
- **Evaluate Feature Flag**: GET request to check feature flag status

### Backend APIs
- **Authentication**: Get current user information
- **HeartBeat**: Monitor application health
- **Dashboard**: Get analytics and metrics
- **Audit**: Search and retrieve audit logs
- **Feature Management**: Manage feature flags
- **Health Monitoring**: Application health checks

## Customization

You can easily customize this collection by:

1. **Adding New Requests**: Create new `.bru` files in appropriate folders
2. **Modifying Variables**: Edit the environment file or add new environments
3. **Updating Authentication**: Modify auth settings in individual requests
4. **Adding Tests**: Bruno supports JavaScript tests and assertions

## Features

- **Version Control Friendly**: Plain text files work great with Git
- **Environment Management**: Easy switching between different environments
- **Authentication Support**: Built-in support for various auth methods
- **Organized Structure**: Logical folder organization for easy navigation
- **Documentation**: Each request includes documentation

## Support

For more information about Bruno, visit:
- **Website**: https://www.usebruno.com/
- **Documentation**: https://docs.usebruno.com/
- **GitHub**: https://github.com/usebruno/bruno
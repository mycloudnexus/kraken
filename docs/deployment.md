# Deployment

## app-controller 

### Prerequisites
1. PostgreSQL DB Server is UP.
2. Configure the DB Server URL in the `application.yaml`.

To support HA, you can start multiple instances of this service. All instances can point to the same DB.

## app-hub

### Prerequisites
1. PostgreSQL DB Server is UP.
2. Configure the DB Server URL in the `application.yaml`.

To support HA, you can start multiple instances of this service. All instances can point to the same DB.

## app-agent

### Prerequisites
1. PostgreSQL DB Server is UP.
2. Configure the DB Server URL in the `application.yaml`.
3. The DB Server MUST be the same as the one for `app-hub`.

Only one instance of this service should be running.

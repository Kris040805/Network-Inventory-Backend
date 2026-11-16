pipeline {
    agent any

    environment {
        COMPOSE_PROJECT_NAME = 'network-inventory-ci'
        DB_NAME = 'inventory'
        DB_SA_PASSWORD = credentials('db-sa-password')
        DB_APP_PASSWORD = credentials('db-app-password')
        API_ADMIN_PASSWORD = credentials('api-admin-password')
    }

    stages {
        stage('Build') {
            steps {
                sh 'docker compose --profile test build'
            }
        }

    }

}
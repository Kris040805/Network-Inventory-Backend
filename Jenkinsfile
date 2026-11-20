pipeline {
    agent any

    environment {
        COMPOSE_PROJECT_NAME = 'network-inventory-ci'
        DB_NAME              = 'inventory'
        DB_SA_PASSWORD       = credentials('db-sa-password')
        DB_APP_PASSWORD      = credentials('db-app-password')
        API_ADMIN_PASSWORD   = credentials('api-admin-password')
    }

    stages {
        stage('Build') {
            steps {
                sh 'docker compose build'
            }
        }

        stage('Run') {
            steps {
                sh 'docker compose down -v'
                sh 'docker compose up -d'
                sh '''
                    for i in $(seq 1 30); do
                        status=$(docker inspect --format '{{.State.Health.Status}}' $(docker compose ps -q app) || true)
                        echo "app health: $status"
                        if [ "$status" = "healthy" ]; then
                            exit 0
                        fi
                        sleep 5
                    done
                    echo "app did not become healthy in time"
                    exit 1
                '''
            }
        }
    }

    post {
        failure {
            sh 'docker compose logs --no-color || true'
        }
        cleanup {
            sh 'docker compose down -v || true'
        }
    }
}
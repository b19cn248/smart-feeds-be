pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'smart-feeds-api'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        TELEGRAM_CHAT_ID = '-4754148533'
        TELEGRAM_BOT_TOKEN = credentials('telegram-bot-token')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Maven') {
            agent {
                docker {
                    image 'maven:3.9.9-eclipse-temurin-21-alpine'
                    reuseNode true
                }
            }
            steps {
                sh '''
                echo "Sử dụng Maven Docker image để build"
                mvn clean package -DskipTests
                '''
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} -f docker/Dockerfile ."
                sh "docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest"
            }
        }

        stage('Deploy') {
            steps {
                sh 'docker-compose -f docker/docker-compose.yml down || true'
                sh "DOCKER_TAG=${DOCKER_TAG} docker-compose -f docker/docker-compose.yml up -d"
            }
        }

        stage('Health Check') {
            steps {
                sh '''
                # Đợi ứng dụng khởi động
                echo "Đợi ứng dụng khởi động..."
                sleep 30

                # Kiểm tra trạng thái của container
                CONTAINER_STATUS=$(docker ps -a --filter "name=smart-feeds-api" --format "{{.Status}}")
                echo "Trạng thái container: $CONTAINER_STATUS"

                # Kiểm tra API có phản hồi không
                HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8688/actuator/health || echo "000")

                if [[ $HTTP_CODE == "200" ]]; then
                    echo "Ứng dụng đã triển khai thành công. API phản hồi với mã HTTP: $HTTP_CODE"
                else
                    echo "Kiểm tra sức khỏe thất bại. API phản hồi với mã HTTP: $HTTP_CODE"
                fi
                '''
            }
        }
    }

    post {
        success {
            script {
                sh """
                curl -s -X POST https://api.telegram.org/bot\${TELEGRAM_BOT_TOKEN}/sendMessage \
                -d chat_id=${TELEGRAM_CHAT_ID} \
                -d parse_mode="HTML" \
                -d text="✅ <b>Build Thành Công!</b> \n<b>Dự án:</b> Smart Feeds API \n<b>Nhánh:</b> ${env.GIT_BRANCH} \n<b>Số Build:</b> ${env.BUILD_NUMBER} \n<b>URL Build:</b> ${env.BUILD_URL}"
                """
            }
        }
        failure {
            script {
                sh """
                curl -s -X POST https://api.telegram.org/bot\${TELEGRAM_BOT_TOKEN}/sendMessage \
                -d chat_id=${TELEGRAM_CHAT_ID} \
                -d parse_mode="HTML" \
                -d text="❌ <b>Build Thất Bại!</b> \n<b>Dự án:</b> Smart Feeds API \n<b>Nhánh:</b> ${env.GIT_BRANCH} \n<b>Số Build:</b> ${env.BUILD_NUMBER} \n<b>URL Build:</b> ${env.BUILD_URL}"
                """
            }
        }
        always {
            cleanWs()
        }
    }
}
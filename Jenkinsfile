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

        stage('Setup Maven') {
            steps {
                script {
                    def mvnHome = tool name: 'Maven', type: 'maven'
                    env.PATH = "${mvnHome}/bin:${env.PATH}"

                    // Kiểm tra cài đặt Maven
                    sh 'mvn --version || (apt-get update && apt-get install -y maven)'
                }
            }
        }

        stage('Build Maven') {
            steps {
                sh '''
                # Sử dụng Maven Wrapper nếu có
                if [ -f "./mvnw" ]; then
                    chmod +x ./mvnw
                    ./mvnw clean package -DskipTests
                else
                    mvn clean package -DskipTests
                fi
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
    }
}
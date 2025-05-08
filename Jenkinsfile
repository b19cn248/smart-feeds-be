pipeline {
    agent any

    environment {
        TELEGRAM_BOT_TOKEN = credentials('telegram-bot-token')
        TELEGRAM_CHAT_ID = credentials('telegram-chat-id')
    }

    stages {
        stage('Manual Checkout') {
            steps {
                // Thực hiện clone thủ công
                sh '''
                    rm -rf ./*
                    git clone https://github.com/b19cn248/smart-feeds-be.git .
                    git checkout develop
                '''

                sh '''
                    curl -s -X POST https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage \
                    -d chat_id=${TELEGRAM_CHAT_ID} \
                    -d parse_mode=markdown \
                    -d text="🔄 *Bắt đầu Build*: Dự án Smart-Feeds đang được build trên nhánh develop"
                '''
            }
        }

        stage('Build and Deploy') {
            steps {
                sh '''
                    cd docker
                    docker compose down
                    docker compose up -d --build
                '''
            }
        }
    }

    post {
        success {
            sh '''
                curl -s -X POST https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage \
                -d chat_id=${TELEGRAM_CHAT_ID} \
                -d parse_mode=markdown \
                -d text="✅ *Build Thành công*: Dự án Smart-Feeds đã được triển khai thành công"
            '''
        }
        failure {
            sh '''
                curl -s -X POST https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage \
                -d chat_id=${TELEGRAM_CHAT_ID} \
                -d parse_mode=markdown \
                -d text="❌ *Build Thất bại*: Build dự án Smart-Feeds thất bại. Vui lòng kiểm tra Jenkins để biết chi tiết."
            '''
        }
    }
}
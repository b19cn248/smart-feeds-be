pipeline {
    agent any

    environment {
        // Định nghĩa các biến môi trường
        TELEGRAM_BOT_TOKEN = credentials('telegram-bot-token')
        TELEGRAM_CHAT_ID = credentials('telegram-chat-id')
    }

    stages {
        stage('Checkout') {
            steps {
                // Dọn dẹp trước khi checkout
                cleanWs()
                checkout scm

                // Gửi thông báo - build bắt đầu
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
                // Di chuyển đến thư mục docker và rebuild các container
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
            // Gửi thông báo thành công
            sh '''
                curl -s -X POST https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage \
                -d chat_id=${TELEGRAM_CHAT_ID} \
                -d parse_mode=markdown \
                -d text="✅ *Build Thành công*: Dự án Smart-Feeds đã được triển khai thành công"
            '''
        }
        failure {
            // Gửi thông báo thất bại
            sh '''
                curl -s -X POST https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage \
                -d chat_id=${TELEGRAM_CHAT_ID} \
                -d parse_mode=markdown \
                -d text="❌ *Build Thất bại*: Build dự án Smart-Feeds thất bại. Vui lòng kiểm tra Jenkins để biết chi tiết."
            '''
        }
    }
}
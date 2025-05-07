pipeline {
    agent any

    environment {
        TELEGRAM_BOT_TOKEN = credentials('telegram-bot-token')
        TELEGRAM_CHAT_ID = credentials('telegram-chat-id')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scmGit(
                    branches: [[name: '*/develop']],
                    extensions: [],
                    userRemoteConfigs: [[
                        credentialsId: 'github-credentials',
                        url: 'https://github.com/YOUR_USERNAME/smart-feeds.git'
                    ]]
                )
                sh 'git rev-parse --short HEAD > .git/commit-id'
                script {
                    env.COMMIT_ID = readFile('.git/commit-id').trim()
                }
            }
        }

        stage('Build and Deploy') {
            steps {
                sh '''
                    cd docker
                    docker-compose down
                    docker-compose up -d --build
                '''
            }
        }
    }

    post {
        success {
            sh """
                curl -s -X POST https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage \
                -d chat_id=${TELEGRAM_CHAT_ID} \
                -d parse_mode=Markdown \
                -d text='✅ *Smart Feeds build và deploy thành công!*\nCommit: ${env.COMMIT_ID}\nBranch: develop'
            """
        }
        failure {
            sh """
                curl -s -X POST https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage \
                -d chat_id=${TELEGRAM_CHAT_ID} \
                -d parse_mode=Markdown \
                -d text='❌ *Smart Feeds build thất bại!*\nCommit: ${env.COMMIT_ID}\nBranch: develop'
            """
        }
    }
}
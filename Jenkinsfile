pipeline {
    agent any

    environment {
        AWS_ACCOUNT_ID = 'YOUR_AWS_ACCOUNT_ID'
        AWS_DEFAULT_REGION = 'us-east-1'
        IMAGE_REPO_NAME_PREFIX = 'booknest'
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        ECR_REGISTRY = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Backend') {
            steps {
                dir('Backend') {
                    sh './mvnw clean package -DskipTests'
                }
            }
        }

        stage('Build Frontend') {
            steps {
                // Automatically clone the frontend repo into a local folder
                sh 'rm -rf frontend-build'
                sh 'git clone https://github.com/Divyansh-Pandey24/BookStoreEcommercePlatform-Frontend.git frontend-build'
                dir('frontend-build') {
                    sh 'npm install'
                    sh 'npm run build'
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                script {
                    // Login to ECR
                    sh "aws ecr get-login-password --region ${AWS_DEFAULT_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}"

                    def backendServices = [
                        'api-gateway': 'Backend/api-gateway',
                        'eureka-server': 'Backend/eureka-server',
                        'auth-service': 'Backend/auth-service',
                        'book-service': 'Backend/book-service-1',
                        'cart-service': 'Backend/cart-service',
                        'order-service': 'Backend/order-service',
                        'wallet-service': 'Backend/wallet-service',
                        'notification-service': 'Backend/notification-service',
                        'review-service': 'Backend/review-service',
                        'admin-server': 'Backend/admin-server'
                    ]

                    backendServices.each { name, path ->
                        echo "Building and Pushing ${name}..."
                        sh "docker build -t ${ECR_REGISTRY}/${IMAGE_REPO_NAME_PREFIX}-${name}:${IMAGE_TAG} ${path}"
                        sh "docker push ${ECR_REGISTRY}/${IMAGE_REPO_NAME_PREFIX}-${name}:${IMAGE_TAG}"
                        sh "docker tag ${ECR_REGISTRY}/${IMAGE_REPO_NAME_PREFIX}-${name}:${IMAGE_TAG} ${ECR_REGISTRY}/${IMAGE_REPO_NAME_PREFIX}-${name}:latest"
                        sh "docker push ${ECR_REGISTRY}/${IMAGE_REPO_NAME_PREFIX}-${name}:latest"
                    }

                    // Build and Push Frontend
                    echo "Building and Pushing frontend..."
                    sh "docker build -t ${ECR_REGISTRY}/${IMAGE_REPO_NAME_PREFIX}-frontend:${IMAGE_TAG} frontend-build"
                    sh "docker push ${ECR_REGISTRY}/${IMAGE_REPO_NAME_PREFIX}-frontend:${IMAGE_TAG}"
                    sh "docker tag ${ECR_REGISTRY}/${IMAGE_REPO_NAME_PREFIX}-frontend:${IMAGE_TAG} ${ECR_REGISTRY}/${IMAGE_REPO_NAME_PREFIX}-frontend:latest"
                    sh "docker push ${ECR_REGISTRY}/${IMAGE_REPO_NAME_PREFIX}-frontend:latest"
                }
            }
        }

        stage('Deploy to AWS') {
            steps {
                echo "Deployment step: In a production environment, you would use 'aws ecs update-service' or an SSH script to run docker-compose on EC2."
                // Example SSH deployment:
                // sshagent(['my-ssh-credential-id']) {
                //     sh "ssh -o StrictHostKeyChecking=no ubuntu@YOUR_EC2_IP 'cd /home/ubuntu/app && docker-compose pull && docker-compose up -d'"
                // }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}

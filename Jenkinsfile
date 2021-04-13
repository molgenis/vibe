pipeline {
    agent {
        kubernetes {
            label 'molgenis-jdk11'
        }
    }
    environment {
        LOCAL_REPOSITORY = "${LOCAL_REGISTRY}/molgenis/vibe"
        TIMESTAMP = sh(returnStdout: true, script: "date -u +'%F_%H-%M-%S'").trim()
    }
    stages {
        stage('Prepare') {
            steps {
                script {
                    env.GIT_COMMIT = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                }
                container('vault') {
                    script {
                        sh "mkdir ${JENKINS_AGENT_WORKDIR}/.m2"
                        sh(script: "vault read -field=value secret/ops/jenkins/maven/settings.xml > ${JENKINS_AGENT_WORKDIR}/.m2/settings.xml")
                        env.GITHUB_TOKEN = sh(script: 'vault read -field=value secret/ops/token/github', returnStdout: true)
                        env.SONAR_TOKEN = sh(script: 'vault read -field=value secret/ops/token/sonar', returnStdout: true)
                        env.PGP_PASSPHRASE = 'literal:' + sh(script: 'vault read -field=passphrase secret/ops/certificate/pgp/molgenis-ci', returnStdout: true)
                        env.GITHUB_USER = sh(script: 'vault read -field=username secret/ops/token/github', returnStdout: true)
                    }
                }
            }
        }
        stage('Build: [ pull request ]') {
            when {
                changeRequest()
            }
            environment {
                // PR-1234-231
                TAG = "PR-${CHANGE_ID}-${BUILD_NUMBER}"
            }
            steps {
                container('maven') {
                    sh "sh TestsPreprocessor.sh"
                    sh "mvn clean install -Dmaven.test.redirectTestOutputToFile=true -DexcludedGroups='skipOnJenkins' -T4"
                }
            }
            post {
                always {
                    container('maven') {
                        // Fetch the target branch, sonar likes to take a look at it
                        sh "git fetch --no-tags origin ${CHANGE_TARGET}:refs/remotes/origin/${CHANGE_TARGET}"
                        sh "mvn -q -B sonar:sonar -Dsonar.login=${env.SONAR_TOKEN} -Dsonar.github.oauth=${env.GITHUB_TOKEN} -Dsonar.pullrequest.base=${CHANGE_TARGET} -Dsonar.pullrequest.branch=${BRANCH_NAME} -Dsonar.pullrequest.key=${env.CHANGE_ID} -Dsonar.pullrequest.provider=GitHub -Dsonar.pullrequest.github.repository=molgenis/vibe -Dsonar.ws.timeout=120"
                    }
                }
            }
        }
        stage('Build: [ master ]') {
            when {
                branch 'master'
            }
            environment {
                TAG = "dev-${TIMESTAMP}"
            }
            steps {
                milestone 1
                container('maven') {
                    sh "sh TestsPreprocessor.sh"
                    sh "mvn clean install -Dmaven.test.redirectTestOutputToFile=true -DexcludedGroups='skipOnJenkins' -T4"
                }
            }
            post {
                always {
                    container('maven') {
                        sh "mvn -q -B sonar:sonar -Dsonar.login=${SONAR_TOKEN} -Dsonar.ws.timeout=120"
                    }
                }
            }
        }
        stage('Steps: [ x.x ]') {
            when {
                expression { BRANCH_NAME ==~ /[0-9]+\.[0-9]+/ }
            }
            stages {
                stage('Build [ x.x ]') {
                    steps {
                        container('maven') {
                            sh "sh TestsPreprocessor.sh"
                            sh "mvn -q -B clean install -Dmaven.test.redirectTestOutputToFile=true -DexcludedGroups='skipOnJenkins' -T4"
                            sh "mvn -q -B sonar:sonar -Dsonar.login=${SONAR_TOKEN} -Dsonar.branch.name=${BRANCH_NAME} -Dsonar.ws.timeout=120"
                        }
                    }
                }
                stage('Prepare Release [ x.x ]') {
                    steps {
                        timeout(time: 40, unit: 'MINUTES') {
                            input(message: 'Prepare to release?')
                        }
                        container('maven') {
                            sh "mvn -q -B release:prepare -Dmaven.test.redirectTestOutputToFile=true -DexcludedGroups='skipOnJenkins' -Darguments=\"-q -B -Dmaven.test.redirectTestOutputToFile=true -DexcludedGroups='skipOnJenkins'\""
                        }
                    }
                }
                stage('Perform release [ x.x ]') {
                    steps {
                        container('vault') {
                            script {
                                env.PGP_SECRETKEY = "keyfile:${JENKINS_AGENT_WORKDIR}/key.asc"
                                sh(script: "vault read -field=secret.asc secret/ops/certificate/pgp/molgenis-ci > ${JENKINS_AGENT_WORKDIR}/key.asc")
                            }
                        }
                        container('maven') {
                            sh "mvn -q -B release:perform -Darguments=\"-q -B -DskipTests -Dmaven.test.redirectTestOutputToFile=true -Pjenkins_release\""
                        }
                    }
                }
                stage('Manually close and release on sonatype [ x.x ]') {
                    steps {
                        input(message='Log on to https://oss.sonatype.org/ and manually close and release to maven central.')
                    }
                }
            }
        }
    }
}

#!groovy

node {
    try {
        stage('Checkout') {
            checkout scm
        }

        // Mark the code build
        stage('Build') {

            withMaven(mavenLocalRepo: '.repository', mavenSettingsFilePath: "${env.MVN_SETTINGS_PATH}") {

                // Run the maven build
                sh "mvn clean install"

                step([$class: 'JacocoPublisher'])
                step([$class: 'TasksPublisher', canComputeNew: false, defaultEncoding: '', excludePattern: '', healthy: '', high: 'FIXME', ignoreCase: true, low: '', normal: 'TODO', pattern: '', unHealthy: ''])
            }
        }


    } catch (any) {
        currentBuild.result = 'FAILURE'
        throw any //rethrow exception to prevent the build from proceeding
    } finally {

        stage('Reporting') {

            // Notify on build failure using the Email-ext plugin
            emailext(body: '${DEFAULT_CONTENT}', mimeType: 'text/html',
                    replyTo: '$DEFAULT_REPLYTO', subject: '${DEFAULT_SUBJECT}',
                    to: emailextrecipients([[$class: 'CulpritsRecipientProvider'],
                                            [$class: 'RequesterRecipientProvider']]))
        }
    }
}


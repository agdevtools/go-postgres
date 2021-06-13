// Run on an agent where we want to use Go
node {
    // Ensure the desired Go version is installed
    def root = tool type: 'go', name: 'Go 1.15'

    // Export environment variables pointing to the directory where Go was installed
    withEnv(["GOROOT=${root}", "PATH+GO=${root}/bin"]) {
        sh 'go version'
    }
}



node {
    def gitRepository = 'https://github.com/agdevtools/go-postgres.git/'
    def gitBranch = '*/master'
    def githubcreds = [
            $class      : 'UsernamePasswordMultiBinding',
            credentialsId : 'githubcreds',
            usernameVariable : 'GIT_USER',
            passwordVariable : 'GIT_PASS'
    ]

    stage('Clean Workspace') {
        cleanWs()
    }

    stage('Git Checkout') {
        withCredentials([githubcreds]){
            checkout([
                    $class      : 'GitSCM',
                    branches    : [[name:"${gitBranch}"]],
                    doGenerateSubModuleConfigurations : false,
                    extensions: [],
                    submoduleCfg: [],
                    userRemoteConfigs: [[credentialsId  : 'githubcreds',
                                         url            :"${gitRepository}"]]

            ])

        }
    }

    stage('Build') {

        sh 'go build -o bin/main main/main.go'

    }

    stage('Test') {

        sh 'echo running tests ...'

    }

    stage('Deploy to Heroku') {

        sh 'git commit --allow-empty -m "Trigger Heroku deploy after enabling collectstatic"'
        sh 'git push'

    }
}
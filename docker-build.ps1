$BUILD=$args[0]

if('-b' -eq $BUILD){
    Write-Host("Build the base image.")
    & docker build -t esd-java/esd-build -f .\Dockerfile.build .

    Write-Host("Build JDK image.")
    & docker build -t esd-java/esd-jdk -f .\Dockerfile.jdk .
}

Write-Host("Build the init image.")
& docker build -t esd-java/esd-initial -f .\Dockerfile.init .

Write-Host("Build the server image.")
& docker build -t esd-java/esd-server -f .\Dockerfile.server .

Write-Host("Build the auth image.")
& docker build -t esd-java/esd-auth -f .\Dockerfile.auth .

Write-Host("Build the client image.")
& docker build -t esd-java/esd-client -f .\Dockerfile.client .

if('-b' -eq $BUILD){
    Write-Host("Remove <none> images.")
    & docker image prune --filter="dangling=true"
}

Write-Host 'Build Packages'
& mvn clean package -DskipTests

Write-Host 'Put Files'
Copy-Item .\esd-server\target\esd-server-latest.jar	-Destination .\esd-deploy\gembook\gembook-services\server\esd-server-latest.jar
Copy-Item .\esd-auth\target\esd-auth-latest.jar		-Destination .\esd-deploy\gembook\gembook-services\auth\esd-auth-latest.jar
Copy-Item .\esd-client\target\esd-client-latest.jar	-Destination .\esd-deploy\gembook\gembook-services\client\esd-client-latest.jar

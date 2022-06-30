$PWD = (Get-Location).Path;

& docker run -d `
    -p 57000:57000 `
    -v "$PWD\esd-deploy\gembook\auth\application.yml:/app/application.yml" `
    -v "$PWD\esd-deploy\gembook\auth\application-gembook.yml:/app/application-gembook.yml" `
    --name esd-auth `
    esd-java/esd-auth

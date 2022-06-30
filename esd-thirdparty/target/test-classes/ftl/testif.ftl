<html>
    <body>
        <h3>${testIf.siloUser?default('your name')}</h3>
        <h3>${testIf.siloUser!'your name'}</h3>
        <h3>${testIf.siloUser!123}</h3>
        <h3>${testIf.siloUser!default}</h3>
        <h3><#if testIf.siloUser??>${testIf.siloUser.name}<#else>when-missing</#if></h3>
    </body>
</html>
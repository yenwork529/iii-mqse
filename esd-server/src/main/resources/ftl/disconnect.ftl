<html>
<body>
<h3>以下設備已斷線，請盡快處理。</h3>
<#if list??>
    <ul>
        <#list list as device >
            <li>設備ID：${device.id}
                設備名稱：${device.name!device.id}<#if device.fieldProfile??> 所屬場域：${device.fieldProfile.name}(${device.fieldProfile.id})</#if>。
            </li>
        </#list>
    </ul>
</#if>
</body>
</html>
<html>
<style>
    table {
        border-spacing: 1;
        border-collapse: collapse;
        background: white;
        width: 450px;
        margin: 0 auto;
    }

    table td, table th {
        padding-left: 8px;
        text-align: left;
    }

    table thead tr {
        height: 35px;
        background: #36304a;
    }

    table thead th {
        font-size: 18px;
        color: #fff;
    }

    table tbody tr {
        height: 30px;
        font-size: 16px;
        color: #888888;
    }

    table tbody tr.odd {
        background-color: #e3d5d5;
    }
</style>
<body>
<h3>系統管理員您好：</h3>
<h3>調度系統${date?string("yyyy-MM-dd")}各場域每分鐘用電資料如下。</h3>
<table>
    <thead>
    <tr>
        <th>場域名稱</th>
        <th>補值</th>
        <th>數量</th>
    </tr>
    </thead>
    <#if list??>
        <tbody>
        <#list list as data>
            <tr <#if data_index%2==1>class="odd"</#if>>
                <td>${data.fieldId.name}(${data.fieldId.id})</td>
                <td>${data.needFix?c}</td>
                <td>${data.count}</td>
            </tr>
        </#list>
        </tbody>
    </#if>
</table>
<br/>
<table>
    <thead>
    <tr>
        <th>場域名稱</th>
        <th>時間</th>
    </tr>
    </thead>
    <#if needFixlist??>
        <tbody>
        <#list needFixlist as data>
            <tr <#if data_index%2==1>class="odd"</#if>>
                <td>${data.fieldProfile.name}(${data.fieldProfile.id})</td>
                <td>${data.time?string("yyyy-MM-dd HH:mm:ss")}</td>
            </tr>
        </#list>
        </tbody>
    </#if>
</table>
</body>
</html>
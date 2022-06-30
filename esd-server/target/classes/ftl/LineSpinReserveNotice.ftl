台電已於${noticeTime?string("yyyy-MM-dd HH:mm:ss")}下達即時備轉調度通知，
請配合在${responseMin}分鐘內(${startTime?string("HH:mm:ss")})執行場域降載作業，降載期間需持續${duration}分鐘。
總降載量：${clipKW} kW (降載到${target} kW)。
<#if fields??>
    <#if fields?size == 1>貴<#else>各</#if>場域分配額度：
    <#list fields as field >
        ▪️[${field.name}]降載量：${field.unload}kW，降載到${field.target}kW。
    </#list>
</#if>
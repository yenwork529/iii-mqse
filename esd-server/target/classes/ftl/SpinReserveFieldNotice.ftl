<html>
<body>
<h3>台電已於${noticeTime?string("yyyy-MM-dd HH:mm:ss")}下達即時備轉調度通知，</h3>
<h3>請配合在${responseMin}分鐘內(${startTime?string("HH:mm:ss")})執行場域降載作業，降載期間需持續${duration}分鐘。</h3>
<h3>本次降載量：${clipKW} kW (降載到${target} kW)。</h3>
<h3>貴場域分配額度：</h3>
<ul>
    <li>[${field.name}]降載量：<b>${field.unload}</b> kW，降載到<b>${field.target}</b> kW。</li>
</ul>
</body>
</html>
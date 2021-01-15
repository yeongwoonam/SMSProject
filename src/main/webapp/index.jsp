<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    request.setCharacterEncoding(StandardCharsets.UTF_8.name());
    String requestValue = request.getParameter("_");
    if (requestValue != null && requestValue.equals("bocare-sms-dev")) {


%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>send message demo</title>
</head>
<body>
<h1>send LMS form</h1>
<form method="post" action="sendLMS">
    from : <input type="text" name="from"><br>
    to : <input type="text" name="to"><br>
    받는사람 이름(toName) : <input type="text" name="toName"><br>
    time : <input id="time" type="datetime-local" name="time">format : yyyy-MM-dd'T'hh:mm ex)2020-11-12T09:52<br>
    groupId :<input type="text" name="groupId"><br>
    <label>
        text :
        <textarea name="text"></textarea>
    </label><br>
    <input type="submit"> <input type="reset">
</form>
<h1>send form</h1>
<form method="post" action="sendMessage">
    from : <input type="text" name="from"><br>
    to : <input type="text" name="to"><br>
    받는사람 이름(toName) : <input type="text" name="toName"><br>
    time : <input id="time" type="datetime-local" name="time">format : yyyy-MM-dd'T'hh:mm ex)2020-11-12T09:52<br>
    groupId :<input type="text" name="groupId"><br>
    <label>
        text :
        <textarea name="text"></textarea>
    </label><br>
    <input type="submit"> <input type="reset">
</form>
<h1>get log form</h1>
<form method="post" action="getMessageLog">
    from : <input type="text" name="from"><br>
    startDate : <input type="text" name="startDate" placeholder="yyyy-mm-dd"><br>
    endDate : <input type="text" name="endDate" placeholder="yyyy-mm-dd"><br>
    trPhone : <input type="text" name="searchTarget" placeholder="받는 사람 번호(없으면 전체 검색)"><br>
    <input type="submit"> <input type="reset"><br>
</form>
</body>
<h1>get reserved message form</h1>
<form method="post" action="getReservedMessage">
    from : <input type="text" name="from"><br>
    startDate : <input type="text" name="startDate" placeholder="yyyy-mm-dd"><br>
    endDate : <input type="text" name="endDate" placeholder="yyyy-mm-dd"><br>
    trPhone : <input type="text" name="searchTarget" placeholder="받는 사람 번호(없으면 전체 검색)"><br>
    <input type="submit"> <input type="reset"><br>
</form>
<h1>delete reserved message form</h1>
<form method="post" action="deleteReservedMessage">
    trEtc1 : <input type="text" name="trEtc1"><br>
    <input type="submit"> <input type="reset"><br>
</form>
<h1>get Message History Detail By Group</h1>
<form method="post" action="getMessageHistoryDetailGroup">
    trEtc1 : <input type="text" name="trEtc1"><br>
    <input type="submit"> <input type="reset">
</form>
<script type="text/javascript">
    const now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    document.getElementById('time').value = now.toISOString().slice(0, 16);
</script>
</html>
<%
    } else {
        response.setStatus(404);
    }
%>
<%@ include file="/include.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:useBean id="queueState" scope="request" type="jetbrains.buildServer.queueManager.settings.QueueState"/>
<c:set var="visible" value="${not queueState.queueEnabled}"/>

<c:choose>
  <c:when test="${visible}">
    <c:set var="message" value="<%=queueState.describe()%>"/>

    <style type="text/css">
      .queueStateMessage {
        background: #ff85a5;
        margin-bottom: 5px;
        padding: 5px 25px;
        border: 1px solid darkorange;
        color: #222;
      }
    </style>

    <div class="queueStateMessage"><c:out value="${message}" escapeXml="false"/></div>
  </c:when>
</c:choose>
<%--<authz:authorize allPermissions="VIEW_SERVER_ERRORS">--%>

<%--</authz:authorize>--%>

  
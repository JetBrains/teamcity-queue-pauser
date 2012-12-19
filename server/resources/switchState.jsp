<%@ include file="/include.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:useBean id="queueManagerSettings" type="jetbrains.buildServer.queueManager.settings.QueueManagerSettings" scope="request"/>
<c:url var="actionUrl" value="/queueManager.html"/>

<c:choose>
  <c:when test="${queueManagerSettings.queueEnabled}">
    <c:set var="switchQueueStateActionText" value="Pause Build Queue"/>
  </c:when>
  <c:otherwise>
    <c:set var="switchQueueStateActionText" value="Resume"/>
  </c:otherwise>
</c:choose>
<authz:authorize allPermissions="DISABLE_AGENT">
  <form action="${actionUrl}" id="switchQueueStateForm" method="post" class="clearfix" style="display: inline  ">
    <input class="btn btn_mini submitButton" id="switchQueueState" type="submit" value="${switchQueueStateActionText}"/>
    <input type="hidden" name="newQueueState" value="${not queueManagerSettings.queueEnabled}"/>
  </form>
</authz:authorize>
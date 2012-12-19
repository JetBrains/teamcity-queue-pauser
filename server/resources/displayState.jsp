<%@ page import="jetbrains.buildServer.queueManager.PluginConstants" %>
<%@ include file="/include.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<jsp:useBean id="queueState" scope="request" type="java.lang.Boolean"/>
<c:set var="PARAM_NEW_QUEUE_STATE" value="<%=PluginConstants.WEB.PARAM_NEW_QUEUE_STATE%>"/>

<c:choose>
  <c:when test="${queueState}">
    <c:set var="switchQueueStateActionText" value="Disable Build Queue"/>
  </c:when>
  <c:otherwise>
    <c:set var="switchQueueStateActionText" value="Enable Build Queue"/>
  </c:otherwise>
</c:choose>

<c:set var="actionParams" value="${PARAM_NEW_QUEUE_STATE}=${not queueState}"/>
<c:url var="actionUrl" value="/queueManager.html?${actionParams}"/>

<script type="text/javascript">
  $j(document).ready(function() {
    $j('.quickLinks').append('<a class="quickLinksItem" href="${actionUrl}">${switchQueueStateActionText}</a>');
  });
</script>


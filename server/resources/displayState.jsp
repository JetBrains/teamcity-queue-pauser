<%@ include file="/include.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<jsp:useBean id="queueManagerSettings" type="jetbrains.buildServer.queueManager.settings.QueueManagerSettings" scope="request"/>


<c:choose>
  <c:when test="${queueManagerSettings.queueEnabled}">
    <c:set var="switchQueueStateActionText" value="Pause Build Queue"/>
    <jsp:include page="switchState.jsp"/>
  </c:when>
  <c:otherwise>
    <c:set var="buildQueuePauseActionText" value="Resume"/>
    <div class="successMessage">
      Build Queue was paused $by user$ <%--<bs:_commentUserInfo user="${queueManagerSettings.user}"/>--%>
      <jsp:include page="switchState.jsp"/>
    </div>
  </c:otherwise>
</c:choose>


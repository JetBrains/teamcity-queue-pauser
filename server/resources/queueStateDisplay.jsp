<%@ include file="/include.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:useBean id="queueState" scope="request" type="jetbrains.buildServer.queueManager.settings.QueueState"/>
<c:set var="visible" value="${not queueState.queueEnabled}"/>
<c:set var="user" value="${queueState.user}"/>

<c:choose>
  <c:when test="${visible}">
    <c:set var="action">
      Queue was ${queueState.queueEnabled ? "enabled" : "disabled"}
    </c:set>
    <c:set var="actor">
      <c:choose>
        <c:when test="${user != null}">
          &nbsp;by <strong><c:out value="${user.descriptiveName}"/></strong>
        </c:when>
        <c:otherwise/>
      </c:choose>
    </c:set>
    <c:set var="date">
      &nbsp;<bs:date value="${queueState.timestamp}" smart="true"/>
    </c:set>
    <c:set var="reason">
      <c:choose>
        <c:when test="${not empty queueState.reason}">
          &nbsp;with comment: <c:out value="${queueState.reason}"/>
        </c:when>
        <c:otherwise> </c:otherwise>
      </c:choose>
    </c:set>
    <div class="attentionComment">${action}${actor}${date}${reason}</div>
  </c:when>
</c:choose>

  
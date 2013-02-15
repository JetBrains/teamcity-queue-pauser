<%@ include file="/include.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:useBean id="queueState" scope="request" type="jetbrains.buildServer.queueManager.settings.QueueState"/>
<jsp:useBean id="canManage" scope="request" type="java.lang.Boolean"/>
<c:set var="visible" value="${not queueState.queueEnabled}"/>
<c:set var="user" value="${queueState.user}"/>
<c:set var="canManage" value="${canManage}"/>

<c:choose>
  <c:when test="${visible}">
    <c:set var="action">
      Build queue was paused
    </c:set>
    <c:set var="actor">
      <c:choose>
        <c:when test="${user != null}">
          &nbsp;by <strong><c:out value="${user.descriptiveName}"/></strong>&nbsp;
        </c:when>
        <c:otherwise/>
      </c:choose>
    </c:set>
    <c:set var="date">
      <bs:date value="${queueState.timestamp}" smart="true"/>
    </c:set>
    <c:set var="reason">
      <c:choose>
        <c:when test="${not empty queueState.reason}">
          &nbsp;with comment: <c:out value="${queueState.reason}"/>
        </c:when>
        <c:otherwise/>
      </c:choose>
    </c:set>
    <div class="attentionComment">
        ${action}${actor}${date}${reason}. No builds will be started until queue is activated.
      <c:if test="${canManage}">
        <div style="float:right">
          <a class="btn btn_mini" href="#" onclick="BS.QueueStateActions.resumeQueue(); return false">Resume</a>
        </div>
      </c:if>
    </div>
  </c:when>
</c:choose>


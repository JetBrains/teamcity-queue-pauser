<%@ page import="jetbrains.buildServer.serverSide.auth.AuthorityHolder" %>
<%@ page import="jetbrains.buildServer.web.util.SessionUser" %>
<%@ page import="jetbrains.buildServer.users.SUser" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/include-internal.jsp" %>
<jsp:useBean id="healthStatusItem" type="jetbrains.buildServer.serverSide.healthStatus.HealthStatusItem" scope="request"/>
<%--@elvariable id="queueState" type="jetbrains.buildServer.queueManager.settings.QueueState"--%>
<%--@elvariable id="allowManualResume" type="java.lang.Boolean"--%>
<c:set var="queueState" value="${healthStatusItem.additionalData['QUEUE_STATE']}"/>
<c:set var="user" value="${queueState.user}"/>
<c:set var="allowManualResume" value="${healthStatusItem.additionalData['allowManualResume']}"/>
<c:set var="action">
  Build queue was paused
</c:set>
<c:set var="actor">
  <c:choose>
    <c:when test="${not empty user}">
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
    <c:otherwise/>
  </c:choose>
</c:set>
<div>
  ${action}${actor}${date}${reason} No builds will be started until queue is resumed.

  <c:set var="allowResume">
    <%
      final AuthorityHolder user = SessionUser.getUser(request);
      if (user != null && ((SUser)user).isSystemAdministratorRoleGranted()) {
    %>
      true
    <%
    } else {
    %>
    <authz:authorize allPermissions="ENABLE_DISABLE_AGENT">
          <jsp:attribute name="ifAccessGranted">
            ${allowManualResume}
          </jsp:attribute>
    </authz:authorize>
    <%
      }
    %>
  </c:set>

  <c:if test="${allowResume}">
    <div style="float:right">
      <a class="btn btn_mini" href="#" onclick="BS.QueueStateActions.resumeQueue(); return false">Resume</a>
    </div>
  </c:if>
</div>



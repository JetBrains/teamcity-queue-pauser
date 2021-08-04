<%@ page import="java.util.Map" %>
<%@ page import="jetbrains.buildServer.queueManager.server.MessageViewer" %>
<%@ page import="jetbrains.buildServer.queueManager.settings.QueueState" %>
<%@ page import="jetbrains.buildServer.users.SUser" %>
<%@ page import="jetbrains.buildServer.web.util.SessionUser" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/include-internal.jsp" %>
<%--
  ~ Copyright 2000-2021 JetBrains s.r.o.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<jsp:useBean id="healthStatusItem" type="jetbrains.buildServer.serverSide.healthStatus.HealthStatusItem" scope="request"/>
<%--@elvariable id="queueState" type="jetbrains.buildServer.queueManager.settings.QueueState"--%>
<%--@elvariable id="allowManualResume" type="java.lang.Boolean"--%>
<%--@elvariable id="serverAllowsResuming" type="java.lang.Boolean"--%>
<c:set var="queueState" value="${healthStatusItem.additionalData['QUEUE_STATE']}"/>
<c:set var="user" value="${queueState.user}"/>
<c:set var="allowManualResume" value="${healthStatusItem.additionalData['allowManualResume']}"/>
<c:set var="serverAllowsResuming" value="${healthStatusItem.additionalData['serverAllowsResuming']}"/>
<%
  final SUser currentUser = SessionUser.getUser(request);
  final Map<String, Object> additionalData = healthStatusItem.getAdditionalData();
%>

<c:set var="action">
  The build queue was paused
</c:set>
<c:set var="user">
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
      with the comment: <%=MessageViewer.viewMessage(currentUser, (QueueState)additionalData.get("QUEUE_STATE"))%>
    </c:when>
    <c:otherwise/>
  </c:choose>
</c:set>

${action}${user}${date}<c:if test="${not empty reason}">&nbsp;<bs:out value="${reason}"/></c:if><br/>
No builds will be started until the queue is resumed.<bs:help file="Build+Queue" anchor="Pausing%2FResumingBuildQueue"/>

<c:if test="${serverAllowsResuming}">
  <c:set var="allowResume">
    <%
      if (currentUser != null && currentUser.isSystemAdministratorRoleGranted()) {
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
    <script>
      BS.QueueStateActions = {
        url: window['base_uri'] + "/queueStartStop.html",
        resumeQueue: function() {
          var params = {
            'newQueueState': 'true',
            'stateChangeReason' : ''
          };
          //noinspection JSUnusedGlobalSymbols
          BS.ajaxRequest(this.url, {
            parameters: params,
            onSuccess: function() {
              window.location.reload();
            }
          });
        }
      };
    </script>
    <div style="float:right">
      <a class="btn btn_mini" href="#" onclick="BS.QueueStateActions.resumeQueue(); return false">Resume</a>
    </div>
  </c:if>
</c:if>




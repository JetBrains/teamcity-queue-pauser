<%@ page import="jetbrains.buildServer.queueManager.PluginConstants" %>
<%@ include file="/include.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%--
  ~ Copyright 2000-2012 JetBrains s.r.o.
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


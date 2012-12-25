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
<%@ include file="/include.jsp" %>
<%@ page import="jetbrains.buildServer.queueManager.PluginConstants" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<c:set var="PARAM_NEW_QUEUE_STATE" value="<%=PluginConstants.WEB.PARAM_NEW_QUEUE_STATE%>"/>
<c:set var="PARAM_STATE_CHANGE_REASON" value="<%=PluginConstants.WEB.PARAM_STATE_CHANGE_REASON%>"/>
<c:set var="QUEUE_MANAGER_URL" value="<%=PluginConstants.WEB.QUEUE_MANAGER_URL%>"/>

<jsp:useBean id="queueState" scope="request" type="jetbrains.buildServer.queueManager.settings.QueueState"/>
<c:set var="queueEnabled" value="${queueState.queueEnabled}"/>

<c:choose>
  <c:when test="${queueEnabled}">
    <c:set var="switchQueueStateActionText" value="Disable Build Queue"/>
  </c:when>
  <c:otherwise>
    <c:set var="switchQueueStateActionText" value="Enable Build Queue"/>
  </c:otherwise>
</c:choose>

<c:url var="actionUrl" value="${QUEUE_MANAGER_URL}"/>

<script type="text/javascript">
  BS.ChangeQueueStateDialog = OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
    formElement: function() {
      return $('changeQueueStateForm');
    },

    getContainer: function() {
      return $('changeQueueStateFormDialog');
    },

    showDialog: function() {
      this.formElement().${PARAM_STATE_CHANGE_REASON}.value = this.formElement().${PARAM_STATE_CHANGE_REASON}.defaultValue;
      this.showCentered();
      this.formElement().${PARAM_STATE_CHANGE_REASON}.focus();
      this.formElement().${PARAM_STATE_CHANGE_REASON}.select();

      var dialogTitle;
      var dialogSubmitButtonTitle;
      <c:choose>
      <c:when test="${queueEnabled}">
      dialogTitle = "Disable build queue";
      dialogSubmitButtonTitle = "Disable";
      </c:when>
      <c:otherwise>
      dialogTitle = "Enable build queue";
      dialogSubmitButtonTitle = "Enable";
      </c:otherwise>
      </c:choose>

      $j("#ChangeQueueStateSubmitButton").prop('value', dialogSubmitButtonTitle);
      $j("#changeQueueStateFormTitle").html(dialogTitle);

      this.bindCtrlEnterHandler(this.submit.bind(this));

      return false;
    },

    submit: function() {
      BS.FormSaver.save(BS.ChangeQueueStateDialog, BS.ChangeQueueStateDialog.formElement().action, OO.extend(BS.SimpleListener, {
        onCompleteSave: function(form, responseXML, err) {
          BS.reload(true);
        }
        // todo: onFailure or onException
      }));
      return false;
    }
  }));

  <%----------------------------------------------------------%>
  $j(document).ready(function() {
    $j('.quickLinks').append('<a href="#" class="quickLinksItem" onclick="BS.ChangeQueueStateDialog.showDialog();">${switchQueueStateActionText}</a>');
  });
  <%--------------%>
</script>


<bs:modalDialog formId="changeQueueStateForm"
                title="Disable queue"
                action="${actionUrl}"
                closeCommand="BS.ChangeQueueStateDialog.close();"
                saveCommand="BS.ChangeQueueStateDialog.submit()">
  <label for="${PARAM_STATE_CHANGE_REASON}">Reason:</label>
  <textarea id="${PARAM_STATE_CHANGE_REASON}"
            name="${PARAM_STATE_CHANGE_REASON}"
            rows="3" cols="46" class="commentTextArea"
            onkeyup="if (this.value.length > 140) this.value = this.value.substring(0, 140)"
            onfocus="if (this.value == this.defaultValue) this.value = ''"
            onblur="if (this.value == '') this.value='&lt;your comment here&gt;'">&lt;your comment here&gt;</textarea>
  <input type="hidden" name="${PARAM_NEW_QUEUE_STATE}" value="${not queueEnabled}">
  <div class="popupSaveButtonsBlock">
    <forms:cancel onclick="BS.ChangeQueueStateDialog.close()"/>
    <forms:submit  label="Save" id="ChangeQueueStateSubmitButton"/>
    <forms:saving/>
  </div>
</bs:modalDialog>


/**
 * Contains action for quick queue resume
 */
BS.QueueStateActions = {
  url: window['base_uri'] + "/queuePauser.html",
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
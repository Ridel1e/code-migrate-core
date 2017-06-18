/* dependencies */
const WebSocket = require('ws');
const CommandValidationService = 
  require('./services').CommandValidationService;
const Router = 
  require('./app.router').Router;


/* server creation */ 
const wsServer = new WebSocket.Server({ port: 8080 });

/* help functions */
const createResponse = function (response, type) {
  const completedReponse = Object.assign(response, {
    type: type
  });

  return JSON.stringify(completedReponse);
}

const createSuccessResponse = function (response) {
  return createResponse(response, 'success');
};

const createErrorResponse = function (response) {
  return createResponse(response, 'error');
}

/* handlers */
wsServer.on('connection', function (wsClient) {
  console.log('new client connected');

  wsClient.on('message', function (message) {
    const command = JSON.parse(message);
    
    if(!CommandValidationService.isCommand(command)) {
      const error = {
        status: 400,
        message: 'Command has incorrectFormat'
      };
      wsClient.send(createErrorResponse(error));
      return;
    }

    Router
      .callCommand(command)
      .then(function (result) {
        wsClient.send(createSuccessResponse(result));
      })
      .catch(function (error) {
        wsClient.send(createErrorResponse(error))
      });
  })
})

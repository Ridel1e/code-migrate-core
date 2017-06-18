/* test sender */
const WebSocket = require('ws');
 
const ws = new WebSocket('ws://localhost:8080');
 
ws.on('open', function open() {
  const cmd = {
    cmd: 'install',
  };

  ws.send(JSON.stringify(cmd));
});

ws.on('message', function (message) {
  console.log(message);
})

const commands = require('./commands');

/* command list */
const commandList = {
  'install': commands.installCommand
};

function callCommand (command, response) {
  const commandFunction = commandList[command.cmd];
  const data = command.data;

  const isFunction = commandFunction instanceof Function;

  if (!isFunction) {
    const commandNotFoundError = {
      status: '404',
      message: 'command not found'
    };
    return Promise.reject(commandNotFoundError);
  }

  return commandFunction(data);
}

/*  */
const Router = {
  callCommand: callCommand
};

/*  exporting */
module.exports.Router = Router

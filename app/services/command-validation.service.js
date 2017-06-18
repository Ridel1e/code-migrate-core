/**
 * Returns true if object has cmd field 
 * @public 
 * @param {Object} command command object
 * @returns {boolean}
 */
function isCommand (command) {
  return command.cmd !== undefined;
}

const CommandValidationService = {
  isCommand: isCommand
};

module.exports.CommandValidationService = CommandValidationService;

const CommandValidationService = 
  require('./command-validation.service').CommandValidationService;
const ModulesApiFacade = 
  require('./modules-api.facade').ModulesApiFacade;
const ZipService =
  require('./zip-service.js').ZipService;

module.exports.CommandValidationService = CommandValidationService;
module.exports.ModulesApiFacade = ModulesApiFacade;
module.exports.ZipService = ZipService;

const ModulesApiFacade = require('../services').ModulesApiFacade;
const ZipService = require('../services').ZipService;
const Path = require('path');

const MODULES_PATH = Path.resolve('./modules');
const argumentTypeMismatchError = {
  status: '400',
  message: 'installed: argumentType missmatch array or undefined expected'  
};

/**
 * 
 * @param {*} data 
 */
function installCommand (data) {
  const isArray = data instanceof Array;

  if(!isArray && data !== undefined) {
    return Promise.reject(argumentTypeMismatchError);
  }
  
  return ModulesApiFacade
    .fetchAllModules()
    .then(function (modules) {
      const modulesPromises = modules.map(installModule)

      return Promise.all(modulesPromises);
    })
    .then(function (){
      return 'success';
    })
    
}

/**
 * 
 * @param {*} module 
 */
function installModule (module) {
  return ModulesApiFacade
    .downloadModule(module.name, module.url)
    .then(function (filePath) {
      const resolvedFilePath = Path.resolve(filePath);
      const targetPath = MODULES_PATH + '/' + module.name;

      return ZipService.unzipFile(resolvedFilePath, targetPath)
    });
}

module.exports.installCommand = installCommand;

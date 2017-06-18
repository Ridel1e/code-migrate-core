const rp = require('request-promise');
const URI = 'http://localhost:3000/modules';

/* trash */
const http = require('http');
const fs = require('fs');


const fetchAllModules = function () {
  const options = {
    method: 'GET',
    uri: URI,
    json: true
  };

  return rp(options);
}

/* trash code. NEED TO REWRITE */
const downloadModule = function (name, url) {
  return new Promise (function (resolve, reject) {
      const path = './tmp/' + name + '.zip';
      var file = fs.createWriteStream(path);
      var request = http.get(url, function(response) {
        response.pipe(file);
        file.on('finish', function() {
          resolve(path);
        });
      }).on('error', function(err) { // Handle errors
        fs.unlink(dest); // Delete the file async. (But we don't check the result)
        reject(err);
      });
  })
}

const ModulesApiFacade = {
  fetchAllModules,
  downloadModule
};

module.exports.ModulesApiFacade = ModulesApiFacade;

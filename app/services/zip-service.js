var Zip = require('adm-zip');
var fs = require('fs');


/**
 * 
 * @param {*} path 
 */
function unzipFile (filePath, targetPath) {
  return createDir(targetPath)
    .then(function () {
      const zip = new Zip(filePath);

      zip.extractAllTo(targetPath, true);
    })
}

function createDir (folderPath) {
  return new Promise (function (resolve, reject) {
    fs.mkdir(folderPath, function (err, result) {
      if (err) { reject(err) }
      resolve(folderPath);
    })
  })
}

const ZipService = {
  unzipFile: unzipFile,
  createDir: createDir
};

module.exports.ZipService = ZipService;

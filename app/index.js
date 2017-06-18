var Zip = require('adm-zip');
var fs = require('fs');

const ZIP_FOLDER_PATH = __dirname + '/data';
const UNZIP_FOLDER_PATH = __dirname + '/results';

const unzipAllFiles = function (files) {
  files.forEach(function (file) {
    if (isZipFile(file)) { 
      const filePath = ZIP_FOLDER_PATH + '/' + file;

      createDirByZipFileName(file).then(function (targetPath) {
        unzipFile(filePath, targetPath);    
      });
    }
  })
}

readDir(ZIP_FOLDER_PATH)
  .then(unzipAllFiles);


/**
 * 
 * @param {*} path 
 */
function unzipFile (path, targetPath) {
  const zip = new Zip(path);

  zip.extractAllTo(targetPath, true);
}



function isZipFile (fileName) {
  const ZIP_FORMAT = 'zip';

  const splitedFileName = fileName.split('.');
  const fileFormat = splitedFileName[splitedFileName.length - 1];

  return fileFormat === ZIP_FORMAT;
}

/**
 * Read all dirs
 * @param {*} dirPath 
 */
function readDir (dirPath) {
  return new Promise (function (resolve, reject) {
    fs.readdir(dirPath, function (err, paths) {
      if (err) { reject(err); }

      resolve(paths);
    });
  })
}

function createDirByZipFileName (fileName) {
  const splitedFileName = fileName.split('.');

  const folderName = splitedFileName
    .slice(0, splitedFileName.length - 1)
    .join('');
  const folderPath = UNZIP_FOLDER_PATH + '/' + folderName;  


  return new Promise (function (resolve, reject) {
    fs.mkdir(folderPath, function (err, result) {
      if (err) { reject(err) }
      resolve(folderPath);
    })
  })
}

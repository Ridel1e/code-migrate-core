var program = require('commander');

program
  .version('0.0.1')
  .option('-p, --peppers', 'Add peppers')
  .parse(process.argv);

console.log(program.peppers);

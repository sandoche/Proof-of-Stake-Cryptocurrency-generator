/**
 * Proof of Stake Cryptocurrency generator
 * Author: Sandoche ADITTANE
 */

'use strict';

const inquirer = require('inquirer');
const cmd = require('node-cmd');
const Promise = require('bluebird');
const replace = require('replace-in-file');


console.log('*************************************************************');
console.log('Welcome to Nxt clone Proof of Stake Cryptocurrency generator');
console.log('*************************************************************');
console.log(' ')

var questions = [
  {
    type: 'input',
    name: 'application',
    message: 'Name of the Blockchain (example: SuperCoin)',
    validate: function(value) {
      var pass = value.match(
        /^\S[A-Za-z]*$/g
      );
      if (pass) {
        return true;
      }
      return 'Please enter a name without space or numbers';
    }
  },
  {
    type: 'input',
    name: 'coin_symbol',
    message: 'Symbol of the coin (example: SPC)',
    validate: function(value) {
      var pass = value.match(
        /^[A-Z]{3,5}$/g
      );
      if (pass) {
        return true;
      }
      return 'Please enter between 3 and 5 capital letters';
    }
  },
  {
    type: 'input',
    name: 'default_peer_port',
    message: 'Peer port (example: 6874)',
    validate: function(value) {
      if(value >= 1000 && value <= 65535) {
        return true;
      }
      return 'Please enter a number between 1000 and 65535';
    }
  },
  {
    type: 'input',
    name: 'testnet_peer_port',
    message: 'Testnet peer port (example: 5874)',
    validate: function(value) {
      if(value >= 1000 && value <= 65535) {
        return true;
      }
      return 'Please enter a number between 1000 and 65535';
    }
  },
  {
    type: 'input',
    name: 'api_server_port',
    message: 'API server port (example: 6876)',
    validate: function(value) {
      if(value >= 1000 && value <= 65535) {
        return true;
      }
      return 'Please enter a number between 1000 and 65535';
    }
  },
  {
    type: 'input',
    name: 'website',
    message: 'Website of the project (or a github)',
  },
  {
    type: 'list',
    name: 'source',
    message: 'Version of NXT Clone Starter',
    choices: ['v1.1.13', 'latest (may not be compatible with the generator)']
  }
];

inquirer.prompt(questions).then(answers => {
  const folderName = answers.application;
  const appName = answers.application;
  const repositoryOfficial = 'https://bitbucket.org/Jelurida/nxt-clone-starter';
  const repositorySandoche = 'https://github.com/sandoche/nxt-clone-starter';
  const source = answers.source === 'v1.1.13' ? repositorySandoche : repositoryOfficial;

  console.log('1. Cloning the nxt-clone-starter')
  const getAsync = Promise.promisify(cmd.get, { multiArgs: true, context: cmd });
  getAsync('git clone ' + source + ' ' + answers.application).then(data => {
    console.log('Repository cloned successfully');

    console.log('');
    console.log('2. Setting up the parameters')
    try {
      const changes1 = replace.sync({
        files: folderName + '/src/java/nxt/Nxt.java',
        from: 'APPLICATION = "NxtClone"',
        to: 'APPLICATION = "' + answers.application + '"'
      });
      const changes2 = replace.sync({
        files: folderName + '/src/java/nxt/Constants.java',
        from: 'COIN_SYMBOL = "NxtCloneCoin"',
        to: 'COIN_SYMBOL = "' + answers.coin_symbol +'"'
      });
      const changes3 = replace.sync({
        files: folderName + '/src/java/nxt/Constants.java',
        from: 'ACCOUNT_PREFIX = "NXT"',
        to: 'ACCOUNT_PREFIX = "' + answers.coin_symbol +'"'
      });
      const changes4 = replace.sync({
        files: folderName + '/src/java/nxt/Constants.java',
        from: 'PROJECT_NAME = "NxtClone"',
        to: 'PROJECT_NAME  = "' + answers.application +'"'
      });
      const changes5 = replace.sync({
        files: folderName + '/src/java/nxt/peer/Peers.java',
        from: 'DEFAULT_PEER_PORT = 47874',
        to: 'DEFAULT_PEER_PORT = ' + answers.default_peer_port
      });
      const changes6 = replace.sync({
        files: folderName + '/src/java/nxt/peer/Peers.java',
        from: 'TESTNET_PEER_PORT = 46874',
        to: 'TESTNET_PEER_PORT = ' + answers.testnet_peer_port
      });
      const changes7 = replace.sync({
        files: folderName + '/contrib/Dockerfile',
        from: '7876',
        to: answers.api_server_port
      });
      const changes7b = replace.sync({
        files: folderName + '/contrib/Dockerfile',
        from: '7874',
        to: answers.default_peer_port
      });
      const changes8 = replace.sync({
        files: folderName + '/Wallet.url',
        from: '7876',
        to: answers.api_server_port
      });
      const changes9 = replace.sync({
        files: folderName + '/conf/nxt-default.properties',
        from: '47874',
        to: answers.default_peer_port
      });
      const changes10 = replace.sync({
        files: folderName + '/conf/nxt-default.properties',
        from: '7876',
        to: answers.api_server_port
      });
      console.log('Modified files:', changes1.join(', '));
      console.log('Modified files:', changes2.join(', '));
      console.log('Modified files:', changes3.join(', '));
      console.log('Modified files:', changes4.join(', '));
      console.log('Modified files:', changes5.join(', '));
      console.log('Modified files:', changes6.join(', '));
      console.log('Modified files:', changes7.join(', '));
      console.log('Modified files:', changes7b.join(', '));
      console.log('Modified files:', changes8.join(', '));
      console.log('Modified files:', changes8.join(', '));
      console.log('Modified files:', changes9.join(', '));
      console.log('Modified files:', changes10.join(', '));

      console.log('');
      console.log('3. Copying assets, and genesis files');
      getAsync('rm -rf ' + folderName + '/conf/data && cp -R  templates/conf/data ' + folderName + '/conf/').then(data => {
        console.log('Genesis files copied');
      }).catch(error => {
        console.log('An error occured', error)
      })

      getAsync('rm -rf ' + folderName + '/html/www/img && cp -R  templates/img ' + folderName + '/html/www/').then(data => {
        console.log('Images files copied');
      }).catch(error => {
        console.log('An error occured', error)
      })

      getAsync('cp -R  templates/favicon.ico ' + folderName + '/html/www/').then(data => {
        console.log('Favicon copied');
      }).catch(error => {
        console.log('An error occured', error)
      })

      console.log('');
      console.log('4. Compiling, renaming complation files');

  		const changes11 = replace.sync({
  		  files: folderName + '/*.sh',
  		  from: 'nxt-clone',
  		  to: appName
  		});
  		console.log('Modified files:', changes11.join(', '));

  		getAsync('cd ' + folderName + ' && sh ./compile.sh').then(data => {
  		    console.log('Compilation done');
          console.log('');
          console.log('5. Updating the build tools');

          const changes12 = replace.sync({
            files: 'build_tools/*.sh',
            from: /nxt.exe/g,
            to: appName + '.exe'
          });
          const changes13 = replace.sync({
            files: 'build_tools/*.sh',
            from: /nxt.jar/g,
            to: appName + '.jar'
          });
          const changes14 = replace.sync({
            files: 'build_tools/*.sh',
            from: /nxtservice.exe/g,
            to: appName + 'service.exe'
          });
          const changes15 = replace.sync({
            files: 'build_tools/*.sh',
            from: /nxtservice.jar/g,
            to: appName + 'service.jar'
          });
          const changes16 = replace.sync({
            files: 'build_tools/*.sh',
            from: /nxt-client/g,
            to: answers.coin_symbol + '-client'
          });
          const changes17 = replace.sync({
            files: 'build_tools/installer/RegistrySpec.xml',
            from: 'https://nxtforum.org/nxt-helpdesk',
            to: answers.website
          });
          const changes18 = replace.sync({
            files: 'build_tools/installer/RegistrySpec.xml',
            from: 'nxt.org',
            to: answers.website
          });
          const changes19 = replace.sync({
            files: 'build_tools/installer/setup.xml',
            from: 'NXT',
            to: appName
          });
          const changes20 = replace.sync({
            files: 'build_tools/installer/setup.xml',
            from: 'NXT',
            to: appName
          });
          const changes21 = replace.sync({
            files: 'build_tools/installer/setup.xml',
            from: 'nxt.app',
            to: appName + '.app'
          });
          const changes22 = replace.sync({
            files: 'build_tools/installer/setup.xml',
            from: /nxt.exe/g,
            to: appName + '.exe'
          });
          const changes23 = replace.sync({
            files: 'build_tools/installer/setup.xml',
            from: /nxt.jar/g,
            to: appName + '.jar'
          });
          const changes24 = replace.sync({
            files: 'build_tools/installer/setup.xml',
            from: /nxtservice.exe/g,
            to: appName + 'service.exe'
          });
          const changes25 = replace.sync({
            files: 'build_tools/installer/setup.xml',
            from: /nxtservice.jar/g,
            to: appName + 'service.jar'
          });
          const changes26 = replace.sync({
            files: 'build_tools/installer/setup.xml',
            from: 'MacOS/nxt',
            to: 'MacOS/' + answers.coin_symbol
          });
          const changes27 = replace.sync({
            files: 'build_tools/installer/shortcutSpec.xml',
            from: '7876',
            to: answers.api_server_port
          });
          const changes28 = replace.sync({
            files: 'build_tools/installer/shortcutSpec.xml',
            from: [/NXT/g, /Nxt/g, /nxt/g],
            to: appName
          });
          const changes29 = replace.sync({
            files: 'build_tools/installer/Unix_shortcutSpec.xml',
            from: /NXT/g,
            to: appName
          });
          const changes30 = replace.sync({
            files: 'build_tools/installer/Unix_shortcutSpec.xml',
            from: /Nxt/g,
            to: appName
          });
          const changes31 = replace.sync({
            files: 'build_tools/installer/setup.xml',
            from: 'https://nxt.org',
            to: answers.website
          });
          const changes32 = replace.sync({
            files: 'build_tools/mac-release-package.sh',
            from: /nxt-installer/g,
            to: answers.coin_symbol + '-installer'
          });
          const changes33 = replace.sync({
            files: 'build_tools/mac-release-package.sh',
            from: /Nxt-Installer/g,
            to: answers.coin_symbol + '-Installer'
          });
          const changes42 = replace.sync({
            files: 'build_tools/README.md',
            from: /Nxt/g,
            to: answers.coin_symbol
          });
          const changes43 = replace.sync({
            files: 'build_tools/README.md',
            from: /7876/g,
            to: answers.api_server_port
          });


          console.log('Modified files:', changes12.join(', '));
          console.log('Modified files:', changes13.join(', '));
          console.log('Modified files:', changes14.join(', '));
          console.log('Modified files:', changes15.join(', '));
          console.log('Modified files:', changes16.join(', '));
          console.log('Modified files:', changes17.join(', '));
          console.log('Modified files:', changes18.join(', '));
          console.log('Modified files:', changes18.join(', '));
          console.log('Modified files:', changes19.join(', '));
          console.log('Modified files:', changes20.join(', '));
          console.log('Modified files:', changes21.join(', '));
          console.log('Modified files:', changes22.join(', '));
          console.log('Modified files:', changes23.join(', '));
          console.log('Modified files:', changes24.join(', '));
          console.log('Modified files:', changes25.join(', '));
          console.log('Modified files:', changes26.join(', '));
          console.log('Modified files:', changes27.join(', '));
          console.log('Modified files:', changes28.join(', '));
          console.log('Modified files:', changes28.join(', '));
          console.log('Modified files:', changes29.join(', '));
          console.log('Modified files:', changes30.join(', '));
          console.log('Modified files:', changes31.join(', '));
          console.log('Modified files:', changes32.join(', '));
          console.log('Modified files:', changes33.join(', '));
          console.log('Modified files:', changes42.join(', '));
          console.log('Modified files:', changes43.join(', '));

          getAsync('cp -r  build_tools/* ' + folderName + '/').then(data => {
            console.log('Files edited and moved');
            console.log(' ')

	          console.log('6. Generating the API documentation');
            const changes34 = replace.sync({
              files: 'doc/api_examples.html',
              from: /7876/g,
              to: answers.api_server_port
            });
            const changes35 = replace.sync({
              files: 'doc/api.html',
              from: /7876/g,
              to: answers.api_server_port
            });
            const changes36 = replace.sync({
              files: 'doc/api_examples.html',
              from: /NXT /g,
              to: answers.coin_symbol + ' '
            });
            const changes37 = replace.sync({
              files: 'doc/api.html',
              from: /NXT /g,
              to: answers.coin_symbol + ' '
            });
            const changes38 = replace.sync({
              files: 'doc/api_examples.html',
              from: /NXT\-/g,
              to: answers.coin_symbol + '-'
            });
            const changes39 = replace.sync({
              files: 'doc/api_examples.html',
              from: / NXT/g,
              to: answers.coin_symbol + ' '
            });
            const changes40 = replace.sync({
              files: 'doc/api.html',
              from: / NXT/g,
              to: ' ' + answers.coin_symbol
            });
            const changes41 = replace.sync({
              files: 'doc/api.html',
              from: / Nxt/g,
              to: ' ' + answers.coin_symbol
            });

            console.log('Modified files:', changes34.join(', '));
            console.log('Modified files:', changes35.join(', '));
            console.log('Modified files:', changes36.join(', '));
            console.log('Modified files:', changes37.join(', '));
            console.log('Modified files:', changes38.join(', '));
            console.log('Modified files:', changes39.join(', '));
            console.log('Modified files:', changes40.join(', '));
            console.log('Modified files:', changes41.join(', '));
            console.log('Doc generated find it in ./doc');
            console.log(' ');

            console.log('Congratulations, your Cryptocurrency is now generated. You can now run it, with `cd ' + folderName + '` then run `sh ./compile.sh` then `sh ./run.sh` or for windows `sh ./win-compile.sh` then `run.bat`');
          }).catch(error => {
            console.log('An error occured', error)
          })

  		}).catch(error => {
  		  console.log('An error occured', error)
  		})

    }
    catch (error) {
      console.error('An error occurred:', error);
    }

  }).catch(error => {
    console.log('An error occured', error)
  })

});

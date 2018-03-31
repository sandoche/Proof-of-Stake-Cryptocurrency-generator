/**
 * Proof of Stake Cryptocurrency generator
 * Author: Sandoche ADITTANE
 */

'use strict';

const inquirer = require('inquirer');
const cmd = require('node-cmd');
const Promise = require('bluebird');
const replace = require('replace-in-file');


console.log('*****************************************************************');
console.log('Hi, welcome to Nxt clone Proof of Stake Cryptocurrency generator');
console.log('*****************************************************************');
console.log(' ')

var questions = [
  {
    type: 'input',
    name: 'application',
    message: 'What is the name of your blockchain (example: SuperCoin)',
  },
  {
    type: 'input',
    name: 'coin_symbol',
    message: 'What is the name the symbol of your coin (example: SPC)'
  },
  {
    type: 'input',
    name: 'default_peer_port',
    message: 'What is the port that you want to use for the peer node (example: 97874)',
  },
  {
    type: 'input',
    name: 'testnet_peer_port',
    message: 'What is the port that you want to use for the peer testnet node (example: 96874)',
  },
  {
    type: 'input',
    name: 'api_server_port',
    message: 'What is the port that you want to use for the api server port (example: 97876)',
  },
  {
    type: 'input',
    name: 'website',
    message: 'What the URL of the website of the Cryptocurrency (or a github is enough)',
  },
  {
    type: 'list',
    name: 'source',
    message: 'Which version of the starter do you want to clone',
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
          console.log('5. Adding the build tools, mobile app source and tests to the project');

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
            from: /NXT/i,
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

          getAsync('cp -r  build_tools/* ' + folderName + '/').then(data => {
            console.log('Files edited and moved');
            console.log(' ')
            console.log('Congratulations, your Cryptocurrency is now generated. You can now run it, launch compile.sh then run.sh');
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

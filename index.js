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
    type: 'list',
    name: 'source',
    message: 'Which version of the starter do you want to clone',
    choices: ['v1.1.13', 'latest (may not be compatible with the generator)']
  },
  {
    type: 'list',
    name: 'wallet',
    message: 'Do you want to add the Wallet executable installer (experimental)',
    choices: ['Yes', 'No']
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
  		  getAsync('cd ' + folderName + ' && sh ./jar.sh').then(data => {
  			     console.log('Jar files created');

             if(answers.wallet === 'Yes') {
               console.log('');
               console.log('5. Setting up the electron wallet');
               const changes12 = replace.sync({
                 files: 'wallet-electron/index.html',
                 from: /GTD Wallet/g,
                 to: appName
               });
               const changes13 = replace.sync({
                 files: 'wallet-electron/index.html',
                 from: /37876/g,
                 to: answers.api_server_port
               });
               console.log('Modified files:', changes12.join(', '));
               console.log('Modified files:', changes13.join(', '));

               getAsync('cp ./' + folderName + '/' + appName + '.jar ./wallet-electron/blockchain.jar && cp -R ./' + folderName +'/lib ./wallet-electron/  && cp -R ./' + folderName +'/conf ./wallet-electron/   && cp -R ./' + folderName +'/html ./wallet-electron/ && cp -R ./templates/wallet-electron ./').then(data => {
                 console.log('Files for the wallet copied');
                 console.log('');
                 console.log('The wallet is now ready to be built, go to wallet-electron folder, install the dependencies and run yarn:dist to get the wallet installer');
               }).catch(error => {
                 console.log('An error occured', error)
               })
             } else {
               console.log('');
             }

             console.log('Congratulations, your Cryptocurrency is now generated. You can now run it, launch run.sh or the jar file.');


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

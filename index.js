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
    message: "What is the name of your blockchain (example: SuperCoin)"
  },
  {
    type: 'input',
    name: 'coin_symbol',
    message: "What is the name the symbol of your coin (example: SPC)"
  },
  {
    type: 'input',
    name: 'default_peer_port',
    message: "What is the port that you want to use for the peer node (example: 97874)"
  },
  {
    type: 'input',
    name: 'testnet_peer_port',
    message: "What is the port that you want to use for the peer testnet node (example: 96874)"
  },
  {
    type: 'input',
    name: 'api_server_port',
    message: "What is the port that you want to use for the api server port (example: 97876)"
  }
];

inquirer.prompt(questions).then(answers => {
  const folderName = answers.application;
  const appName = answers.application;

  console.log('1. Cloning the nxt-clone-starter')
  const getAsync = Promise.promisify(cmd.get, { multiArgs: true, context: cmd });
  getAsync('git clone https://bitbucket.org/Jelurida/nxt-clone-starter ' + answers.application).then(data => {
    console.log('Repository cloned successfully');

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

             // Copy the wallet

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

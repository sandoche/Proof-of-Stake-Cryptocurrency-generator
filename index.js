/**
 * Proof of Stake Cryptocurrency generator
 */

'use strict';
const inquirer = require('inquirer');
const cmd = require('node-cmd');
const Promise = require('bluebird');

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
  // Rename the different files (check the commits)
  // copy the genesis block & other parameters

  // git clone https://bitbucket.org/Jelurida/nxt-clone-starter answers.application

  console.log('1. Cloning the nxt-clone-starter')

  const getAsync = Promise.promisify(cmd.get, { multiArgs: true, context: cmd });

  getAsync('git clone https://bitbucket.org/Jelurida/nxt-clone-starter ' + answers.application).then(data => {
    console.log('Repository cloned successfully');
  }).catch(err => {
    console.log('An error occured', err)
  })
});

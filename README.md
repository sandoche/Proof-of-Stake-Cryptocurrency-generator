# Proof-of-Stake-Cryptocurrency-generator
Create your own Proof of Stake cryptocurrency with its own blockchain based on "Nxt Blockchain Creation Kit".
It should satisfy the requirements of the Jelurida Public License version 1.1 for the Nxt Public Blockchain Platform.
Basically 10% of your tokens should be given to the owners of the Nxt Cryptocurrency.

This generator will assist you building your NXT clone.
If you are an advanced user follow directly the official tutorial:
* Nxt-clone-starter: https://bitbucket.org/Jelurida/nxt-clone-starter
* Introduction to the NXT Blockchain Creation Kit: https://www.youtube.com/watch?v=6Wg3uv07GU4

## Requirements
* Java
* NodeJS

## Step 1 - Download the Nxt blockchain and create the genesis block
1. Clone this repository with `git clone https://github.com/sandoche/Proof-of-Stake-Cryptocurrency-generator`, also feel free to fork this repository!
2. Go to the cloned folder and install the npm dependencies with `npm install`
3. Run the Nxt Blockchain with `npm run step-1:nxt:run`
4. The Nxt wallet will open, create an account and save the private keys securely, wait until the full blockchain is synced, this can take a few hours. Copy your nxt address and also its public key.
5. Open the file `docs/config/newGenesisAccounts.json`, this file will define the repartition of the coins your are creating. On the first block creation 1 billion of coins will be distributed, 10% of them will be distributed to the Nxt holders (this is part of the Jelurida Public License). You have to put the list of accounts you want to credit in the first block (the genesis block) and its matching public key. The total of the amounts should be 90000000000000000 if you don't know what to do, just put your NXT address your created (Step 1-4), and it's public key like this and save it. If you are not sure about what you are doing check this video: https://www.youtube.com/watch?v=6Wg3uv07GU4
```
{
    "balances": {
         "my nxt address here": 90000000000000000
     },
     "publicKeys": [
         "the public key of this nxt address"
     ]
 }
```
6. Go to http://localhost:7876/test?requestTag=ADDONS click on "downloadJPLSnapshot" then upload your file "newGenesisAccounts.json" and copy paste the height you can see in your wallet as in the screenshot below, then submit. A file will be generated after a few minutes, save it as "genesisAccounts.json" in the `templates/conf/data` folder.
--- SCREENSHOT ---
7. Create another account, and save its public address in the file `templates/conf/data/genesisParameter.json`, also edit the epochBeginning with the current date.
8. Now (or later) delete the nxt blockchain with `npm run step-1:nxt:delete` after closing the wallet

## Step 2 - Create your own Cryptocurrency
* Todo
* Tell the user to put the icons in the icons folders
* Tell the user where are the settings

## Step 3 - Create the executable wallet files (optionnal)
* Todo

## Step 4 - Host your nodes in some servers
* Todo

## Todo
- [] How to install NodeJS
- [] How to install Java
- [] How to use the generator

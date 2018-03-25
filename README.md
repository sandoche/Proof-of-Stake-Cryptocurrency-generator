# Proof-of-Stake-Cryptocurrency-generator
Create your own Proof of Stake cryptocurrency with its own blockchain based on "Nxt Blockchain Creation Kit". It should satisfy the requirements of the Jelurida Public License version 1.1 for the Nxt Public Blockchain Platform.
Basically 10% of your tokens should be given to the owners of the Nxt Cryptocurrency.

## Step 1 - Download the Nxt blockchain and create the genesis block
1. Download and install the last Nxt package from https://bitbucket.org/Jelurida/nxt/downloads
2. Copy the conf/nxt-default.properties to conf/nxt.properties and put this setting: nxt.addOns=nxt.addons.JPLSnapshot
3. Run the node, running run.sh or run.bat (for windows), create a wallet, and run the node, it will download the full blockchain it can take a few hours.
4. Then create your input file like the following:
```
{
    "balances": {
         "NXT-NZKH-MZRE-2CTT-98NPZ": 30000000000000000,
         "NXT-X5JH-TJKJ-DVGC-5T2V8": 30000000000000000,
         "NXT-LTR8-GMHB-YG56-4NWSE": 30000000000000000
     },
     "publicKeys": [
         "bf0ced0472d8ba3df9e21808e98e61b34404aad737e2bae1778cebc698b40f37",
         "39dc2e813bb45ff063a376e316b10cd0addd7306555ca0dd2890194d37960152",
         "011889a0988ccbed7f488878c62c020587de23ebbbae9ba56dd67fd9f432f808"
     ]
 }
 ```
 This 3 accounts will be the one receiving the first coins of the genesis block.
 Each public key should math each account, you can have only one if you feel like, but the total should be 90000000000000000.
 5. Once the Nxt blockchain is sync go to http://localhost:7876/test?requestTag=ADDONS and upload your file. It will generate the genesis block, just save it.

## Step 2 - Create your own Cryptocurrency
* Todo

## Step 3 - Create the executable wallet files (optionnal)
* Todo

## Step 4 - Host your nodes in some servers
* Todo
